#include "contiki.h"
#include "os/dev/button-hal.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"

#include <stdio.h>
#include <string.h>
#include <strings.h>

/*---------------------------------------------------------------------------*/
/* LOG settings */
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"
static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Defaukt config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64

/*---------------------------------------------------------------------------*/
/* Buffers for Client ID and Topics.*/
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];

/*---------------------------------------------------------------------------*/
/* The main MQTT buffers.*/
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];

/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    		  0
#define STATE_NET_OK    	  1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5

/*---------------------------------------------------------------------------*/
/* GLOBAL VARIABLES*/
static struct mqtt_message *msg_ptr = 0;
static struct mqtt_connection conn;

// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
#define WAIT_FOR_RECONNECTION 10
static struct etimer e_timer;
static struct etimer sleep_timer;
static struct ctimer sensing_timer;

static mqtt_status_t status;
static char broker_address[CONFIG_IP_ADDR_STR_LEN];

static int value = 15;
static uint8_t min_humidity_parameter = 10;
static uint8_t max_humidity_parameter = 30;
static bool alarm_state = false;

#define SENSE_PERIOD 		4		// seconds
#define SENSE_PERIOD_ON_ALERT 	2		// seconds
#define NUM_PERIOD_BEFORE_SEND  6 		// every 30 second there's one pub

static int num_period = 0;
static int is_first_pub_flag = 1;
button_hal_button_t *btn;
/*---------------------------------------------------------------------------*/
/* SENSING SIMULATION */
static void sense_callback(void *ptr){
	printf("I'm sensing - value %d", value);	
	value++;			//TODO change to generate fake values!

    if(value < min_humidity_parameter)
        alarm_state = true;
    else if(value > max_humidity_parameter)
        alarm_state = false;

    if(alarm_state){
	    printf("\t ALERT STATE\n");
    }
    else{
	    num_period++;
        printf("\n");
    }


	if(num_period == NUM_PERIOD_BEFORE_SEND || alarm_state){	
		if (state == STATE_SUBSCRIBED){	
			sprintf(pub_topic, "%s", "sensor/humidity");	
			sprintf(app_buffer, "%d", value); // copia il valore
			mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
			
		}
	}
	if(num_period >= NUM_PERIOD_BEFORE_SEND)
		num_period = 0;		//reset
    
    if(alarm_state)
        ctimer_set(&sensing_timer, SENSE_PERIOD_ON_ALERT * CLOCK_SECOND, sense_callback, NULL);
    else
        ctimer_set(&sensing_timer, SENSE_PERIOD * CLOCK_SECOND, sense_callback, NULL);
}


/*---------------------------------------------------------------------------*/
PROCESS(sensor_humidity, "MQTT sensor_humidity");
AUTOSTART_PROCESSES(&sensor_humidity);

static void pub_handler_humidity(const char *topic, uint16_t topic_len, const uint8_t *chunk, uint16_t chunk_len){
    sscanf((char *)chunk, "%hhd_%hhd", &min_humidity_parameter, &max_humidity_parameter);
    printf("Pub Handler: topic=HUMIDITY, min=%hhd max=%hhd\n", min_humidity_parameter, max_humidity_parameter);
}

static void mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data){
    switch(event) {
    case MQTT_EVENT_CONNECTED: {
        printf("Application has a MQTT connection\n");

        state = STATE_CONNECTED;
        break;
    }
    case MQTT_EVENT_DISCONNECTED: {
        printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

        state = STATE_DISCONNECTED;
        process_poll(&sensor_humidity);
        break;
    }
    case MQTT_EVENT_PUBLISH: {      //triggered when a new msg is published
        msg_ptr = data;

        if(strcmp(msg_ptr->topic, "param/humidity") == 0)
            pub_handler_humidity(msg_ptr->topic, strlen(msg_ptr->topic),msg_ptr->payload_chunk, msg_ptr->payload_length);

        break;
    }
    case MQTT_EVENT_SUBACK: {
        #if MQTT_311
            mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

            if(suback_event->success) {
            printf("Application is subscribed to topic successfully\n");
            } else {
            printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
            }
        #else
            printf("Application is subscribed to topic successfully\n");
        #endif
            break;
    }
    case MQTT_EVENT_UNSUBACK: {
        printf("Application is unsubscribed to topic successfully\n");
        break;
    }
    case MQTT_EVENT_PUBACK: {
        printf("Publishing complete.\n");
        break;
    }
    default:
        printf("Application got a unhandled MQTT event: %i\n", event);
        break;
    }
}

static bool have_connectivity(void){
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
        uip_ds6_defrt_choose() == NULL) {
        return false;
    }
    return true;
}

PROCESS_THREAD(sensor_humidity, ev, data){
    PROCESS_BEGIN();    
    printf("MQTT sensor_humidity started\n");

    // Initialize the ClientID as MAC address
    snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                        linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                        linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                        linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

    // Broker registration					 
    mqtt_register(&conn, &sensor_humidity, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);
    state=STATE_INIT;
                        
    //button initialization
    btn = button_hal_get_by_index(0);

    // Initialize periodic timer to check the status 
    etimer_set(&e_timer, STATE_MACHINE_PERIODIC);

    /* Main loop */
    while(1) {

        PROCESS_YIELD();

        if((ev == PROCESS_EVENT_TIMER && data == &e_timer) || 
            ev == PROCESS_EVENT_POLL || (ev == PROCESS_EVENT_TIMER && data == &sleep_timer)){
                            
            if(state==STATE_INIT){
                if(have_connectivity()==true)  
                    state = STATE_NET_OK;
            } 
            
            if(state == STATE_NET_OK){
                // Connect to MQTT broker
                printf("Connecting to MQTT broker!\n");
                
                memcpy(broker_address, broker_ip, strlen(broker_ip));
                
                mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
                            (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
                            MQTT_CLEAN_SESSION_ON);
                state = STATE_CONNECTING;
            }
            
            if(state==STATE_CONNECTED){
            
                // Subscribe to a topic
                strcpy(sub_topic,"param/humidity");

                status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

                printf("Subscribing to param/humidity topic!\n");
                if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
                    LOG_ERR("Tried to subscribe but command queue was full!\n");
                    PROCESS_EXIT();
                }
                
                state = STATE_SUBSCRIBED;
            }
                
            if(state == STATE_SUBSCRIBED){
                if(is_first_pub_flag == 1){
                    //publish on topic=sensor/humidity
                    ctimer_set(&sensing_timer, SENSE_PERIOD * CLOCK_SECOND, sense_callback, NULL);	
                    is_first_pub_flag = 0;	
                }
                // Publish something
               // sprintf(pub_topic, "%s", "sensor/humidity");
                
               // sprintf(app_buffer, "report %d", value);    //### da togliere
               // value++;                                    //### da togliere
                    
               // mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
               // strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
            
            } else if ( state == STATE_DISCONNECTED ){
            	LOG_ERR("Disconnected from MQTT broker\n");	
            	// Recover from error: try to reconnect after WAIT_FOR_RECONNECTION seconds
                state = STATE_INIT;
                etimer_set(&sleep_timer, WAIT_FOR_RECONNECTION * CLOCK_SECOND);
                continue;
            }
            
            etimer_set(&e_timer, STATE_MACHINE_PERIODIC);
        }
        else if(ev == button_hal_press_event){
            value = min_humidity_parameter - 3;
            //sense_callback(NULL);
        }

    }

    PROCESS_END();
}
