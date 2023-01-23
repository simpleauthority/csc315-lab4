public abstract class Instruction {
    private final Opcode opcode;

    public Instruction(final Opcode opcode) {
        this.opcode = opcode;
    }

    public Opcode opcode() {
        return opcode;
    }

    public abstract String assemble();
}
