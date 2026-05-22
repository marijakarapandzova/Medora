package medora.models.enums;

public enum BloodType {
    A_POS("A+"),
    A_NEG("A-"),
    B_POS("B+"),
    B_NEG("B-"),
    AB_POS("AB+"),
    AB_NEG("AB-"),
    O_POS("O+"),
    O_NEG("O-");

    private final String value;

    BloodType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BloodType fromValue(String value) {
        for (BloodType b : values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Invalid blood type: " + value);
    }
}