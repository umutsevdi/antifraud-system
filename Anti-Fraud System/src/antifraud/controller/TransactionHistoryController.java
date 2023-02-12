package antifraud.controller;

import antifraud.repository.entity.TransactionHistory;
import antifraud.service.AuthorizationService;
import antifraud.service.TransactionService;
import antifraud.service.dto.UserRole;
import antifraud.util.AntiFraudException;
import antifraud.util.StaticValidationUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/antifraud/history")
public class TransactionHistoryController {
    private final AuthorizationService authorizationService;
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionHistory>> getTransactionHistory(HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), UserRole.SUPPORT);
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        return new ResponseEntity<>(transactionService.getTransactionHistory(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<TransactionHistory>> getTransactionHistoryByCard(@PathVariable String id, HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), UserRole.SUPPORT);
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        if (!StaticValidationUtil.checkCardNumber(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<TransactionHistory> transactionHistory = transactionService.findByCardNumber(id);
        if (transactionHistory.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(transactionHistory, HttpStatus.OK);
    }
}
