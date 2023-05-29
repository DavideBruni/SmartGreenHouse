package greenHouse.unipi.it.model;

import greenHouse.unipi.it.DAO.ResourceDAO;
import greenHouse.unipi.it.threads.CoapClientThread;

public class HumiditySensor extends Sensor{
    private static HumiditySensor INSTANCE;

    private HumiditySensor() {
	min = 60;
	max = 75;
    }

    public static HumiditySensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new HumiditySensor();
        }

        return INSTANCE;
    }
    public void setActionMin(){
        ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("sprinkler");
        if(resourceDAO.getStatus().equals("off"))
            new CoapClientThread(resourceDAO, "on").start();
    }
    public void setActionMax(){
        ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("sprinkler");
        if(resourceDAO.getStatus().equals("on"))
            new CoapClientThread(resourceDAO, "off").start();
    }
}
