import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum Opcode {
    AND(InstructionFormat.R, 0x0, 0x24),
    OR(InstructionFormat.R, 0x0, 0x25),
    ADD(InstructionFormat.R, 0x0, 0x20),
    ADDI(InstructionFormat.I, 0x8, null),
    SLL(InstructionFormat.R, 0x0, 0x0),
    SUB(InstructionFormat.R, 0x0, 0x22),
    SLT(InstructionFormat.R, 0x0, 0x2A),
    BEQ(InstructionFormat.I, 0x4, null),
    BNE(InstructionFormat.I, 0x5, null),
    LW(InstructionFormat.I, 0x23, null),
    SW(InstructionFormat.I, 0x2B, null),
    J(InstructionFormat.J, 0x2, null),
    JR(InstructionFormat.R, 0x0, 0x8),
    JAL(InstructionFormat.J, 0x3, null);

    private final InstructionFormat instructionFormat;
    private final Integer opcodeBitPattern;
    private final Integer functBitPattern;

    Opcode(final InstructionFormat instructionFormat, final Integer opcodeBitPattern, final Integer functBitPattern) {
        this.instructionFormat = instructionFormat;
        this.opcodeBitPattern = opcodeBitPattern;
        this.functBitPattern = functBitPattern;
    }

    public InstructionFormat instructionFormat() {
        return instructionFormat;
    }

    public Integer opcodeBitPattern() {
        return opcodeBitPattern;
    }

    public Integer functBitPattern() {
        return functBitPattern;
    }

    /**
     * Find the opcode by its name
     *
     * @param opcodeName the opcode name
     * @return the opcode
     */
    public static Opcode getByString(String opcodeName) {
        return Arrays.stream(values())
                .filter(opcode -> opcode.name().equalsIgnoreCase(opcodeName))
                .findFirst()
                .orElseThrow();
    }

    public static Pattern opcodePattern() {
        final StringBuilder builder = new StringBuilder();

        final List<String> names = Arrays.stream(values())
                .map(Opcode::name)
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());

        Collections.reverse(names);

        for (final String name : names) {
            builder.append("^").append(name).append("|").append("^").append(name.toLowerCase(Locale.ROOT)).append("|");
        }

        return Pattern.compile("(" + builder.substring(0, builder.length() - 1) + ")");
    }

    public static List<Opcode> getAllByInstructionFormat(InstructionFormat format) {
        return Arrays.stream(values())
                .filter(opcode -> opcode.instructionFormat == format)
                .collect(Collectors.toList());
    }
}
