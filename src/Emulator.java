import java.util.Arrays;
import java.util.List;

public class Emulator {
    private final List<Instruction> instructions;
    private final int[] registers = new int[32];
    private final int[] memory = new int[8192];

    private int programCounter = 0;

    public Emulator(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    /**
     * Reads the value stored in the given register
     * @param registerIdx the register to read
     * @return the value stored in the register
     */
    public final int readRegister(final int registerIdx) {
        return registers[registerIdx];
    }

    /**
     * Writes the given value to the given register.
     * @param registerIdx the register to write to
     * @param value the value to write
     */
    public final void writeRegister(final int registerIdx, final int value) {
        registers[registerIdx] = value;
    }

    /**
     * Retrieves all (supported) registers and displays the data stored in them.
     */
    public final void dumpRegisters() {
        final String format = "pc = %-10d\n" +
                "$0 = %-11d$v0 = %-10d$v1 = %-10d$a0 = %-10d\n" +
                "$a1 = %-10d$a2 = %-10d$a3 = %-10d$t0 = %-10d\n" +
                "$t1 = %-10d$t2 = %-10d$t3 = %-10d$t4 = %-10d\n" +
                "$t5 = %-10d$t6 = %-10d$t7 = %-10d$s0 = %-10d\n" +
                "$s1 = %-10d$s2 = %-10d$s3 = %-10d$s4 = %-10d\n" +
                "$s5 = %-10d$s6 = %-10d$s7 = %-10d$t8 = %-10d\n" +
                "$t9 = %-10d$sp = %-10d$ra = %-10d\n";

        System.out.println();
        System.out.printf(
                format,
                programCounter,
                registers[0], registers[2], registers[3], registers[4],
                registers[5], registers[6], registers[7], registers[8],
                registers[9], registers[10], registers[11], registers[12],
                registers[13], registers[14], registers[15], registers[16],
                registers[17], registers[18], registers[19], registers[20],
                registers[21], registers[22], registers[23], registers[24],
                registers[25], registers[29], registers[31]
        );
        System.out.println();
    }

    /**
     * Reads the value stored in the given memory cell.
     * @param memoryIdx the memory cell to read
     * @return the value stored in the memory cell
     */
    public final int readMemory(final int memoryIdx) {
        return memory[memoryIdx];
    }

    /**
     * Writes the given value to the given memory cell.
     * @param memoryIdx the memory cell to write to
     * @param value the value to write
     */
    public final void writeMemory(final int memoryIdx, final int value) {
        memory[memoryIdx] = value;
    }

    /**
     * Retrieves the memory cells between the given lower and upper address bounds, inclusive.
     * @param lower the inclusive lower bound memory address
     * @param upper the inclusive upper bound memory address
     */
    public final void dumpMemory(int lower, int upper) {
        System.out.println();
        for (int i = lower; i <= upper; i++) {
            System.out.printf("[%d] = %d\n", i, memory[i]);
        }
        System.out.println();
    }

    /**
     * Resets the emulator. All registers and memory cells are reinitialized to zero, and the program
     * counter is reset to 0.
     */
    public void reset() {
        Arrays.fill(registers, 0);
        Arrays.fill(memory, 0);
        programCounter = 0;
    }

    public List<Instruction> instructions() {
        return instructions;
    }

    public boolean hasMoreInstructions() {
        return programCounter < instructions.size();
    }

    public int programCounter() {
        return programCounter;
    }

    /**
     * Runs a single instruction pointed to by the current program counter.
     */
    public Instruction emulateOneInstruction() {
        final Instruction currentInstruction = instructions.get(programCounter);

        if (currentInstruction instanceof RFormatInstruction inst) {
            final int rs = readRegister(inst.rs());
            final int rt = readRegister(inst.rt());

            switch (inst.opcode()) {
                case ADD -> writeRegister(inst.rd(), rs + rt);
                case AND -> writeRegister(inst.rd(), rs & rt);
                case OR -> writeRegister(inst.rd(), rs | rt);
                case SLL -> writeRegister(inst.rd(), rt << inst.shamt());
                case SUB -> writeRegister(inst.rd(), rs - rt);
                case SLT -> writeRegister(inst.rd(), rs < rt ? 1 : 0);
                case JR -> {
                    programCounter = rs;
                    return inst;
                }
            }
        } else if (currentInstruction instanceof IFormatInstruction inst) {
            final int rs = readRegister(inst.rs());
            final int rt = readRegister(inst.rt());

            switch (inst.opcode()) {
                case ADDI -> writeRegister(inst.rt(), rs + inst.imm());
                case BEQ -> {
                    if (rs == rt) {
                        programCounter += 1 + inst.imm();
                        inst.setBranchTaken(true);
                        return inst;
                    }
                }
                case BNE -> {
                    if (rs != rt) {
                        programCounter += 1 + inst.imm();
                        inst.setBranchTaken(true);
                        return inst;
                    }
                }
                case LW -> writeRegister(inst.rt(), readMemory(rs + inst.imm()));
                case SW -> writeMemory(rs + inst.imm(), rt);
            }
        } else if (currentInstruction instanceof JFormatInstruction inst) {
            switch (inst.opcode()) {
                case J -> {
                    programCounter = inst.address();
                    return inst;
                }
                case JAL -> {
                    writeRegister(31, programCounter + 1);
                    programCounter = inst.address();
                    return inst;
                }
            }
        }

        programCounter++;
        return currentInstruction;
    }
}
