package antifraud.controller;

import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.entity.SuspiciousIp;
import antifraud.service.AuthorizationService;
import antifraud.service.dto.UserRole;
import antifraud.util.AntiFraudException;
import antifraud.util.StaticValidationUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Controller
@RequestMapping("/api/antifraud/suspicious-ip")
public class SuspiciousIpController {
    private final SuspiciousIpRepository repository;
    private final AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<List<SuspiciousIp>> getSuspiciousIps(HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), List.of(UserRole.SUPPORT));
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);

    }

    @PostMapping
    public ResponseEntity<SuspiciousIp> addSuspiciousIp(@RequestBody Map.Entry<String, String> ipAddress, HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), List.of(UserRole.SUPPORT));
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        if (ipAddress == null || !"ip".equals(ipAddress.getKey()) || !StaticValidationUtil.checkIp(ipAddress.getValue())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (repository.existsByIp(ipAddress.getValue())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        SuspiciousIp suspiciousIp = repository.save(new SuspiciousIp(ipAddress.getValue()));
        return new ResponseEntity<>(suspiciousIp, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map.Entry<String, String>> removeSuspiciousIp(@PathVariable("id") String ipAddress, HttpServletRequest request) {
        try {
            authorizationService.validateEntry(request.getUserPrincipal(), List.of(UserRole.SUPPORT));
        } catch (AntiFraudException e) {
            return new ResponseEntity<>(e.getException().getStatusCode());
        }
        if (!StaticValidationUtil.checkIp(ipAddress)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!repository.existsByIp(ipAddress)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.deleteByIp(ipAddress);

        return new ResponseEntity<>(Map.entry("status", "IP " + ipAddress + " successfully removed!"), HttpStatus.OK);
    }
}
