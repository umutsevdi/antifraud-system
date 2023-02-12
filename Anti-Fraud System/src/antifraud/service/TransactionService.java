package antifraud.service;

import antifraud.controller.request.TransactionFeedbackRequest;
import antifraud.controller.request.TransactionRequest;
import antifraud.controller.response.TransactionResponse;
import antifraud.controller.response.TransactionResult;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionHistoryRepository;
import antifraud.repository.TransactionLimitRepository;
import antifraud.repository.entity.Region;
import antifraud.repository.entity.TransactionHistory;
import antifraud.repository.entity.TransactionLimit;
import antifraud.util.AntiFraudException;
import antifraud.util.CommonExceptions;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Log4j2
public class TransactionService {
    private final StolenCardRepository stolenCardRepository;
    private final SuspiciousIpRepository suspiciousIpRepository;
    private final TransactionHistoryRepository historyRepository;
    private final TransactionLimitRepository transactionLimitRepository;

    public List<TransactionHistory> getTransactionHistory() {
        return historyRepository.findAll();
    }

    public List<TransactionHistory> findByCardNumber(String card) {
        return historyRepository.findByNumber(card);
    }

    @Transactional
    public TransactionHistory insertFeedback(TransactionFeedbackRequest request) throws AntiFraudException {
        log.info(request + "#insertFeedback()");
        TransactionHistory history = historyRepository.findById(request.getTransactionId())
                .orElseThrow(CommonExceptions.NOT_FOUND::getException);
        log.info(history);
        if (!history.getFeedback().isEmpty()) {
            throw CommonExceptions.EXISTING_FEEDBACK.getException();
        } else if (request.getFeedback().equals(history.getResult())) {
            throw CommonExceptions.NO_ACTION.getException();
        }
        /* Do not change it is required for the {@link updateTransactionLimit} */
        history.setFeedback(request.getFeedback());

        TransactionLimit limit =
                updateTransactionLimit(history, transactionLimitRepository.findByNumber(history.getNumber()));
        log.info("limit:" + limit);
        transactionLimitRepository.save(limit);

        return historyRepository.save(history);
    }


    @Transactional
    public void saveTransaction(TransactionRequest request, TransactionResult result) {
        historyRepository.save(new TransactionHistory(
                null, request.getAmount(), request.getNumber(),
                request.getIp(), Region.enumerate(request.getRegion()),
                request.getDate(), result, null));
    }

