package antifraud.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TransactionResponse {
    private TransactionResult result;
    private String info;
}
