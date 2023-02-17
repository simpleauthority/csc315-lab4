import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingDeque;

public class CPUSimulator {
    private final Emulator emulator;
    private final Deque<Instruction> pipeline;
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

        final String[] plReg = Arrays.stream(pipelineAsArray())
                .map(r -> {
                    if (r == null) {
                        return "empty";
                    } else if (r.opcode() == Opcode.STALL) {
                        return "stall";
                    } else if (r.opcode() == Opcode.SQUASH) {
                        return "squash";
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
            if (!runOneCycle(false)) break;
        }

        System.out.println();
        System.out.println("Program complete");
        printTimingInformation();
        System.out.println();
    }

    public final void runNCycles(int n) {
        for (int i = 0; i < n; i++) {
            runOneCycle(true);
        }
    }

    public final boolean runOneCycle(boolean dumpPipeline) {
        if (emulator.hasMoreInstructions()) {
            if (pipeline.size() == 4) {
                // pipeline is full, expire last
                pipeline.pollLast();
            }

            // use-after-load detection
            final Instruction[] pipelineArr = pipelineAsArray();
            final Instruction ifId = pipelineArr[0]; // fetch/decode
            final Instruction idEx = pipelineArr[1]; // decode/execute
            final Instruction exMe = pipelineArr[2]; // execute/mem

            if (exMe != null && (exMe.opcode() == Opcode.BEQ || exMe.opcode() == Opcode.BNE)) {
                if (exMe.branchTaken()) {

                }
            }
            else if (idEx != null && idEx.opcode() == Opcode.LW) {
                    final IFormatInstruction loadInst = (IFormatInstruction) idEx;
                    if (ifId instanceof RFormatInstruction useInst) {
                        if (!checkAndProceedStall(loadInst, useInst)) {
                            proceedEmulateOne();
                        }
                    } else if (ifId instanceof IFormatInstruction useInst) {
                        if (!checkAndProceedStall(loadInst, useInst)) {
                            proceedEmulateOne();
                        }
                    } else {
                        proceedEmulateOne();
                    }
            }
            else {
                proceedEmulateOne();
            }
        } else {
            if (pipeline.pollLast() == null) {
                return false;
            }
        }

        cycles++;
        if (dumpPipeline) dumpPipelineRegisterState();
        return true;
    }

    private boolean checkAndProceedStall(IFormatInstruction loadInst, IFormatInstruction useInst) {
        if (loadInst.rt() == useInst.rs()) {
            checkAndProceedStall0(useInst);
            return true;
        }

        return false;
    }

    private boolean checkAndProceedStall(IFormatInstruction loadInst, RFormatInstruction useInst) {
        if (loadInst.rt() == useInst.rs() || loadInst.rt() == useInst.rt()) {
            checkAndProceedStall0(useInst);
            return true;
        }

        return false;
    }

    private void checkAndProceedStall0(Instruction useInst) {
        pipeline.removeFirst();
        pipeline.addFirst(new StallInstruction());
        pipeline.addFirst(useInst);
    }

    private void proceedEmulateOne() {
        pipeline.addFirst(emulator.emulateOneInstruction());
        programCounter = emulator().programCounter();
    }

    public final void printTimingInformation() {
        final int instCount = emulator.instructions().size();
        System.out.printf(
                "CPI = %.3f\tCycles = %d\tInstructions = %d\n",
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

    private Instruction[] pipelineAsArray() {
        return pipeline.toArray(new Instruction[4]);
    }
}
