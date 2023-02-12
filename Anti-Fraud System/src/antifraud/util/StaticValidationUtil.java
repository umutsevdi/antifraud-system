package antifraud.util;

import antifraud.controller.request.TransactionRequest;
import antifraud.repository.entity.Region;

public class StaticValidationUtil {
    public static boolean checkTransaction(TransactionRequest transaction) {
        return checkIp(transaction.getIp()) &&
                checkCardNumber(transaction.getNumber()) &&
                checkRegion(transaction.getRegion());
    }

    private static boolean checkRegion(String region) {
        return Region.enumerate(region) != null;
    }

    public static boolean checkIp(String ip) {
        if (ip == null) return false;
        String[] indexes = ip.split("\\.");
        if (indexes.length != 4) return false;
        for (String i : indexes) {
            try {
                int v = Integer.parseInt(i);
                if (v < 0 || v > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkCardNumber(String card) {
        if (card == null || card.length() != 16) {
            return false;
        }
        try {
            return applyLuhnAlgorithm(card);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean applyLuhnAlgorithm(String creditCardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = creditCardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(creditCardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
