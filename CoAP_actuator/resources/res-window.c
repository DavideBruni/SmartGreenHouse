#include "contiki.h"
#include "coap-engine.h"
#include "os/dev/leds.h"
#include "json_util.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

static uint8_t window_status = 0;       // 0 closed, 1 opened

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_window,
         "title=\"SmartGreenHouse: ?acutaor_window=0..\" POST/PUTaction=<action>\";rt=\"Control\";if=\"actuator\"",
         NULL,
         NULL,
         res_put_handler,
         NULL);

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    int len = 0;
    char* action = NULL;
    const uint8_t *chunk;
	
    
    len = coap_get_payload(request,&chunk);
	
    if(len>0){
        action = findJsonField_String((char *)chunk, "action");
        LOG_INFO("received command: action=%s\n", action);
	}  
    
    if(action!=NULL && strlen(action)!=0){
        if((strncmp(action, "open", len) == 0) && window_status==0){
            leds_set(LEDS_GREEN);
	        window_status = 1;
		    coap_set_status_code(response, CHANGED_2_04);
	    }
        else if((strncmp(action, "close", len) == 0) && window_status==1){
            leds_set(LEDS_RED);
	        window_status = 0;
		    coap_set_status_code(response, CHANGED_2_04);
	    }
        else
            coap_set_status_code(response, BAD_OPTION_4_02);
    }
    else{
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }

    free(action);
}
