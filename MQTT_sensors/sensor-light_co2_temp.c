#include "contiki.h"
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

#define STATE_INIT    		    0
#define STATE_NET_OK    	    1
#define STATE_CONNECTING        2
#define STATE_CONNECTED         3
#define STATE_SUBSCRIBING1      4
#define STATE_SUBSCRIBED1       5
#define STATE_SUBSCRIBING2      6
#define STATE_SUBSCRIBED2       7
#define STATE_SUBSCRIBING3      8
#define STATE_SUBSCRIBED3       9
#define STATE_DISCONNECTED      10

/*---------------------------------------------------------------------------*/
/* GLOBAL VARIABLES*/
static struct mqtt_message *msg_ptr = 0;
static struct mqtt_connection conn;

// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer e_timer;

static mqtt_status_t status;
static char broker_address[CONFIG_IP_ADDR_STR_LEN];

static int value = 0;
static uint8_t sub_num = 0;

static uint8_t min_light_parameter;
static uint8_t max_light_parameter;
static uint8_t min_co2_parameter;
static uint8_t max_co2_parameter;
static uint8_t min_temp_parameter;
static uint8_t max_temp_parameter;

/*---------------------------------------------------------------------------*/
PROCESS(sensor_light_co2_temp, "MQTT sensor_light_co2_temp");
AUTOSTART_PROCESSES(&sensor_light_co2_temp);

static void pub_handler_light(const char *topic, uint16_t topic_len, const uint8_t *chunk, uint16_t chunk_len){
    sscanf((char *)chunk, "%hhd_%hhd", &min_light_parameter, &max_light_parameter);
    printf("Pub Handler: topic=LIGHT, min=%hhd max=%hhd\n", min_light_parameter, max_light_parameter);
}

static void pub_handler_co2(const char *topic, uint16_t topic_len, const uint8_t *chunk, uint16_t chunk_len){
    sscanf((char *)chunk, "%hhd_%hhd", &min_co2_parameter, &max_co2_parameter);
    printf("Pub Handler: topic=CO2, min=%hhd max=%hhd\n", min_co2_parameter, max_co2_parameter);
}

static void pub_handler_temp(const char *topic, uint16_t topic_len, const uint8_t *chunk, uint16_t chunk_len){
    sscanf((char *)chunk, "%hhd_%hhd", &min_temp_parameter, &max_temp_parameter);
    printf("Pub Handler: topic=TEMP, min=%hhd max=%hhd\n", min_temp_parameter, max_temp_parameter);
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
        process_poll(&sensor_light_co2_temp);
        break;
    }
    case MQTT_EVENT_PUBLISH: {      //triggered when a new msg is published
        msg_ptr = data;

        if(strcmp(msg_ptr->topic, "param/light") == 0)
            pub_handler_light(msg_ptr->topic, strlen(msg_ptr->topic),msg_ptr->payload_chunk, msg_ptr->payload_length);
        else if(strcmp(msg_ptr->topic, "param/co2") == 0)
            pub_handler_co2(msg_ptr->topic, strlen(msg_ptr->topic), msg_ptr->payload_chunk, msg_ptr->payload_length);
        else if(strcmp(msg_ptr->topic, "param/temp") == 0)
            pub_handler_temp(msg_ptr->topic, strlen(msg_ptr->topic), msg_ptr->payload_chunk, msg_ptr->payload_length);

        break;
    }
    case MQTT_EVENT_SUBACK: {
        #if MQTT_311
            mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

            if(suback_event->success) {
                printf("Application is subscribed to topic successfully\n");
                sub_num++;
            } else {
                printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
            }
        #else
            printf("Application is subscribed to topic successfully\n");
            sub_num++;
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

PROCESS_THREAD(sensor_light_co2_temp, ev, data){
    PROCESS_BEGIN();    
    printf("MQTT sensor_light_co2_temp started\n");

    // Initialize the ClientID as MAC address
    snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                        linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                        linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                        linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

    // Broker registration					 
    mqtt_register(&conn, &sensor_light_co2_temp, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);
         
    state=STATE_INIT;
                        
    // Initialize periodic timer to check the status 
    etimer_set(&e_timer, STATE_MACHINE_PERIODIC);

    /* Main loop */
    while(1) {

        PROCESS_YIELD();

        if((ev == PROCESS_EVENT_TIMER && data == &e_timer) || 
            ev == PROCESS_EVENT_POLL){
                            
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
                // topic: light
                strcpy(sub_topic,"param/light");
                status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);
                printf("Subscribing to param/light topic!\n");
                if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
                    LOG_ERR("Tried to subscribe to param/light topic but command queue was full!\n");
                    PROCESS_EXIT();
                }
                
                state = STATE_SUBSCRIBING1;
            }
            if(state == STATE_SUBSCRIBING1){
                if(sub_num == 1)
                    state = STATE_SUBSCRIBED1;
            }

            if(state == STATE_SUBSCRIBED1){
                //topic: co2
                strcpy(sub_topic,"param/co2");
                status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);
                printf("Subscribing to param/co2 topic!\n");
                if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
                    LOG_ERR("Tried to subscribe to param/co2 but command queue was full!\n");
                    PROCESS_EXIT();
                }

                state = STATE_SUBSCRIBING2;
            }
            if(state == STATE_SUBSCRIBING2){
                if(sub_num == 2)
                    state = STATE_SUBSCRIBED2;
            }

            if(state == STATE_SUBSCRIBED2){
                // topic: temp
                strcpy(sub_topic,"param/temp");
                status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);
                printf("Subscribing to param/temp topic!\n");
                if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
                    LOG_ERR("Tried to subscribe to param/temp topic but command queue was full!\n");
                    PROCESS_EXIT();
                } 

                state = STATE_SUBSCRIBING3;
            }
            if(state == STATE_SUBSCRIBING3){
                if(sub_num == 3)
                    state = STATE_SUBSCRIBED3;
            }
                
            if(state == STATE_SUBSCRIBED3){
                // Publish something
                sprintf(pub_topic, "%s", "sensor/light_co2_temp");
                
                sprintf(app_buffer, "report %d", value);    //### da togliere
                value++;                                    //### da togliere
                    
                mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
                strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
            
            } else if ( state == STATE_DISCONNECTED ){
            LOG_ERR("Disconnected from MQTT broker\n");	
            // Recover from error
            }
            
            etimer_set(&e_timer, STATE_MACHINE_PERIODIC);
        }

    }

    PROCESS_END();
}