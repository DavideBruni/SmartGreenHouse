package greenHouse.unipi.it.threads;

import greenHouse.unipi.it.DAO.ResourceDAO;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.json.simple.JSONObject;

public class CoapClientThread extends Thread{

    private ResourceDAO resourceDAO;
    private String payload;

    public CoapClientThread(ResourceDAO resourceDAO, String payload) {
        this.resourceDAO = resourceDAO;
        this.payload = payload;
    }

    public void run() {
        String uri = "coap://"+resourceDAO.getIp()+"/"+resourceDAO.getResource();
        CoapClient client = new CoapClient(uri);
        Request req = new Request(CoAP.Code.PUT);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", payload);
        req.setPayload(jsonObject.toJSONString());
        req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
        CoapResponse response = client.advanced(req);
        if (response!=null) {
            CoAP.ResponseCode code = response.getCode();
            switch (code) {
                case CHANGED:
                    resourceDAO.updateStatus(payload);
                    break;
                case BAD_REQUEST:
                    System.err.println("Internal application error!");
                    break;
                case BAD_OPTION:
                    System.err.println("BAD_OPTION error");
                    break;
                default:
                    System.err.println("Actuator error!");
                    resourceDAO.changeStatus("Error");
                    break;
            }
        }
    }
}
