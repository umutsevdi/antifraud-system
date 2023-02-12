package antifraud.repository.entity;

import antifraud.controller.response.TransactionResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("transactionId")
    private Long id;
    @Column
    private Long amount;
    @Column
    private String number;
    @Column
    private String ip;
    @Column
    private Region region;
    @Column
    private LocalDateTime date;
    @Column
    private String result;
    @Column
    private String feedback;

    public TransactionHistory(Long id, Long amount, String number, String ip, Region region, LocalDateTime date, TransactionResult result, TransactionResult feedback) {
        this.id = id;
        this.amount = amount;
        this.number = number;
        this.ip = ip;
        this.region = region;
        this.date = date;
        this.result = result != null ? result.name() : "";
        this.feedback = feedback != null ? feedback.name() : "";
    }
}
