package greenHouse.unipi.it.model;

public class Co2Sensor {
    private static Co2Sensor INSTANCE;
    private int min;
    private int value;

    private Co2Sensor() {
    }

    public static Co2Sensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Co2Sensor();
        }

        return INSTANCE;
    }

    public void setMin(int min) {
        this.min = min;
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
}
