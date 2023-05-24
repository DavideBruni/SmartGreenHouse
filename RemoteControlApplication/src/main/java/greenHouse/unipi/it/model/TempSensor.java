package greenHouse.unipi.it.model;

public class TempSensor {
    private static TempSensor INSTANCE;
    private int min;
    private int max;
    private int value;

    private TempSensor() {
    }

    public static TempSensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TempSensor();
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
