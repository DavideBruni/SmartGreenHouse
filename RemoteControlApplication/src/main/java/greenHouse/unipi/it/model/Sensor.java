package greenHouse.unipi.it.model;

public abstract class Sensor {
    protected int min;
    protected int max;
    protected int value;


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

    public abstract void setActionMin();
    public abstract void setActionMax();
}
