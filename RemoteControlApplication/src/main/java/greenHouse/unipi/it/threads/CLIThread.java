package greenHouse.unipi.it.threads;

import greenHouse.unipi.it.DAO.ResourceDAO;
import greenHouse.unipi.it.model.Co2Sensor;
import greenHouse.unipi.it.model.HumiditySensor;
import greenHouse.unipi.it.model.LightSensor;
import greenHouse.unipi.it.model.TempSensor;
import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CLIThread extends Thread{

    private static final String broker = "tcp://127.0.0.1:1883";
    private static final String clientId = "RemoteControlApp";
    private static Map<String, Boolean> is_changed = new HashMap<>();

    @Override
    public void run() {
        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            System.out.println("----- Main menu -----");
            System.out.println("Type \"\\help\" to show all the available commands");
            print_help();
            String command;
            // Reading data using readLine
            try {
                command = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch(command){
                case "\\help":
                    print_help();
                    break;
                case "\\action":
                    System.out.println("What do you want to do?");
                    System.out.println("\\window_open --> to open window");
                    System.out.println("\\window_close --> to close window");
                    System.out.println("\\sprinkler_on --> to active sprinkler");
                    System.out.println("\\sprinkler_off --> to deactivate sprinkler");
                    System.out.println("\\light_up --> to increase light brightness");
                    System.out.println("\\light_down --> to decrease light brightness");
                    try {
                        command = reader.readLine();
                        active_actuator(command);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "\\change_params":

                    while(true) {
                        System.out.println("What params do you want to change? Press 0 to exit");
                        System.out.println("\\min_light_parameter --> change the minimum value of the brightness, insert an integer");
                        System.out.println("\\max_light_parameter --> change the maximum value of the brightness, insert an integer");
                        System.out.println("\\light_hours --> change the intervall in which there will be light on, insert  \"starting-hour_end-hour\"");
                        System.out.println("\\min_co2_parameter --> change the minimum value of the co2 inside the GreenHouse, insert an integer");
                        System.out.println("\\min_temp_parameter --> change the minimum value of the temperature inside the GreenHouse, insert an integer");
                        System.out.println("\\max_temp_parameter --> change the maximum value of the temperature inside the GreenHouse, insert an integer");
                        System.out.println("\\min_humidity_parameter --> change the minimum value of the field humidity, insert an integer");
                        System.out.println("\\max_humidity_parameter --> change the maximum value of the field humiidty, insert an integer");
                        try {
                            command = reader.readLine();
                            if(command.equals("0")){
                               break;
                            }
                            String value = reader.readLine();
                            setParameters(command,value);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        MqttClient client = new MqttClient(broker, clientId);
                        client.connect();
                        String content;
                        for (String topic : is_changed.keySet()) {
                        send_mqtt(client, "param/" + topic);
                        }

                        client.disconnect();

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    break;
                case "\\set_day_mode":
                case "\\set_night_mode":
                    if(command.equals("\\light_on")){
                        LightSensor.getInstance().setNight(false);
                    }else{
                        LightSensor.getInstance().setNight(true);
			String command_value = "off";
		        ResourceDAO resourceDAO = ResourceDAO.retrieveInformation("light");
		        if(resourceDAO.getStatus().equals(command_value)) {
		            //System.out.println("Light already off");
		            break;
		        }
			new CoapClientThread(resourceDAO, command_value).start();
                    }
                    try {
                        MqttClient client = new MqttClient(broker, clientId);
                        client.connect();
                        send_mqtt(client, "param/light");
                        client.disconnect();

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    break;
                case "\\show_actuators_status":
                    ResourceDAO light_act = ResourceDAO.retrieveInformation("light");
                    ResourceDAO window_act = ResourceDAO.retrieveInformation("window");
                    ResourceDAO sprinkler_act = ResourceDAO.retrieveInformation("sprinkler");

                    System.out.println(light_act);
                    System.out.println(window_act);
                    System.out.println(sprinkler_act);
                    break;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    private void active_actuator(String command){
        String command_value;
        ResourceDAO resourceDAO;
        switch (command){
            case "\\window_open":
                command_value = "open";
                resourceDAO = ResourceDAO.retrieveInformation("window");
                if(resourceDAO.getStatus().equals(command_value)) {
                    System.out.println("Window already open");
                    return;
                }
                break;
            case "\\window_close":
                command_value = "close";
                resourceDAO = ResourceDAO.retrieveInformation("window");
                if(resourceDAO.getStatus().equals(command_value)) {
                    System.out.println("Window already closed");
                    return;
                }
                break;
            case "\\sprinkler_on":
                command_value = "on";
                resourceDAO = ResourceDAO.retrieveInformation("sprinkler");
                if(resourceDAO.getStatus().equals(command_value)) {
                    System.out.println("Sprinkler already on");
                    return;
                }
                break;
            case "\\sprinkler_off":
                command_value = "off";
                resourceDAO = ResourceDAO.retrieveInformation("sprinkler");
                if(resourceDAO.getStatus().equals(command_value)) {
                    System.out.println("Sprinkler already off");
                    return;
                }
                break;
            case "\\light_up":
		if (LightSensor.getInstance().getIsNight()==0){
                	command_value = "up";
                	resourceDAO = ResourceDAO.retrieveInformation("light");
		}else{
			System.out.println("You must put light on before");
			return;		
		}
                break;
            case "\\light_down":
                command_value = "down";
                resourceDAO = ResourceDAO.retrieveInformation("light");
                break;
            default:
                System.out.println("Invalid command");
                return;
        }
        new CoapClientThread(resourceDAO, command_value).start();

    }

    private void setParameters(String command, String value) {

        switch (command) {
            case "\\min_light_parameter":
                LightSensor.getInstance().setMin(Integer.parseInt(value));
                is_changed.put("light", true);
                break;
            case "\\max_light_parameter":
                LightSensor.getInstance().setMax(Integer.parseInt(value));
                is_changed.put("light", true);
                break;
            case "\\min_co2_parameter":
                Co2Sensor.getInstance().setMin(Integer.parseInt(value));
                is_changed.put("co2", true);
                break;
            case "\\min_temp_parameter":
                TempSensor.getInstance().setMin(Integer.parseInt(value));
                is_changed.put("temp", true);
                break;
            case "\\max_temp_parameter":
                TempSensor.getInstance().setMax(Integer.parseInt(value));
                is_changed.put("temp", true);
                break;
            case "\\min_humidity_parameter":
                HumiditySensor.getInstance().setMin(Integer.parseInt(value));
                is_changed.put("humidity", true);
                break;
            case "\\max_humidity_parameter":
                HumiditySensor.getInstance().setMax(Integer.parseInt(value));
                is_changed.put("humidity", true);
                break;
            default:
                System.out.println("Invalid command");
        }

    }



    private void send_mqtt(MqttClient client, String topic) throws MqttException {
        JSONObject jsonObject = new JSONObject();
        switch(topic) {
            case "param/humidity":
                jsonObject.put("min_humidity_parameter",HumiditySensor.getInstance().getMin());
                jsonObject.put("max_humidity_parameter",HumiditySensor.getInstance().getMax());
                break;
            case "param/co2":
                jsonObject.put("min_co2_parameter",Co2Sensor.getInstance().getMin());
                break;
            case "param/temp":
                jsonObject.put("min_temp_parameter",TempSensor.getInstance().getMin());
                jsonObject.put("max_temp_parameter",TempSensor.getInstance().getMax());
                break;
            case "param/light":
                jsonObject.put("min_light_parameter",LightSensor.getInstance().getMin());
                jsonObject.put("max_light_parameter",LightSensor.getInstance().getMax());
                jsonObject.put("is_night",LightSensor.getInstance().getIsNight());
                break;
            default:
                System.err.println("Internal error: invalid topic");
                return;
        }
        MqttMessage message = new MqttMessage(jsonObject.toJSONString().getBytes());
        client.publish(topic, message);

    }


    private void print_help(){

        System.out.println("Available commands: \n\\action --> open the menu to send a command to an actuator");
        System.out.println("\\change_params --> open the menu to change sensors parameters");
        System.out.println("\\set_day_mode --> the light sensor will report anomaly, it's day!");
        System.out.println("\\set_night_mode --> the light sensor will not report anomaly, it's night!");
        System.out.println("\\show_actuators_status --> show the current status of the available actuators");
    }
}
