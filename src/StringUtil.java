public class StringUtil {
    /**
     * Converts an integer of known length to a zero-padded binary representation for display
     * <p><a href="https://stackoverflow.com/a/28198839">See this stack overflow post</a></p>
     *
     * @param len the length of the binary representation
     * @param num the integer to convert
     * @return the zero-padded binary representation
     */
    public static String zeroPadBinary(int len, int num) {
        return Integer.toBinaryString((1 << len) | (num & ((1 << len) - 1))).substring(1);
    }
}
