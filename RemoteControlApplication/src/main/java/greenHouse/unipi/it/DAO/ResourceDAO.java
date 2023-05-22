package greenHouse.unipi.it.DAO;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;

import java.sql.*;

public class ResourceDAO {

    private static final String url = "jdbc:mysql://localhost:3306/SmartGreenHouse";
    private static final String username = "root";
    private static final String password = "Mysql2023!";

    private String ip;
    private String resource;

    public ResourceDAO(String ip, String resource) {
        this.ip = ip;
        this.resource = resource;
    }

    public static ResourceDAO retrieveInformation(String actuator){
        String resource = null;
        ResourceDAO resourceDAO = null;
        // in a real environment this mapping (or the following query) need to be changed
        switch(actuator){
            case "window":
                resource = "actuator_window";
                break;
            case "sprinkler":
                resource = "actuator_sprinkler";
                break;
            case "light":
                resource = "actuator_light";
		break;
	    default:
		return resourceDAO;
        }
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            PreparedStatement ps = connection.prepareStatement("SELECT ip FROM actuators WHERE resource = ? LIMIT 1;");
            ps.setString(1,resource);
            ResultSet res = ps.executeQuery();
            while(res.next()){
                String ip = res.getString("ip");
                resourceDAO = new ResourceDAO(ip,resource);
            }
        } catch (SQLException e) {
            System.err.println("Cannot connect the database!");
        }
        return resourceDAO;
    }

    public String getIp() {
        return "["+ip+"]";
    }

    public String getResource() {
        return resource;
    }
}
