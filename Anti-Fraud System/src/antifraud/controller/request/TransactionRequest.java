package antifraud.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@ToString
public class TransactionRequest {
    private Long amount;
    private String ip;
    private String number;
    private String region;
    private LocalDateTime date;
}
