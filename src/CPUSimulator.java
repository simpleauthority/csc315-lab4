import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class CPUSimulator {
    private final Emulator emulator;
    private Deque<Instruction> pipeline;
    private int programCounter;
    private int cycles;

    public CPUSimulator(Emulator emulator) {
        this.emulator = emulator;
        pipeline = new LinkedBlockingDeque<>(4);
        programCounter = 0;
    }

    public final void dumpPipelineRegisterState() {
        final String format = "%-7s\t%-7s\t%-7s\t%-7s\t%-7s\n" +
                "%-7d\t%-7s\t%-7s\t%-7s\t%-7s\n";

        final String[] plReg = Arrays.stream(pipeline.toArray(new Instruction[4]))
                .map(r -> {
                    if (r == null) {
                        return "empty";
                    } else {
                        return r.opcode().name().toLowerCase(Locale.ROOT);
                    }
                })
                .toArray(String[]::new);

        System.out.println();
        System.out.printf(
                format,
                "pc", "if/id", "id/exe", "exe/mem", "mem/wb",
                programCounter, plReg[0], plReg[1], plReg[2], plReg[3]
        );
        System.out.println();
    }

    public final void run() {
        while (true) {
            if (!runOneCycle()) break;
        }

        System.out.println("Program complete");
        printTimingInformation();
    }

    public final void runNCycles(int n) {
        for (int i = 0; i < n; i++) {
            runOneCycle();
        }
    }

    public final boolean runOneCycle() {
        if (emulator.hasMoreInstructions()) {
            if (pipeline.size() == 4) {
                pipeline.pollLast();
            }

            pipeline.addFirst(emulator.emulateOneInstruction());
        } else {
            if (pipeline.pollLast() == null) {
                return false;
            }
        }

        programCounter = emulator().programCounter();
        cycles++;
        dumpPipelineRegisterState();
        return true;
    }

    public final void printTimingInformation() {
        final int instCount = emulator.instructions().size();
        System.out.printf(
                "CPI = %.2f\tCycles = %d\tInstructions = %d\n",
                (float) cycles / instCount,
                cycles,
                instCount
        );
    }

    public final void reset() {
        pipeline.clear();
        programCounter = 0;
        cycles = 0;
        emulator.reset();
    }

    public Emulator emulator() {
        return emulator;
    }
}
