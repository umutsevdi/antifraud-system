package antifraud.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class TransactionFeedbackRequest {
    private Long transactionId;
    private String feedback;
}
