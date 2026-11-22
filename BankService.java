
import java.util.Set;

class BankService {
    private final Set<String> declinedCards = Set.of("4000000000000002");

    boolean authorise(String cardNumber, int amountPence) {
        return !declinedCards.contains(cardNumber);
    }
}