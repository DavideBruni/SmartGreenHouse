package greenHouse.unipi.it.threads;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;

public class CoapClientThread extends Thread{

    private String actuatorIp;
    private String resource;
    private JSONObject payload;

    public CoapClientThread(String actuatorIp, String resource, JSONObject payload) {
        this.actuatorIp = actuatorIp;
        this.resource = resource;
        this.payload = payload;
    }

    public void run() {
        String uri = "coap://"+actuatorIp+"/"+resource;
        CoapClient client = new CoapClient(uri);
        CoapResponse response = client.put(String.valueOf(payload), MediaTypeRegistry.APPLICATION_JSON);
        System.out.println(response.getResponseText());
    }
}
