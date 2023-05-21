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

static uint8_t window_status = 0; // 0 closed, 1 opened

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_leds,
         "",
         NULL,
         NULL,
         res_put_handler,
         NULL);

static void
res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    size_t len = 0;
    const char *action = NULL;

    ///
    //coap_packet_t *coap_req = (coap_packet_t *)request;
    //
    //if (COAP_IS_REQUEST_PUT(coap_req)) {
    //    // Access the payload
    //    uint8_t *payload = NULL;
    //    uint16_t payload_len = 0;
    //    coap_get_payload(coap_req, &payload, &payload_len);
    //    
    //    // Process the payload
    //    // ...
    //}
    /////
    
    if((len = coap_get_post_variable(request, "action", &action))) {
        if((strncmp(action, "open", len) == 0) && window_status==0)
            leds_set(LEDS_GREEN);
        else if((strncmp(action, "close", len) == 0) && window_status==1)
            leds_set(LEDS_RED);
        else
            coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
