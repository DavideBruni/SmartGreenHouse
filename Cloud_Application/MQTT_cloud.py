import paho.mqtt.client as mqtt
from threading import Thread
import mysql.connector

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

def on_connect_light(client, userdata, flags, rc):
	print("Connected with result code " + str(rc))
	client.subscribe("sensor/light")

def on_connect_co2(client, userdata, flags, rc):
	print("Connected with result code " + str(rc))
	client.subscribe("sensor/co2")

def on_connect_temp(client, userdata, flags, rc):
	print("Connected with result code " + str(rc))
	client.subscribe("sensor/temp")
# ------------------------------------------------------------------------------------

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
	print(msg.topic+" "+str(msg.payload))

	sensed_type = msg.topic[7:]		#remove sensor/

	val = (float(msg.payload), sensed_type)
	mycursor.execute(sql, val)

	mydb.commit()


on_connect_callbacks = { 
	"humidity" : on_connect_humidity,
	"light": on_connect_light,
	"co2": on_connect_co2,
	"temp": on_connect_temp
}


def mqtt_client(topic):
	
	client = mqtt.Client()
	client.on_connect = on_connect_callbacks[topic]
	client.on_message = on_message
	client.connect("127.0.0.1", 1883, 60)
	
	client.loop_forever()


def main():
	thread_humidity = Thread(target=mqtt_client, args=("humidity",))
	thread_light = Thread(target=mqtt_client, args=("light",))
	thread_co2 = Thread(target=mqtt_client, args=("co2",))
	thread_temp = Thread(target=mqtt_client, args=("temp",))
	
	# starting threads
	thread_humidity.start()
	thread_light.start()
	thread_co2.start()
	thread_temp.start()	

if __name__ == '__main__':
	main()
