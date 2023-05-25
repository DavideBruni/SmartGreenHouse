#ifndef JSON_UTIL_H
#define JSON_UTIL_H

char* findJsonField_String(const char* jsonString, const char* fieldName);
int findJsonField_Number(const char* jsonString, const char* fieldName);

#endif  // JSON_UTIL_H