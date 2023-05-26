package greenHouse.unipi.it.model;

import greenHouse.unipi.it.DAO.ResourceDAO;
import greenHouse.unipi.it.threads.CoapClientThread;

public class LightSensor extends Sensor{
    private static LightSensor INSTANCE;
    private static int is_night = 0;
    private LightSensor() {
	min = 32000;
	max = 100000;
    }

    public static LightSensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new LightSensor();
        }

        return INSTANCE;
    }

    public void setActionMin(){
        ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
        new CoapClientThread(resourceDAO, "up").start();
    }
    public void setActionMax(){
        ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
        new CoapClientThread(resourceDAO, "down").start();
    }

    public void setNight(boolean b) {
        if(b)
            is_night = 1;
        else
            is_night = 0;
    }

    public int getIsNight() {
        return is_night;
    }
}
