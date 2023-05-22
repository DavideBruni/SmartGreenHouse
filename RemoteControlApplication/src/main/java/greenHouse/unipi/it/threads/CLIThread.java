package greenHouse.unipi.it.threads;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLIThread extends Thread{
    @Override
    public void run() {
        // Enter data using BufferReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            System.out.println("----- Main menu -----");
            String command = null;
            // Reading data using readLine
            try {
                command = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switch(command){
                case "\\help":
                    break;
                case "\\sense":
                    break;
                case "\\action":
                    System.out.println("What do you want to do?");
                    System.out.println("\\window_open --> to open window");
                    System.out.println("\\window_close --> to close window");
                    System.out.println("\\sprinkler_active --> to active sprinkler");
                    System.out.println("\\sprinkler_deactivate --> to deactivate sprinkler");
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
                    break;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    void active_actuator(String command){

        String ip = "[fd00::202:2:2:2]";
        String resource = "actuator_window";
        String command_value;

        switch (command){
            // per ogni commando recuperare l'ip della risorsa
            // e il name della risorsa
            case "\\window_open":
                command_value = "open";
                break;
            case "\\window_close":
                command_value = "close";
                break;
            case "\\sprinkler_active":
                command_value = "on";
                break;
            case "\\sprinkler_deactivate":
                command_value = "off";
                break;
            case "\\light_up":
                command_value = "up";
                break;
            case "\\light_down":
                command_value = "down";
                break;
            default:
                System.out.println("Invalid command");
                return;
        }
        new CoapClientThread(ip,resource,command_value).start();

    }
}
