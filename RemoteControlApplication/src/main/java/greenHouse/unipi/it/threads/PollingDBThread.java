package greenHouse.unipi.it.threads;

import greenHouse.unipi.it.DAO.ResourceDAO;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PollingDBThread extends Thread{
    private static final String url = "jdbc:mysql://localhost:3306/SmartGreenHouse";
    private static final String username = "root";
    private static final String password = "Mysql2023!";
    private static final String[] types = {"humidity","co2","light","temp"};
    private static Timestamp lastTimestamp;
    private static Map<String,Integer> values;

    public PollingDBThread(){
        java.util.Date date = new java.util.Date();
        lastTimestamp = new java.sql.Timestamp(date.getTime());
        values = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            sleep(10*1000);     //30 seconds
            // read from DB
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                java.util.Date date = new java.util.Date();
                lastTimestamp = new java.sql.Timestamp(date.getTime());
                for (String str : types) {
                    PreparedStatement ps = connection.prepareStatement(
                            "SELECT value FROM dataSensed WHERE timestamp > ? AND type = ? ORDER BY timestamp DESC LIMIT 1;");
                    ps.setTimestamp(1, lastTimestamp);
                    ps.setString(2, str);
                    ResultSet res = ps.executeQuery();
                    while (res.next()) {
                        int value = res.getInt("value");
                        values.put(str, value);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Cannot connect the database!");
            }
            // check values
            if(!values.isEmpty()){                      // ResultSet was not empty
                for(String type : types){
                    if(values.containsKey(type)){       // check if there's a new value for each type
                        //TODO eventually send command
                        System.out.println(values.get(type));
                    }
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
