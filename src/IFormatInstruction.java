public class IFormatInstruction extends Instruction {
    private int rs;
    private int rt;
    private int imm;

    public IFormatInstruction(final Opcode opcode) {
        super(opcode);
    }

    public int rs() {
        return rs;
    }

    public void setRs(int rs) {
        this.rs = rs;
    }

    public int rt() {
        return rt;
    }

    public void setRt(int rt) {
        this.rt = rt;
    }

    public int imm() {
        return imm;
    }

    public void setImm(int imm) {
        this.imm = imm;
    }

    @Override
    public String assemble() {
        return String.format(
                "%s %s %s %s",
                StringUtil.zeroPadBinary(6, opcode().opcodeBitPattern()),
                StringUtil.zeroPadBinary(5, rs),
                StringUtil.zeroPadBinary(5, rt),
                StringUtil.zeroPadBinary(16, imm)
        );
    }

    @Override
    public String toString() {
        return "IFormatInstruction{" +
                "opcode=" + opcode() +
                ", rs=" + rs +
                ", rt=" + rt +
                ", imm=" + imm +
                '}';
    }
}
