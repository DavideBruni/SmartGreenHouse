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
                ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
                if(!resourceDAO.getStatus().equals("level_2"))      // level_2 == to max light level
                    new CoapClientThread(resourceDAO, "up").start();
            }
        }else{
            ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("window");
            if(resourceDAO.getStatus().equals("open"))
                new CoapClientThread(resourceDAO, "close").start();
        }

    }
    public void setActionMax(){
        if(last_time_light = Boolean.FALSE){
            last_time_light = Boolean.TRUE;        // if the temperature remains HIGH, then I need to open the window
            ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
            if(!resourceDAO.getStatus().equals("off"))
                new CoapClientThread(resourceDAO, "down").start();
        }else {
            ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("window");
            if(resourceDAO.getStatus().equals("close"))
                new CoapClientThread(resourceDAO, "open").start();
        }
    }

}
