public class InvalidInstructionException extends RuntimeException {
    public InvalidInstructionException(String message) {
        super(String.format("invalid instruction: %s", message));
    }
}
