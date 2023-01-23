public class JFormatInstruction extends Instruction {
    private int address;

    public JFormatInstruction(final Opcode opcode) {
        super(opcode);
    }

    public int address() {
        return address;
    }

    public void setAddress(final int address) {
        this.address = address;
    }

    @Override
    public String assemble() {
        return String.format(
                "%s %s",
                StringUtil.zeroPadBinary(6, opcode().opcodeBitPattern()),
                StringUtil.zeroPadBinary(26, address)
        );
    }

    @Override
    public String toString() {
        return "JFormatInstruction{" +
                "opcode=" + opcode() +
                ", address=" + address +
                '}';
    }
}