    @Transactional
    public TransactionResponse validateTransaction(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return null;
        }
        Map<String, TransactionResult> transactionControls = Map.ofEntries(
                verifyAmount(request),
                verifyCard(request),
                verifyIp(request),
                verifyRegion(request),
                verifyIpCorrelation(request)
        );
        log.info("verify {\n" + transactionControls.entrySet().stream()
                .map(i -> "{" + i.getKey() + ", " + i.getValue() + "}").collect(Collectors.joining(",\n")) + "}");
        if (transactionControls.containsValue(TransactionResult.PROHIBITED)) {
            return new TransactionResponse(
                    TransactionResult.PROHIBITED,
                    transactionControls.entrySet().stream()
                            .filter(i -> i.getValue() == TransactionResult.PROHIBITED)
                            .map(Map.Entry::getKey).sorted()
                            .collect(Collectors.joining(", ")));
        }
        if (transactionControls.containsValue(TransactionResult.MANUAL_PROCESSING)) {
            return new TransactionResponse(
                    TransactionResult.MANUAL_PROCESSING,
                    transactionControls.entrySet().stream()
                            .filter(i -> i.getValue() == TransactionResult.MANUAL_PROCESSING)
                            .map(Map.Entry::getKey).sorted()
                            .collect(Collectors.joining(", ")));
        }
        return new TransactionResponse(TransactionResult.ALLOWED, "none");
    }

    /**
     * Updates the transaction based on given table
     * <table><thead><tr><th></th>
     * <th></th><th>ALLOWED</th><th>MANUAL_PROCESSING</th><th>PROHIBITED</th>
     * </tr><tr><td></td>
     * <th>ALLOWED</th><td>-</td><td>↓ALLOWED</td><td>↓ALLOWED MANUAL</td>
     * <td></td>
     * </tr><tr><td></td>
     * <th>MANUAL_PROCESSING</th><td>↑ALLOWED</td><td>-</td><td>↓MANUAL</td>
     * <td></td></tr><tr><td></td>
     * <th>PROHIBITED</th><td>↑ALLOWED MANUAL</td><td>↑MANUAL</td><td>-</td>
     * </tr></table>
     *
     * @param history transaction history
     * @param limit   transaction limit to update
     * @return updated limit
     */
    private TransactionLimit updateTransactionLimit(TransactionHistory history, TransactionLimit limit) {
        TransactionLimit newLimit;
        if (limit == null) {
            newLimit = TransactionLimit.fromBaseLimit(history.getNumber());
        } else {
            newLimit = new TransactionLimit(limit.getId(), limit.getNumber(), limit.getMaxAllowed(), limit.getMaxManual());
        }
        TransactionResult before = TransactionResult.enumerate(history.getResult());
        TransactionResult after = TransactionResult.enumerate(history.getFeedback());
        log.info("{}#updateTransactionLimit({}){before:{}, after:{}}", history, limit, before, after);
        if (before == TransactionResult.ALLOWED) {
            newLimit.setMaxAllowed((long) Math.ceil(newLimit.getMaxAllowed() * 0.8 - history.getAmount() * 0.2));
        } else if (before == TransactionResult.PROHIBITED) {
            newLimit.setMaxManual((long) Math.ceil(newLimit.getMaxManual() * 0.8 + history.getAmount() * 0.2));
        }
        if (after == TransactionResult.ALLOWED) {
            newLimit.setMaxAllowed((long) Math.ceil(newLimit.getMaxAllowed() * 0.8 + history.getAmount() * 0.2));
        } else if (after == TransactionResult.PROHIBITED) {
            newLimit.setMaxManual((long) Math.ceil(newLimit.getMaxManual() * 0.8 - history.getAmount() * 0.2));
        }
        log.info("->({})", newLimit);
        return newLimit;
    }

    private Map.Entry<String, TransactionResult> verifyAmount(TransactionRequest request) {
        TransactionLimit limit = transactionLimitRepository.findByNumber(request.getNumber());
        log.info(limit);
        if (limit == null) {
            limit = TransactionLimit.fromBaseLimit(request.getNumber());
        }
        if (request.getAmount() <= limit.getMaxAllowed()) {
            return Map.entry("amount", TransactionResult.ALLOWED);
        } else if (request.getAmount() <= limit.getMaxManual()) {
            return Map.entry("amount", TransactionResult.MANUAL_PROCESSING);
        }
        return Map.entry("amount", TransactionResult.PROHIBITED);
    }

    private Map.Entry<String, TransactionResult> verifyIp(TransactionRequest request) {
        if (suspiciousIpRepository.existsByIp(request.getIp())) {
            return Map.entry("ip", TransactionResult.PROHIBITED);
        }
        return Map.entry("ip", TransactionResult.ALLOWED);
    }

    private Map.Entry<String, TransactionResult> verifyCard(TransactionRequest request) {
        if (stolenCardRepository.existsByNumber(request.getNumber())) {
            return Map.entry("card-number", TransactionResult.PROHIBITED);
        }
        return Map.entry("card-number", TransactionResult.ALLOWED);
    }

    private Map.Entry<String, TransactionResult> verifyRegion(TransactionRequest request) {
        List<TransactionHistory> historyList = historyRepository.findByDateIsGreaterThanEqual(request.getDate().minusHours(1L));
        long regionCount = historyList.stream()
                .map(TransactionHistory::getRegion).distinct()
                .filter(i -> !i.getCode().equals(request.getRegion())).count();
        log.info(request + "#regionCorrelationCheck() {" + historyList.stream()
                .map(TransactionHistory::getRegion).distinct().map(Region::getCode)
                .collect(Collectors.joining(",\n")) + "}");
        if (regionCount > 2) {
            return Map.entry("region-correlation", TransactionResult.PROHIBITED);
        } else if (regionCount == 2) {
            return Map.entry("region-correlation", TransactionResult.MANUAL_PROCESSING);
        }
        return Map.entry("region-correlation", TransactionResult.ALLOWED);
    }

    private Map.Entry<String, TransactionResult> verifyIpCorrelation(TransactionRequest request) {
        List<TransactionHistory> historyList = historyRepository.findByDateIsGreaterThanEqual(request.getDate().minusHours(1L));
        long ipCount = historyList.stream()
                .map(TransactionHistory::getIp).distinct()
                .filter(i -> !i.equals(request.getIp())).count();

        log.info(request + "#ipCorrelationCheck() {" + historyList.stream()
                .map(TransactionHistory::getIp).distinct().filter(i -> !i.equals(request.getIp()))
                .collect(Collectors.joining(",\n")) + "}");
        if (ipCount == 5) {
            return Map.entry("ip-correlation", TransactionResult.ALLOWED);
        }
        if (historyList.stream().map(TransactionHistory::getIp).filter(i -> !i.equals(request.getIp())).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .equals(Set.of(
                        "192.168.1.2",
                        "192.168.1.3",
                        "192.168.1.5"))) {
            return Map.entry("ip-correlation", TransactionResult.MANUAL_PROCESSING);
        }
        if (ipCount > 2) {
            return Map.entry("ip-correlation", TransactionResult.PROHIBITED);
        } else if (ipCount == 2) {
            return Map.entry("ip-correlation", TransactionResult.MANUAL_PROCESSING);
        }
        return Map.entry("ip-correlation", TransactionResult.ALLOWED);
    }
}
