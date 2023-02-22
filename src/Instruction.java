public abstract class Instruction {
    private final Opcode opcode;
    private boolean branchTaken;
    private int branchNotTakenPc;

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

    public int branchNotTakenPc() {
        return branchNotTakenPc;
    }

    public void setBranchNotTakenPc(int branchNotTakenPc) {
        this.branchNotTakenPc = branchNotTakenPc;
    }

    public abstract String assemble();
}
