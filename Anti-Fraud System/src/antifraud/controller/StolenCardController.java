package antifraud.controller;

import antifraud.repository.StolenCardRepository;
import antifraud.repository.entity.StolenCard;
import antifraud.service.AuthorizationService;
import antifraud.service.dto.UserRole;
import antifraud.util.AntiFraudException;
import antifraud.util.StaticValidationUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/antifraud/stolencard")
public class StolenCardController {
    private final StolenCardRepository repository;
    private final AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<List<StolenCard>> getSuspiciousIps(HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), List.of(UserRole.SUPPORT));
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);

    }

    @PostMapping
    public ResponseEntity<StolenCard> addCard(@RequestBody Map.Entry<String, String> card, HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), List.of(UserRole.SUPPORT));
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        if (card == null || !"number".equals(card.getKey()) || !StaticValidationUtil.checkCardNumber(card.getValue())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (repository.existsByNumber(card.getValue())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        StolenCard c = repository.save(new StolenCard(card.getValue()));
        return new ResponseEntity<>(c, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map.Entry<String, String>> removeSuspiciousIp(@PathVariable("id") String number, HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), List.of(UserRole.SUPPORT));
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        if (!StaticValidationUtil.checkCardNumber(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!repository.existsByNumber(number)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.deleteByNumber(number);
        return new ResponseEntity<>(Map.entry("status", "Card " + number + " successfully removed!"), HttpStatus.OK);
    }
}
