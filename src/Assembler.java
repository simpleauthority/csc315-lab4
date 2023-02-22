import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    private static final Pattern relativeImmediatePattern = Pattern.compile("(\\d)\\((\\$\\w{1,})\\)");
    private static final Pattern opcodeExtractorPattern = Pattern.compile("^(\\w+)");
    private final List<String> lines;
    private final Map<String, Integer> labelAddresses = new HashMap<>();
    private final List<Instruction> instructions = new ArrayList<>();

    public Assembler(final List<String> lines) {
        this.lines = lines;
    }

    public final List<String> lines() {
        return lines;
    }

    public final Map<String, Integer> labelAddresses() {
        return labelAddresses;
    }

    public final List<Instruction> instructions() {
        return instructions;
    }

    /**
     * Assembler pass one - identify labels and record memory addresses
     */
    public final void identifyLabels() {
        final Pattern pattern = Pattern.compile("(\\w+\\:)");
        final Iterator<String> itr = lines.iterator();

        int addr = 0;

        while (itr.hasNext()) {
            String line = itr.next();

            final Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String match = matcher.group();

                line = line.substring(match.length());
                if (line.isBlank()) {
                    itr.remove();
                } else {
                    lines.set(addr, line);
                }

                labelAddresses.put(match.substring(0, match.length() - 1), addr);
            }

            addr++;
        }
    }

    /**
     * Assembler pass two - identify and parse all instructions
     */
    public final void identifyInstructions() throws InvalidInstructionException {
        for (int addr = 0; addr < lines.size(); addr++) {
            String line = lines.get(addr);

            final Matcher matcher = Opcode.opcodePattern().matcher(line);
            if (matcher.find()) {
                final String group = matcher.group();
                line = line.substring(group.length());
                final Opcode opcode = Opcode.getByString(group);
                final String[] arguments = line.split(",");

                Instruction instruction = null;
                switch (opcode.instructionFormat()) {
                    case R -> {
                        RFormatInstruction rInst = new RFormatInstruction(opcode);
                        int rs, rt, rd, shamt;
                        if (opcode == Opcode.SLL) {
                            rs = 0;
                            rt = Register.getByRegisterName(arguments[1].trim()).number();
                            rd = Register.getByRegisterName(arguments[0].trim()).number();
                            shamt = Integer.parseInt(arguments[2]);
                        } else if (opcode == Opcode.JR) {
                            rs = Register.getByRegisterName(arguments[0].trim()).number();
                            rt = 0;
                            rd = 0;
                            shamt = 0;
                        } else {
                            rs = Register.getByRegisterName(arguments[1].trim()).number();
                            rt = Register.getByRegisterName(arguments[2].trim()).number();
                            rd = Register.getByRegisterName(arguments[0].trim()).number();
                            shamt = 0;
                        }

                        rInst.setRs(rs);
                        rInst.setRt(rt);
                        rInst.setRd(rd);
                        rInst.setShamt(shamt);

                        instruction = rInst;
                    }
                    case I -> {
                        IFormatInstruction iInst = new IFormatInstruction(opcode);
                        int rt = Register.getByRegisterName(arguments[0].trim()).number();
                        int rs, immediate;

                        if (opcode == Opcode.LW || opcode == Opcode.SW) {
                            final Matcher immMatcher = relativeImmediatePattern.matcher(arguments[1].trim());
                            if (immMatcher.find()) {
                                rs = Register.getByRegisterName(immMatcher.group(2)).number();
                                immediate = Integer.parseInt(immMatcher.group(1));
                            } else {
                                throw new RuntimeException(String.format("Invalid relative immediate pattern " +
                                        "encountered. Could not extract rs and immediate value. " +
                                        "Syntax immediate(register). Instead found \"%s\"", arguments[1].trim()));
                            }
                        } else {
                            if (opcode == Opcode.BEQ || opcode == Opcode.BNE) {
                                // for BEQ and BNE, rs is read first...
                                rs = Register.getByRegisterName(arguments[0].trim()).number();
                                rt = Register.getByRegisterName(arguments[1].trim()).number();
                            } else {
                                rs = Register.getByRegisterName(arguments[1].trim()).number();
                            }

                            String immStr = arguments[2].trim();
                            try {
                                immediate = Integer.parseInt(immStr);
                            } catch (NumberFormatException ex) {
                                if (labelAddresses.containsKey(immStr)) {
                                    immediate = labelAddresses.get(immStr);
                                } else {
                                    throw new RuntimeException(String.format("Encountered unknown immediate value label \"%s\"...bailing.", immStr));
                                }
                            }

                            if (opcode == Opcode.BEQ || opcode == Opcode.BNE) {
                                final int begin = addr + 1;
                                if (immediate == begin) {
                                    immediate = 0;
                                } else {
                                    immediate -= begin;
                                }
                            }
                        }

                        iInst.setRs(rs);
                        iInst.setRt(rt);
                        iInst.setImm(immediate);

                        instruction = iInst;
                    }
                    case J -> {
                        JFormatInstruction jInst = new JFormatInstruction(opcode);

                        String addrStr = arguments[0].trim();
                        int address;
                        try {
                            address = Integer.parseInt(addrStr);
                        } catch (NumberFormatException ex) {
                            if (labelAddresses.containsKey(addrStr)) {
                                address = labelAddresses.get(addrStr);
                            } else {
                                throw new RuntimeException(String.format("Encountered unknown immediate value label \"%s\"...bailing.", addrStr));
                            }
                        }

                        jInst.setAddress(address);

                        instruction = jInst;
                    }
                }

                instructions.add(instruction);
            } else {
                final Matcher invalidOpcodeMatcher = opcodeExtractorPattern.matcher(line);
                if (invalidOpcodeMatcher.find()) {
                    throw new InvalidInstructionException(invalidOpcodeMatcher.group());
                } else {
                    throw new InvalidInstructionException(String.format("failed to find opcode; line: %s", line));
                }
            }
        }
    }

    public final List<String> assemble() {
        // Pass One
        identifyLabels();

        // Pass Two
        boolean hadInvalidInstruction = false;
        String invalidInstructionMsg = "";
        try {
            identifyInstructions();
        } catch (InvalidInstructionException ex) {
            hadInvalidInstruction = true;
            invalidInstructionMsg = ex.getMessage();
        }

        // Perform translation to machine code
        final List<String> assembled = new ArrayList<>();

        for (Instruction instruction : instructions) {
            final String instStr = instruction.assemble();
            if (instStr != null) {
                assembled.add(instStr);
            }
        }

        if (hadInvalidInstruction && !invalidInstructionMsg.isBlank()) {
            assembled.add(invalidInstructionMsg);
        }

        return assembled;
    }
}
