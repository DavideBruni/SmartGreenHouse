#include "json_util.h"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdint.h>

char* findJsonField_String(const char* jsonString, const char* fieldName) {
    const char* start = jsonString;
    char* value = NULL;

    while (*start != '\0') {
        if (*start == '\"') {
            start++;
            const char* end = strchr(start, '\"');
            if (end != NULL) {
                size_t fieldLength = end - start;
                char* field = malloc(fieldLength + 1);
                strncpy(field, start, fieldLength);
                field[fieldLength] = '\0';

                if (strcmp(field, fieldName) == 0) {
                    start = strchr(end + 1, '\"');
                    if (start != NULL) {
                        start++;
                        const char* valueStart = start;
                        while (*start != '\"' && *start != '\0') {
                            start++;
                        }

                        size_t valueLength = start - valueStart;
                        value = malloc(valueLength + 1);
                        strncpy(value, valueStart, valueLength);
                        value[valueLength] = '\0';
                    }
                }

                free(field);
            }
        }

        start++;
    }

    return value;
}

int findJsonField_Number(const char* jsonString, const char* fieldName) {
    const char* start = jsonString;
    int number = 0;

    while (*start != '\0') {
        if (*start == '\"') {
            start++;
            const char* end = strchr(start, '\"');
            if (end != NULL) {
                size_t fieldLength = end - start;
                char* field = malloc(fieldLength + 1);
                strncpy(field, start, fieldLength);
                field[fieldLength] = '\0';

                if (strcmp(field, fieldName) == 0) {
                    start = strchr(end + 1, ':');
                    if (start != NULL) {
                        start++;
                        while (*start == ' ' || *start == '\t') {
                            start++;
                        }

                        char* endPtr;
                        number = strtol(start, &endPtr, 10);
                        if (endPtr == start) {
                            number = 0;  // Conversion failed
                        }
                    }
                }

                free(field);
            }
        }

        start++;
    }

    return number;
}