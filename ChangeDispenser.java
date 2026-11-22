import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Holds the coin float used for change; only coins (1p to 2 pounds) are used for change. */
class ChangeDispenser {
    private final Map<Integer, Integer> coins = new TreeMap<>(Comparator.reverseOrder());

    ChangeDispenser() {
        coins.put(200, 1);
        coins.put(100, 2);
        coins.put(50, 3);
        coins.put(20, 3);
        coins.put(10, 5);
        coins.put(5, 5);
        coins.put(2, 5);
        coins.put(1, 5);
    }

    boolean canGiveChange(int amount) {
        return plan(amount) != null;
    }

    /** Bounded change search: finds a valid combination when one exists. */
    private Map<Integer, Integer> plan(int amount) {
        if (amount < 0) return null;
        Map<Integer, Integer> result = new TreeMap<>(Comparator.reverseOrder());
        return searchChange(new ArrayList<>(coins.keySet()), 0, amount, result,
                new java.util.HashSet<>()) ? result : null;
    }

    private boolean searchChange(List<Integer> values, int index, int remaining,
                                 Map<Integer, Integer> result, Set<String> failed) {
        if (remaining == 0) return true;
        if (index == values.size()) return false;
        String key = index + ":" + remaining;
        if (failed.contains(key)) return false;
        int value = values.get(index);
        int max = Math.min(remaining / value, coins.get(value));
        for (int count = max; count >= 0; count--) {
            if (count > 0) result.put(value, count);
            else result.remove(value);
            if (searchChange(values, index + 1, remaining - count * value, result, failed))
                return true;
        }
        result.remove(value);
        failed.add(key);
        return false;
    }

    /** Return the exact coins/notes received; these are held pending completion. */
    Map<Integer, Integer> refundInserted(Map<Integer, Integer> inserted) {
        return new TreeMap<>(inserted);
    }

    Map<Integer, Integer> dispenseChange(int amount) {
        Map<Integer, Integer> given = plan(amount);
        if (given == null) {
            return Collections.emptyMap();
        }
        for (Map.Entry<Integer, Integer> e : given.entrySet()) {
            coins.put(e.getKey(), coins.get(e.getKey()) - e.getValue());
        }
        return given;
    }

    /** Coins inserted for a completed sale join the float (notes go to the cash box). */
    void depositCoins(Map<Integer, Integer> inserted) {
        for (Map.Entry<Integer, Integer> e : inserted.entrySet()) {
            if (coins.containsKey(e.getKey())) {
                coins.put(e.getKey(), coins.get(e.getKey()) + e.getValue());
            }
        }
    }

    Map<Integer, Integer> getCoinLevels() {
        return coins;
    }

    int getTotal() {
        int total = 0;
        for (Map.Entry<Integer, Integer> e : coins.entrySet()) {
            total += e.getKey() * e.getValue();
        }
        return total;
    }
}