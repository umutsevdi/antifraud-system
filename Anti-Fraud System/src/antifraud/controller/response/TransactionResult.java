package antifraud.controller.response;

public enum TransactionResult {
    ALLOWED("ALLOWED"),
    MANUAL_PROCESSING("MANUAL_PROCESSING"),
    PROHIBITED("PROHIBITED");

    private String name;

    TransactionResult(String name) {
        this.name = name;
    }

    public static TransactionResult enumerate(String e) {
        for (TransactionResult i : TransactionResult.values())
            if (i.name.equals(e))
                return i;
        return null;
    }
}
