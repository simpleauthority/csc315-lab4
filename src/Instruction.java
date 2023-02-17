public abstract class Instruction {
    private final Opcode opcode;
    private boolean branchTaken;

    public Instruction(final Opcode opcode) {
        this.opcode = opcode;
    }

    public Opcode opcode() {
        return opcode;
    }

    public boolean branchTaken() {
        return branchTaken;
    }

    public void setBranchTaken(boolean branchTaken) {
        this.branchTaken = branchTaken;
    }

    public abstract String assemble();
}
