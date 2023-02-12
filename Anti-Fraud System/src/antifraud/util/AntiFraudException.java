package antifraud.util;

public class AntiFraudException extends Exception {
    private CommonExceptions exception;

    public AntiFraudException(CommonExceptions c) {
        super(c.getName());
        this.exception = c;
    }

    public CommonExceptions getException() {
        return exception;
    }
}
