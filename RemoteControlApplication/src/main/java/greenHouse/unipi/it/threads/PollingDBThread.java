package greenHouse.unipi.it.threads;

import greenHouse.unipi.it.model.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A thread class for polling data from a database and updating sensor values.
 *
 * This class extends the 'Thread' class and provides functionality to periodically poll
 * data from a MySQL database and update the corresponding sensor values. The thread is
 * responsible for connecting to the database, executing queries, and updating the sensor
 * values based on the retrieved data.
 *
 * The class uses a predefined database URL, username, and password for establishing the
 * database connection. It maintains a map of sensor types, a timestamp for the last read
 * operation, and a map of values to check for changes since the last read.
 * */

public class PollingDBThread extends Thread{
    private static final String url = "jdbc:mysql://localhost:3306/SmartGreenHouse";
    private static final String username = "root";
    private static final String password = "Mysql2023!";
    private static Map<String, Sensor> types;       // key --> Sensor
    private static Timestamp lastTimestamp;
    private static Map<String,Integer> values;      // key --> value, in order to check only changed value wrt to the last read

	/**
	 * Constructs a 'PollingDBThread' object.
	 *
	 * This constructor initializes the 'lastTimestamp', 'values', and 'types' variables.
	 * It sets the initial 'lastTimestamp' to the current timestamp, initializes an empty
	 * 'values' map, and populates the 'types' map with the predefined sensor types and
	 * their corresponding instances.
	 */
    public PollingDBThread(){
        java.util.Date date = new java.util.Date();
        lastTimestamp = new java.sql.Timestamp(date.getTime());
        values = new HashMap<>();
        types = new HashMap<>();
        types.put("humidity", HumiditySensor.getInstance());
        types.put("co2", Co2Sensor.getInstance());
        types.put("temp", TempSensor.getInstance());
        types.put("light", LightSensor.getInstance());

    }

    @Override
    public void run() {
	
	while(true){        
		try {
		    sleep(3*1000);     //3 seconds
		    // read from DB
		    try (Connection connection = DriverManager.getConnection(url, username, password)) {
			
		        java.util.Date date = new java.util.Date();
		        Timestamp tempTimestamp = new java.sql.Timestamp(date.getTime());     // next time, I'll check if there will be command after this timestamp
				/* Iterates over the sensor types and executes a query to retrieve the latest value for
 				    each sensor type since the last timestamp. */
				for (String str : types.keySet()) {
		            PreparedStatement ps = connection.prepareStatement(
		                    "SELECT value FROM dataSensed WHERE timestamp > ? AND type = ? ORDER BY timestamp DESC LIMIT 1;");
		            ps.setTimestamp(1, lastTimestamp);
		            ps.setString(2, str);
		            ResultSet res = ps.executeQuery();
		            while (res.next()) {
		                int value = res.getInt("value");

		                values.put(str, value);
		                types.get(str).setValue(value);
		            }
		        }
			lastTimestamp = tempTimestamp;
		    } catch (SQLException e) {
		        System.err.println("Cannot connect the database!");
		    }
		    // check values
		    if(!values.isEmpty()){                      // ResultSet was not empty
				/* Checks the 'values' map to determine if there are any new values.
					- For each sensor type with a new value, performs the corresponding actions based on the minimum and maximum thresholds of the sensor.
				    - If the sensor type is "light" and it is currently night according to the LightSensor,the action is skipped. */
				for(String type : types.keySet()){
		            if(values.containsKey(type)){
		                int value = values.get(type);
				
		                String action = null;
		                if(type.equals("light")){
		                    if(LightSensor.getInstance().getIsNight()==1)
		                        continue;
		                }
		                if(value < types.get(type).getMin()){
		                    types.get(type).setActionMin();
		                }else if(value >= types.get(type).getMax()){

		                    types.get(type).setActionMax();
		                }


		            }
		        }
		    }

		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}
		values.clear();
	}
    }
}
