import java.util.Set;

class LoyaltyScanner {
    private final Set<String> validCards = Set.of("LOYAL001", "LOYAL002", "LOYAL003", "LOYAL004", "LOYAL005", "LOYAL006", "LOYAL007", "LOYAL008", "LOYAL009", "LOYAL010");

    boolean isValid(String cardId) {
        return validCards.contains(cardId.trim().toUpperCase());
    }
}
