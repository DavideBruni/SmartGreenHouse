package greenHouse.unipi.it.model;

public class HumiditySensor {
    private static HumiditySensor INSTANCE;
    private int min;
    private int max;
    private int value;

    private HumiditySensor() {
    }

    public static HumiditySensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new HumiditySensor();
        }

        return INSTANCE;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
