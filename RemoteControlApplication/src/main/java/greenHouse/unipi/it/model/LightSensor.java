package greenHouse.unipi.it.model;

public class LightSensor {
    private static LightSensor INSTANCE;
    private int min;
    private int max;
    private int value;

    private LightSensor() {
    }

    public static LightSensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LightSensor();
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

    /*public setLightHours(){

    }*/

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
