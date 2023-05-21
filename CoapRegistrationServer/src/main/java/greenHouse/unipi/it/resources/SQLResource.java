package greenHouse.unipi.it.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.InetAddress;

public class SQLResource extends CoapResource {
    public SQLResource(String name) {
        super(name);
        setObservable(true);
    }

    public void handlePOST(CoapExchange exchange) {
        byte[] request = exchange.getRequestPayload();
        String s = new String(request);
	System.out.println(s);
	InetAddress addr = exchange.getSourceAddress();
	System.out.println(addr);
        JSONObject json = null;
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(s);
        }catch (Exception err){
            // do something
        }

        Response response = null;
        if (json.containsKey("name")){
             // recupera info
             response = new Response(CoAP.ResponseCode.CREATED);
        }else{
            response = new Response(CoAP.ResponseCode.BAD_REQUEST);
        }
        exchange.respond(response);
    }
}
