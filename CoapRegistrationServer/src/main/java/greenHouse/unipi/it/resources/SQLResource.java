package greenHouse.unipi.it.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLResource extends CoapResource {

    private static final String url = "jdbc:mysql://localhost:3306/SmartGreenHouse";
    private static final String username = "root";
    private static final String password = "Mysql2023!";

    public SQLResource(String name) {
        super(name);
        setObservable(true);        //TODO da controllare se setobseravable
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
        if (json.containsKey("name")){
            InetAddress addr = exchange.getSourceAddress();
		System.out.println(addr);
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
		
                PreparedStatement ps = connection.prepareStatement("INSERT INTO actuators (ip,resource) VALUES(?,?);");
                ps.setString(1,String.valueOf(addr).substring(1));
                ps.setString(2, (String)json.get("name"));
                ps.executeUpdate();
                if(ps.getUpdateCount()<1){
                    response = new Response(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
                }else{
                    response = new Response(CoAP.ResponseCode.CREATED);
                }
            } catch (SQLException e) {

                response = new Response(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
                System.err.println("Cannot connect the database!");
            }

        }else{
            response = new Response(CoAP.ResponseCode.BAD_REQUEST);
        }
        exchange.respond(response);
    }
}
