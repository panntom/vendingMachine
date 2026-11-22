import java.util.Set;

class CashValidator {
    private static final Set<Integer> ACCEPTED = Set.of(10, 20, 50, 100, 200, 500, 1000);

    boolean isValid(int valuePence) {
        return ACCEPTED.contains(valuePence);
    }
}