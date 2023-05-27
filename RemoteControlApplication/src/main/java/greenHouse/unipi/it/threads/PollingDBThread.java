package greenHouse.unipi.it.threads;

import greenHouse.unipi.it.model.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PollingDBThread extends Thread{
    private static final String url = "jdbc:mysql://localhost:3306/SmartGreenHouse";
    private static final String username = "root";
    private static final String password = "Mysql2023!";
    private static Map<String, Sensor> types;       // key --> Sensor
    private static Timestamp lastTimestamp;
    private static Map<String,Integer> values;      // key --> value, in order to check only changed value wrt to the last read

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
		    sleep(10*1000);     //30 seconds
		    // read from DB
		    try (Connection connection = DriverManager.getConnection(url, username, password)) {
			
		        java.util.Date date = new java.util.Date();
		        Timestamp tempTimestamp = new java.sql.Timestamp(date.getTime());     // next time, I'll check if there will be command after this timestamp
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
		        for(String type : types.keySet()){      // for each kind of sensor
		            if(values.containsKey(type)){       // check if there's a new value for each type
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
