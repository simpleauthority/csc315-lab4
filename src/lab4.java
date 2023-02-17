import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class lab4 {
    public static void main(final String[] args) {
        new lab4().runProgram(args);
    }

    public void runProgram(final String[] args) {
        if (args.length < 1) {
            System.err.println("Invalid syntax.");
            System.err.printf("Syntax: java lab4 <file> [script]%n");
            System.exit(1);
            return;
        }

        final Assembler assembler = new Assembler(readFile(args[0]));
        assembler.assemble();

        final Emulator emulator = new Emulator(assembler.instructions());
        final CPUSimulator simulator = new CPUSimulator(emulator);
        final Repl repl = new Repl(simulator);

        if (args.length < 2) {
            repl.start();
        } else {
            repl.startScript(new File(args[1]));
        }
    }

    private List<String> readFile(final String path) {
        final File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("The specified file does not exist.");
        } else if (!file.canRead()) {
            throw new RuntimeException("The specified file does not allow reading.");
        }

        final List<String> lines = new ArrayList<>();
        try (final Scanner scanner = new Scanner(new FileInputStream(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // skip blank lines and comments
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                // get rid of comments at end of lines
                final int commentIdx = line.indexOf("#");
                if (commentIdx != -1) {
                    line = line.substring(0, commentIdx);
                }

                // get rid of all remaining whitespace in instruction
                lines.add(line.replaceAll("\\s*", ""));
            }
        } catch (IOException ex) {
            System.err.println("Encountered IOException while reading the file.");
            ex.printStackTrace(System.err);
            System.exit(1);
        }

        return lines;
    }
}
