package greenHouse.unipi.it.model;

import greenHouse.unipi.it.DAO.ResourceDAO;
import greenHouse.unipi.it.threads.CoapClientThread;

public class TempSensor extends Sensor{
    private static TempSensor INSTANCE;
    private Boolean last_time_light;
    private TempSensor() {
        last_time_light = Boolean.FALSE;
	    min = 15;
	    max = 20;
    }

    public static TempSensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TempSensor();
        }

        return INSTANCE;
    }

    public void setActionMin(){
        if(Co2Sensor.getInstance().getValue()<Co2Sensor.getInstance().getMin()){        // I cannot close the window, co2 constraints
            if(LightSensor.getInstance().getIsNight()!=1) {
                last_time_light = Boolean.TRUE;
                ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
                new CoapClientThread(resourceDAO, "up").start();
            }
        }else{
            //if last time I increase the brightness, I put them down again in order to re-establish the origin situation
            last_time_light = Boolean.FALSE;
            ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("window");
            new CoapClientThread(resourceDAO, "close").start();
        }

    }
    public void setActionMax(){
        if(last_time_light = Boolean.TRUE){
            last_time_light = Boolean.FALSE;        // if the temperature remains HIGH, then I need to open the window
            ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
            new CoapClientThread(resourceDAO, "down").start();
        }else {
            ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("window");
            new CoapClientThread(resourceDAO, "open").start();
        }
    }

}
