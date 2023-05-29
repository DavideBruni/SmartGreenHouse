package greenHouse.unipi.it.threads;

import greenHouse.unipi.it.DAO.ResourceDAO;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.json.simple.JSONObject;


/**
 * A thread class for sending CoAP messages to an actuator.
 *
 * This class extends the 'Thread' class and provides functionality to send CoAP messages
 * to an actuator specified by a 'ResourceDAO' object.
 *
 * When the thread is started by invoking the 'run()' method, it performs the following steps:
 * 1. Constructs the CoAP URI based on the IP address and resource name obtained from 'resourceDAO'.
 * 2. Creates a 'CoapClient' object with the constructed URI.
 * 3. Creates a CoAP request with the PUT method and sets the payload to a JSON object containing
 *    the 'payload' value.
 * 4. Sets the accept option to expect a JSON response.
 * 5. Sends the CoAP request using the 'client.advanced(req)' method and stores the response.
 * 6. If the response is not null, checks the response code and performs the corresponding action:
 *    - If the code is CHANGED, updates the status of the actuator in 'resourceDAO'.
 *    - If the code is BAD_REQUEST, prints an error message indicating an internal application error.
 *    - If the code is BAD_OPTION, prints an error message indicating a bad option error.
 *    - For other response codes, prints an error message indicating an actuator error and changes
 *      the status of the actuator in 'resourceDAO' to "Error".
 *
 * @see Thread
 */

public class CoapClientThread extends Thread{

    private ResourceDAO resourceDAO;
    private String payload;


    /**
     * Constructs a 'CoapClientThread' object with the specified parameters.
     *
     * @param resourceDAO the 'ResourceDAO' object representing the actuator's information
     * @param payload     the payload for the CoAP message
     */
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
