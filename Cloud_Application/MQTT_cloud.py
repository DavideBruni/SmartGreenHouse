import paho.mqtt.client as mqtt
from threading import Thread
import mysql.connector
import json

mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  password="Mysql2023!",
  database="SmartGreenHouse"
)

mycursor = mydb.cursor()

sql = "INSERT INTO dataSensed (value, type) VALUES (%s, %s)"
# ------------------------------------------------------------------------------------
# The callbacks for when the client receives a CONNACK response from the server.
def on_connect_humidity(client, userdata, flags, rc):
	print("Connected with result code " + str(rc))
	client.subscribe("sensor/humidity")

def on_connect_co2_light_temp(client, userdata, flags, rc):
	print("Connected with result code " + str(rc))
	client.subscribe("sensor/co2_light_temp")

# ------------------------------------------------------------------------------------

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
	print(msg.topic+" "+str(msg.payload.decode("utf-8","ignore")))
	json_payload = json.loads(str(msg.payload.decode("utf-8","ignore")))
	
	sensed_type = msg.topic[7:]		#remove sensor/

	for key in json_payload:
		val = (int(json_payload[key]), sensed_type)
		mycursor.execute(sql, val)

		mydb.commit()


on_connect_callbacks = { 
	"humidity" : on_connect_humidity,
	"co2_light_temp": on_connect_co2_light_temp
}


def mqtt_client(topic):
	
	client = mqtt.Client()
	client.on_connect = on_connect_callbacks[topic]
	client.on_message = on_message
	client.connect("127.0.0.1", 1883, 60)
	
	client.loop_forever()


def main():
	thread_humidity = Thread(target=mqtt_client, args=("humidity",))
	thread_co2_light_temp = Thread(target=mqtt_client, args=("co2_light_temp",))
	
	
	# starting threads
	thread_humidity.start()
	thread_co2_light_temp.start()	

if __name__ == '__main__':
	main()
