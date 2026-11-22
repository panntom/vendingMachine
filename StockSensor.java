import java.util.LinkedHashMap;
import java.util.Map;

class StockSensor {
    private final Map<Drink, Integer> stock = new LinkedHashMap<>();

    void addDrink(Drink drink, int quantity) {
        stock.put(drink, quantity);
    }

    int getStock(Drink drink) {
        return stock.get(drink);
    }

    boolean isInStock(Drink drink) {
        return getStock(drink) > 0;
    }

    void removeOne(Drink drink) {
        stock.put(drink, getStock(drink) - 1);
    }

    void restockAll(int quantity) {
        for (Drink d : stock.keySet()) {
            stock.put(d, quantity);
        }
    }
}
