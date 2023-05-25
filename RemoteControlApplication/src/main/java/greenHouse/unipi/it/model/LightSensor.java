package greenHouse.unipi.it.model;

import greenHouse.unipi.it.DAO.ResourceDAO;
import greenHouse.unipi.it.threads.CoapClientThread;

public class LightSensor extends Sensor{
    private static LightSensor INSTANCE;
    private static boolean is_night = false;
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
        new CoapClientThread(resourceDAO.getIp(), resourceDAO.getResource(), "up").start();
    }
    public void setActionMax(){
        ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
        new CoapClientThread(resourceDAO.getIp(), resourceDAO.getResource(), "down").start();
    }

    public void setNight(boolean b) {
        is_night=b;
    }

    public boolean getIsNight() {
        return is_night;
    }
}
