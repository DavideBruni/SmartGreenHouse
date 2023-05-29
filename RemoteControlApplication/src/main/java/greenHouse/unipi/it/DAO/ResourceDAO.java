package greenHouse.unipi.it.DAO;


import java.sql.*;

/**
 * A Data Access Object (DAO) class for retrieving and updating information about resources (actuators) from a database.
 *
 * This class provides methods to retrieve and update information about resources (actuators) from a MySQL database.
 * It uses a predefined database URL, username, and password for establishing the database connection.
 * The class stores information about the IP address, resource name, and status of an actuator.
 *
 * The class includes a static method 'retrieveInformation' to retrieve information about an actuator from the database.
 * The method takes an actuator as input and returns a 'ResourceDAO' object with the corresponding IP address, resource name,
 * and status retrieved from the database.
 *
 * The class includes methods for changing the status of an actuator and updating it in the database.
 * The 'changeStatus' method updates the status of the actuator in the database, and the 'updateStatus' method updates the
 * status based on a command. The method checks the current status and command and performs the corresponding updates.
 *
 */

public class ResourceDAO {

    private static final String url = "jdbc:mysql://localhost:3306/SmartGreenHouse";
    private static final String username = "root";
    private static final String password = "Mysql2023!";

    private String ip;
    private String resource;
    private String status;

    public ResourceDAO(String ip, String resource) {
        this.ip = ip;
        this.resource = resource;
    }

    public ResourceDAO(String ip, String resource, String status) {
        this.ip = ip;
        this.resource = resource;
        this.status = status;
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

            PreparedStatement ps = connection.prepareStatement("SELECT ip,status FROM actuators WHERE resource = ? LIMIT 1;");
            ps.setString(1,resource);
            ResultSet res = ps.executeQuery();
            while(res.next()){
                String ip = res.getString("ip");
                String status = res.getString("status");
                resourceDAO = new ResourceDAO(ip,resource,status);
            }
        } catch (SQLException e) {
            System.err.println("Cannot connect the database!");
        }
        return resourceDAO;
    }

    public void changeStatus(String new_status){

        if(new_status.equals(status)){
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            PreparedStatement ps = connection.prepareStatement("UPDATE actuators SET status = ? WHERE ip=?;");
            ps.setString(1,new_status);
            ps.setString(2,ip);
            int row_changed = ps.executeUpdate();
            if (row_changed == 0)
                    throw new Exception();
            else
                status = new_status;
        } catch (SQLException e) {
            System.err.println("Cannot connect the database!");
        }catch (Exception e){
            System.err.println("Error while updating the status on the DB!");
        }
    }

    public void updateStatus(String command){
        if (resource.equals("actuator_window")){
            if(command.equals("open"))
                changeStatus("open");
            else if(command.equals("close"))
                changeStatus("close");
        }else if(resource.equals("actuator_light")){
            if(command.equals("up")){
                if(status.equals("off"))
                    changeStatus("level_1");
                else if(status.equals("level_1"))
                    changeStatus("level_2");
            }else if(command.equals("down")){
                if(status.equals("level_2"))
                    changeStatus("level_1");
                else if(status.equals("level_1"))
                    changeStatus("off");
            }
        }else if(resource.equals("actuator_sprinkler")){
            if(command.equals("on"))
                changeStatus("on");
            else if(command.equals("off"))
                changeStatus("off");
        }
    }

    public String getIp() {
        return "["+ip+"]";
    }

    public String getResource() {
        return resource;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Actuator status{\n" +
                "\tip = [" + ip + ']' +
                ",\n\tresource = '" + resource + '\'' +
                ",\n\tstatus = '" + status + '\'' +
                "\n}";
    }
}
