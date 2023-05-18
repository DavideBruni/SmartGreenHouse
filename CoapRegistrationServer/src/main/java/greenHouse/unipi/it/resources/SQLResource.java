package greenHouse.unipi.it.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SQLResource extends CoapResource {
    public SQLResource(String name) {
        super(name);
        setObservable(true);
    }

    public void handlePOST(CoapExchange exchange) {
        byte[] request = exchange.getRequestPayload();
        String s = new String(request);
        JSONObject json = null;
        try {
            JSONParser parser = new JSONParser();
            json = (JSONObject) parser.parse(s);
        }catch (Exception err){
            // do something
        }

        Response response = null;
        if (json.containsKey("ipAddress")){
             // recupera info
             response = new Response(CoAP.ResponseCode.CREATED);
        }else{
            response = new Response(CoAP.ResponseCode.BAD_REQUEST);
        }
        exchange.respond(response);
    }
}
