package antifraud.repository.entity;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class TransactionLimit {
    public static final long BASE_MAX_ALLOWED = 200L;
    public static final long BASE_MAX_MANUAL = 1500L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(View.UserView.class)
    private Long id;
    @Column
    private String number;
    @Column
    private Long maxAllowed;
    @Column
    private Long maxManual;

    public static TransactionLimit fromBaseLimit(String cardNumber) {
        return new TransactionLimit(null, cardNumber, TransactionLimit.BASE_MAX_ALLOWED, TransactionLimit.BASE_MAX_MANUAL);
    }
}
