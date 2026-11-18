import java.util.Map;

/* All money is stored as whole pence (int) to avoid floating-point errors. */
final class Money {
    private Money() { }

    static String format(int pence) {
        return String.format("\u00A3%d.%02d", pence / 100, pence % 100);
    }

    static String label(int pence) {
        return pence < 100 ? pence + "p" : format(pence);
    }

    static String describe(Map<Integer, Integer> items) {
        if (items.isEmpty()) {
            return "nothing";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : items.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(label(e.getKey())).append(" x").append(e.getValue());
        }
        return sb.toString();
    }
}
