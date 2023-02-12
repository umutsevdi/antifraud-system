package antifraud.controller;

import antifraud.controller.request.TransactionFeedbackRequest;
import antifraud.controller.request.TransactionRequest;
import antifraud.controller.response.TransactionResponse;
import antifraud.controller.response.TransactionResult;
import antifraud.repository.entity.TransactionHistory;
import antifraud.service.AuthorizationService;
import antifraud.service.TransactionService;
import antifraud.service.dto.UserRole;
import antifraud.util.AntiFraudException;
import antifraud.util.StaticValidationUtil;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@AllArgsConstructor
@Log4j2
@RestController
@RequestMapping("/api/antifraud/transaction")
public class TransactionController {
    private final AuthorizationService authorizationService;
    private final TransactionService transactionService;

    @PutMapping
    public ResponseEntity<TransactionHistory> feedback(@RequestBody TransactionFeedbackRequest feedback, HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), UserRole.SUPPORT);
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        log.info(feedback);
        if (TransactionResult.enumerate(feedback.getFeedback()) == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            TransactionHistory history = transactionService.insertFeedback(feedback);
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (AntiFraudException e) {
            log.info(e.getException());
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> handleTransaction(@RequestBody TransactionRequest transaction, HttpServletRequest request) {
        log.info(transaction);
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), UserRole.MERCHANT);
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        if (!StaticValidationUtil.checkTransaction(transaction)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        TransactionResponse response = transactionService.validateTransaction(transaction);
        if (response == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        transactionService.saveTransaction(transaction, response.getResult());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
