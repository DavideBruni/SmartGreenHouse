package greenHouse.unipi.it.threads;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

public class CoapClientThread extends Thread{

    private String actuatorIp;
    private String resource;
    private String payload;

    public CoapClientThread(String actuatorIp, String resource, String payload) {
        this.actuatorIp = actuatorIp;
        this.resource = resource;
        this.payload = payload;
    }

    public void run() {
        String uri = "coap://"+actuatorIp+"/"+resource;
        CoapClient client = new CoapClient(uri);
        Request req = new Request(CoAP.Code.PUT);
        //req.getOptions().addUriQuery("action="+payload);
	req.setPayload("action="+payload);
        req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
        CoapResponse response = client.advanced(req);
	System.out.println("Send");
        System.out.println(response.getResponseText());
    }
}
