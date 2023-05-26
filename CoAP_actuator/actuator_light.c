#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "sys/etimer.h"
#include "os/dev/leds.h"
#include <stdio.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

// Server IP and resource path
#define SERVER_EP "coap://[fd00::1]:5683"
#define NODE_NAME_JSON "{\"name\":\"actuator_light\",\"status\":\"off\"}"
#define MAX_REGISTRATION_RETRY 3

static coap_endpoint_t server_ep;
static coap_message_t request[1]; /* This way  the packet can be treated as pointer as usual. */
static char *service_registration_url = "/registration";
static int max_registration_retry = MAX_REGISTRATION_RETRY;

void client_chunk_handler(coap_message_t *response){
	if(response == NULL) {
		LOG_ERR("Request timed out\n");
	}else if(response->code != 65){
		LOG_ERR("Error: %d\n",response->code);	
	}else{
		LOG_INFO("Registration successful\n");
		max_registration_retry = 0;		// if = 0 --> registration ok!
		return;
	}
	
	// If I'm at this point, there was some problem in the registration phasse, so we decide to try again until max_registration_retry != 0
	max_registration_retry--;
	if(max_registration_retry==0)
		max_registration_retry=-1;
}



extern coap_resource_t res_light;
static struct etimer sleep_timer;

PROCESS(light_thread, "light");
AUTOSTART_PROCESSES(&light_thread);

PROCESS_THREAD(light_thread, ev, data){
	
	PROCESS_BEGIN();

	while(max_registration_retry!=0){
		/* -------------- REGISTRATION --------------*/
		// Populate the coap_endpoint_t data structure
		coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
		// Prepare the message
		coap_init_message(request, COAP_TYPE_CON,COAP_POST, 0);
		coap_set_header_uri_path(request, service_registration_url);
		//Set payload
		coap_set_payload(request, (uint8_t *)NODE_NAME_JSON, sizeof(NODE_NAME_JSON) - 1);
	
		COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
    
		/* -------------- END REGISTRATION --------------*/
		if(max_registration_retry == -1){		// something goes wrong more MAX_REGISTRATION_RETRY times, node goes to sleep then try again
			etimer_set(&sleep_timer, 3000*CLOCK_SECOND);
			PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&sleep_timer));
			max_registration_retry = MAX_REGISTRATION_RETRY;
		}
	}
    
    
	coap_activate_resource(&res_light, "actuator_light");
	
	PROCESS_YIELD();	
	

PROCESS_END();
}
