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

static uint8_t sprinkler_status = 0; // 0 off, 1 on

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_window,
         "title=\"SmartGreenHouse: ?acutaor_sprinkler=0..\" POST/PUTaction=<action>\";rt=\"Control\";if=\"actuator\"",
         NULL,
         NULL,
         res_put_handler,
         NULL);

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    size_t len = 0;
    const char *action = NULL;

    printf("Sono qui!");
    if((len = coap_get_post_variable(request, "action", &action))) {
        if((strncmp(action, "on", len) == 0) && sprinkler_status == 0){
            leds_set(LEDS_GREEN);
            sprinkler_status = 1;
	    }else if((strncmp(action, "off", len) == 0) && sprinkler_status == 1){
            leds_set(LEDS_RED);
            sprinkler_status = 0;
	    }else
            coap_set_status_code(response, BAD_OPTION_4_02);
    }else{
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
