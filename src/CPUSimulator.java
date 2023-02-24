import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingDeque;

public class CPUSimulator {
    private final Emulator emulator;
    private final Deque<Instruction> pipeline;
    private int programCounter;
    private int cycles;
    private boolean shouldSquashNext;
    private final Deque<Instruction> afterBranchInstructions;
    private int actuallyExecutedInstructions;

    public CPUSimulator(Emulator emulator) {
        this.emulator = emulator;
        pipeline = new LinkedBlockingDeque<>(4);
        afterBranchInstructions = new LinkedBlockingDeque<>(2);
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
        if (!afterBranchInstructions.isEmpty()) {
            if (pipeline.size() == 4) {
                // pipeline is full, expire last
                pipeline.pollLast();
            }

            pretendEmulateOne();
            programCounter++;
            finishOneCycle(dumpPipeline);
            return true;
        }

        if (emulator.hasMoreInstructions()) {
            if (pipeline.size() == 4) {
                // pipeline is full, expire last
                pipeline.pollLast();
            }

            final Instruction[] pipelineArr = pipelineAsArray();
            final Instruction ifId = pipelineArr[0]; // fetch/decode
            final Instruction idEx = pipelineArr[1]; // decode/execute
            final Instruction exMe = pipelineArr[2]; // ex/mem

            if (shouldSquashNext) {
                pipeline.addFirst(new SquashInstruction());
                programCounter = emulator.programCounter();
                shouldSquashNext = false;
                finishOneCycle(dumpPipeline);
                return true;
            }

            if (ifId != null) {
                if ((ifId.opcode() == Opcode.BEQ || ifId.opcode() == Opcode.BNE) && ifId.branchTaken()) {
                    // if we hit a taken branch, grab the next 3 instructions, so we can pretend...
                    emulator.peekNInstructions(ifId.branchNotTakenPc(), 2)
                            .forEach(afterBranchInstructions::addFirst);

                    programCounter = ifId.branchNotTakenPc() + 1;

                    if (!afterBranchInstructions.isEmpty()) {
                        pretendEmulateOne();
                    }

                    finishOneCycle(dumpPipeline);
                    return true;
                }
            }

            if (idEx != null) {
                // detect and handle use-after-load
                if (idEx.opcode() == Opcode.LW) {
                    final IFormatInstruction loadInst = (IFormatInstruction) idEx;

                    if (ifId instanceof RFormatInstruction useInst) {
                        if (checkAndProceedStall(loadInst, useInst)) {
                            finishOneCycle(dumpPipeline);
                            return true;
                        }
                    }

                    if (ifId instanceof IFormatInstruction useInst) {
                        if (checkAndProceedStall(loadInst, useInst)) {
                            finishOneCycle(dumpPipeline);
                            return true;
                        }
                    }
                }
            }

            if (exMe != null) {
                if ((exMe.opcode() == Opcode.BEQ || exMe.opcode() == Opcode.BNE) && exMe.branchTaken()) {
                    // if we hit a taken branch, we need to squash the prior three instructions
                    pipeline.clear();
                    pipeline.addFirst(exMe);
                    for (int i = 0; i < 3; i++) pipeline.addFirst(new SquashInstruction());
                    programCounter++;
                    finishOneCycle(dumpPipeline);
                    return true;
                }
            }

            proceedEmulateOne();
            finishOneCycle(dumpPipeline);
            return true;
        }

        if (pipeline.pollLast() != null) {
            finishOneCycle(dumpPipeline);
            return true;
        }

        return false;
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

    private void pretendEmulateOne() {
        Instruction inst = afterBranchInstructions.pollLast();
        if (inst != null) {
            pipeline.addFirst(inst);
        }
    }

    private void proceedEmulateOne() {
        pipeline.addFirst(emulator.emulateOneInstruction());

        if (emulator.hadUncondJump()) {
            shouldSquashNext = true;
        }

        programCounter++;
        actuallyExecutedInstructions++;
    }

    private void finishOneCycle(boolean dumpPipeline) {
        cycles++;
        if (dumpPipeline) dumpPipelineRegisterState();
    }

    public final void printTimingInformation() {
        final int instCount = actuallyExecutedInstructions;
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
