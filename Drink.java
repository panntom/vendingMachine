class Drink {
    private final String name;
    private final int price;

    Drink(String name, int pricePence) {
        this.name = name;
        this.price = pricePence;
    }

    String getName() {
        return name;
    }

    int getPrice() {
        return price;
    }

    /** 10% loyalty discount (all four prices give whole pence). */
    int getDiscountedPrice() {
        return price * 90 / 100;
    }
}
