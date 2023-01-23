import java.util.Arrays;

public enum Register {
    ZERO("$0", 0),
    ZERO_STR("$zero", 0),
    AT("$at", 1),
    V0("$v0", 2),
    V1("$v1", 3),
    A0("$a0", 4),
    A1("$a1", 5),
    A2("$a2", 6),
    A3("$a3", 7),
    T0("$t0", 8),
    T1("$t1", 9),
    T2("$t2", 10),
    T3("$t3", 11),
    T4("$t4", 12),
    T5("$t5", 13),
    T6("$t6", 14),
    T7("$t7", 15),
    S0("$s0", 16),
    S1("$s1", 17),
    S2("$s2", 18),
    S3("$s3", 19),
    S4("$s4", 20),
    S5("$s5", 21),
    S6("$s6", 22),
    S7("$s7", 23),
    T8("$t8", 24),
    T9("$t9", 25),
    K0("$k0", 26),
    K1("$k1", 27),
    GP("$gp", 28),
    SP("$sp", 29),
    FP("$fp", 30),
    RA("$ra", 31);

    private final String name;
    private final int number;

    Register(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String registerName() {
        return name;
    }

    public int number() {
        return number;
    }

    public static Register getByRegisterName(final String registerName) {
        return Arrays.stream(Register.values())
                .filter(reg -> reg.registerName().equalsIgnoreCase(registerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid register \"%s\" encountered.", registerName)));
    }

    public static Register getByRegisterNumber(final int registerNumber) {
        return Arrays.stream(Register.values())
                .filter(reg -> reg.number() == registerNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid register \"%d\" encountered.", registerNumber)));
    }
}
