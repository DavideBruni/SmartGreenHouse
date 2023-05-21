import paho.mqtt.client as mqtt
from threading import Thread

# The callbacks for when the client receives a CONNACK response from the server.
def on_connect_humidity(client, userdata, flags, rc):
	print("Connected with result code " + str(rc))
	client.subscribe("humidity")

def on_connect_ligt_co2_temp(client, userdata, flags, rc):
	print("Connected with result code " + str(rc))
	client.subscribe("light_co2_temp")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
	print(msg.topic+" "+str(msg.payload))

on_connect_callbacks = { 
	"humidity" : on_connect_humidity,
	"light_co2_temp": on_connect_ligt_co2_temp
}


def mqtt_client(topic):
	
	client = mqtt.Client()
	client.on_connect = on_connect_callbacks[topic]
	client.on_message = on_message
	client.connect("127.0.0.1", 1883, 60)
	
	client.loop_forever()


def main():
	thread_humidity = Thread(target=mqtt_client, args=("humidity",))
	thread_light_co2_temp = Thread(target=mqtt_client, args=("light_co2_temp",))
	thread_humidity.start()
	thread_light_co2_temp.start()	

if __name__ == '__main__':
	main()
