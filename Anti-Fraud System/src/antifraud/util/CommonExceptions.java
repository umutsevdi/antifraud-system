package antifraud.util;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum CommonExceptions {
    NO_PRINCIPAL("No User Principal Exception", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("Invalid Credentials Exception", HttpStatus.FORBIDDEN),
    ACCESS_DENIED("Access Denied Exception", HttpStatus.FORBIDDEN),

    /*Transaction Feedback Exceptions*/
    NOT_FOUND("Not Found Exception", HttpStatus.NOT_FOUND),
    EXISTING_FEEDBACK("Existing Transaction Feedback Exception", HttpStatus.CONFLICT),
    NO_ACTION("No Action Taken Exception", HttpStatus.UNPROCESSABLE_ENTITY);


    private final String name;
    private final HttpStatus statusCode;

    CommonExceptions(String name, HttpStatus statusCode) {
        this.name = name;
        this.statusCode = statusCode;
    }

    public AntiFraudException getException() {
        return new AntiFraudException(this);
    }
}

