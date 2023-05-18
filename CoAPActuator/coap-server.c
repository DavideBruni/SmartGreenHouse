#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

// Server IP and resource path
#define SERVER_EP "coap://[fd00::1]:5683"
char *service_registration_url = "/registration";

void client_chunk_handler(coap_message_t *response){
	const uint8_t *chunk;
	if(response == NULL) {
		print("Request timed out\n");
	}else if(response->code != CREATED){
		print("Error: %d\n",response->code);
	}else{
		print("OK\n",response->code);
	}
}

extern coap_resource_t res_obs;

PROCESS(window_thread, "window");
AUTOSTART_PROCESSES(&window_thread);

PROCESS_THREAD(window_thread, ev, data){
	static coap_endpoint_t server_ep;
	static coap_message_t request[1]; /* This way  the packet can be treated as pointer as usual. */
	PROCESS_BEGIN();

	// Populate the coap_endpoint_t data structure
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
	
	while(1){
		PROCESS_YIELD();

		// Prepare the message
		coap_init_message(request, COAP_TYPE_CON,COAP_POST, 0);
		coap_set_header_uri_path(request, service_url);
		
		// Set the payload (if needed)
		const char msg[] = "{\"ipAddress\":\"fd00:1\"}";
		coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);
	
		printf("--Done--\n");
		COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
		
	}

PROCESS_END();
}
