public class RFormatInstruction extends Instruction {
    private int rs;
    private int rt;
    private int rd;
    private int shamt;
    private final int funct;

    public RFormatInstruction(final Opcode opcode) {
        super(opcode);
        this.funct = opcode.functBitPattern();
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

    public int rd() {
        return rd;
    }

    public void setRd(int rd) {
        this.rd = rd;
    }

    public int shamt() {
        return shamt;
    }

    public void setShamt(int shamt) {
        this.shamt = shamt;
    }

    public int funct() {
        return funct;
    }

    @Override
    public String assemble() {
        return String.format(
                "%s %s %s %s %s %s",
                StringUtil.zeroPadBinary(6, opcode().opcodeBitPattern()),
                StringUtil.zeroPadBinary(5, rs),
                StringUtil.zeroPadBinary(5, rt),
                StringUtil.zeroPadBinary(5, rd),
                StringUtil.zeroPadBinary(5, shamt),
                StringUtil.zeroPadBinary(6, funct)
        );
    }

    @Override
    public String toString() {
        return "RFormatInstruction{" +
                "opcode=" + opcode() +
                ", rs=" + rs +
                ", rt=" + rt +
                ", rd=" + rd +
                ", shamt=" + shamt +
                ", funct=" + funct +
                '}';
    }
}
