# GreenHouse+ (It was named SmartGreenHouse at the origin!)
It's project deployed for the IoT course at the University of Pisa Artificial Intelligence & Data Engineering Master's Degree. <br>
<b> Greenhouse+ </b> is an IoT system for the automation of frequent procedures in a smart greenhouse. In this project, we leverage smart sensors to monitor and optimize four key factors within the greenhouse: light intensity, temperature, soil moisture, and CO2 levels. Through the use of advanced sensor technology our goal is to enhance the efficiency, productivity, and sustainability of greenhouse cultivation. <br>
<b> Note: all our sensors and the action of the actuators are simulated, we're mainly interested in the communication part between an MQTT network, a CoAP network and a Cloud System </b>
## Network architecture
![network architecture](https://github.com/DavideBruni/SmartGreenHouse/blob/master/network_architecture.jpg?raw=true)
<br>
Note for the project deployment: The MQTT broker (Mosquitto in our scenario), the Remote Control Application and the Cloud Application are hosted on the VM provided by the teachers. If you want to deploy them in a difference place, you need to change the Broker Address in the following files:
<ul>
  <li> MQTT_sensors/sensor-humidity.c</li>
  <li> MQTT_sensors/sensor-light_co2_temp.c</li>
  <li> RemotControlApplication/src/main/greenHouse/unipi/it/threads/CLIThread.java</li>
</ul>

## RPL Border-router deployments
To deploy the RPL border router, we use the <a href="https://github.com/contiki-ng/contiki-ng/tree/develop/examples/rpl-border-router"> code </a> provide in the examples of Contiki-NG Operating systems.

## Deployment on Contiki-NG
The makefiles are written in such a way that the entire SmartGreenHouse directory is placed inside the 'examples' folder in contiki-ng os.
If you want to place the folder in a different location, be sure to change the relative path to Contiki inside the Makefiles.

## Remote control application & CoAP Registration server
They are both Maven Project developped in JAVA. In both directory the jar file is: `target/iot.unipi.it-0.0.1-SNAPSHOT.jar` and you can run each of them by executing `java -jar path/to/the/jar/file`.

## MQTT Collector
It's developped in python. You need to install some libraries reported on `MQTT_Collector/requirements.txt`

## Details
All the details about the logic and the designed choice are reported inside the `Documentation.pdf` file.

## License
Greenhouse+ is released under the MIT License. Feel free to use, modify, and distribute the code for academic, personal, or commercial purposes.
