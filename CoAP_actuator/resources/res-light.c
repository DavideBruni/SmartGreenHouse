#include "contiki.h"
#include "coap-engine.h"
#include "os/dev/leds.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static uint8_t light_status = 0; // 0 off, 1 medium brightness, 2 max brightness

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_light,
         "title=\"SmartGreenHouse: ?acutaor_light=0..\" POST/PUTaction=<action>\";rt=\"Control\";if=\"actuator\"",
         NULL,
         NULL,
         res_put_handler,
         NULL);

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    int len = 0;
    char action[50];
    const uint8_t *chunk;
	
    
    len = coap_get_payload(request,&chunk);
	printf("Chunk: %s\n",(char *)chunk);
	
    if(len>0){
	sscanf((char *)chunk,"{\"action\":\"%[^\"]\"}",action);
	}
     if(action!=NULL && strlen(action)!=0){
        if((strncmp(action, "up", len) == 0)){
            if(light_status == 0){
                leds_set(LEDS_YELLOW);
                light_status++;
            }else if(light_status == 1){
                leds_set(LEDS_GREEN);               // send respponse back ?
                light_status++;
            }else{
                printf("Light has max brightness"); // send respponse back ?
            }
	    }else if((strncmp(action, "down", len) == 0)){
            if(light_status == 2){
                leds_set(LEDS_YELLOW);
                light_status--;
            }else if(light_status == 1){
                leds_off(LEDS_YELLOW);               // send respponse back ?
                light_status--;
            }else{
                printf("Light is turned off "); // send respponse back ?
            }
	    }else if((strncmp(action, "off", len) == 0)){
            light_status = 0;
            leds_off(LEDS_NUM_TO_MASK(LEDS_GREEN) | LEDS_NUM_TO_MASK(LEDS_RED) | LEDS_NUM_TO_MASK(LEDS_YELLOW)); 
	    }else
            coap_set_status_code(response, BAD_OPTION_4_02);
    }else{
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
