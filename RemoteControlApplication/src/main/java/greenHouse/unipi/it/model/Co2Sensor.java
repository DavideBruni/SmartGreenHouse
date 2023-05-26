package greenHouse.unipi.it.model;

import greenHouse.unipi.it.DAO.ResourceDAO;
import greenHouse.unipi.it.threads.CoapClientThread;

public class Co2Sensor extends Sensor{
    private static Co2Sensor INSTANCE;


    private Co2Sensor() {
	min = 300;
	max = 400;
    }

    public static Co2Sensor getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Co2Sensor();
        }

        return INSTANCE;
    }

    public void setActionMin(){
        ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("window");
        new CoapClientThread(resourceDAO, "open").start();
    }
    public void setActionMax(){}

}
