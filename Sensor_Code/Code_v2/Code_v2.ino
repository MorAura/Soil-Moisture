#include <Arduino.h>
#include "FS.h"
#include <LittleFS.h>
#include <WiFi.h>
#include "time.h"
#include "esp_sntp.h"

const char *ssid = "iPhone";
const char *password = "iphone123";

const char *ntpServer1 = "pool.ntp.org";
const char *ntpServer2 = "time.nist.gov";
const long gmtOffset_sec = 19800;
const int daylightOffset_sec = 0;

int SyncHour = 0;

bool newTimeSynced = false;

#define FORMAT_LITTLEFS_IF_FAILED true

// Status Led
#define LedPin 8

// Internal temp config
#include <driver/temperature_sensor.h>
temperature_sensor_handle_t temp_sensor = NULL;

#include "esp_sleep.h"
#define uS_TO_S_FACTOR 1000000ULL

// typically 3600
const uint64_t SLEEP_DURATION_SEC = 10;

/*
Connections:
  Moisture Sensor - A3
  Light Sensor - A2

  Need to finalize temperature measurement but use internal temp for testing
*/

// Determined experimentally
#define MoistureMax 3130
#define MoistureMin 1270

const char* dataFilePath = "/data.csv";


struct SoilData {
  float tempVal;
  int moistureLevel;
  int lightLevel;
};

struct SoilData ReadData(){

  int moistureRead = 0;
  int lightRead = 0;
  float tempRead = 0;

  for(int i = 0;i < 10;i++){
    moistureRead += analogRead(A3);
    lightRead += analogRead(A2);
    float tempData = 0;
    ESP_ERROR_CHECK(temperature_sensor_get_celsius(temp_sensor, &tempData));
    tempRead += tempData;
  }

  moistureRead = moistureRead/10;
  lightRead = lightRead/10;
  tempRead = tempRead/10;
  
  struct SoilData newData;

  newData.moistureLevel = map(moistureRead,MoistureMin,MoistureMax,100,0);
  newData.lightLevel = map(lightRead,0,4095,0,100);
  newData.tempVal = tempRead;

  Serial.print("Moist : ");
  Serial.print(newData.moistureLevel);
  Serial.print(" | Light : ");
  Serial.print(newData.lightLevel);
  Serial.print(" | Temp : ");
  Serial.println(newData.tempVal);
  
  return newData;
}

void readFile(fs::FS &fs, const char * path){
  Serial.printf("Reading file: %s\r\n", path);

  File file = fs.open(path);
  if(!file || file.isDirectory()){
    Serial.println("- failed to open file for reading");
    return;
  }

  Serial.println("- read from file:");
  while(file.available()){
    Serial.write(file.read());
  }
  file.close();
}

void writeFile(fs::FS &fs, const char *path, const char *message) {
  Serial.printf("Writing file: %s\r\n", path);

  File file = fs.open(path, FILE_WRITE);
  if (!file) {
    Serial.println("- failed to open file for writing");
    return;
  }
  if (file.print(message)) {
    Serial.println("- file written");
  } else {
    Serial.println("- write failed");
  }
  file.close();
}

void appendFile(fs::FS &fs, const char *path, const char *message) {
  Serial.printf("Appending to file: %s\r\n", path);

  File file = fs.open(path, FILE_APPEND);
  if (!file) {
    Serial.println("- failed to open file for appending");
    return;
  }
  if (file.print(message)) {
    Serial.println("- message appended");
  } else {
    Serial.println("- append failed");
  }
  file.close();
}

void timeavailable(struct timeval *t) {
  // Callback function (gets called when time adjusts via NTP)
  Serial.println("Got time adjustment from NTP!");
  newTimeSynced = true;
}

void SyncTime(){
  
  Serial.printf("Connecting to %s ", ssid);
  WiFi.begin(ssid, password);
  esp_sntp_servermode_dhcp(1);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println(" CONNECTED");

  sntp_set_time_sync_notification_cb(timeavailable);
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer1, ntpServer2);

  delay(1000);

  newTimeSynced = false;
  while(!newTimeSynced){
    delay(100); // wait for time sync
  }

  // Code for connecting to online sever and syncing server data.

}

void setup(){

  Serial.begin(115200);
  while (!Serial) {
    ;  // wait for serial port to connect. 
  }
  Serial.println("Starting up");
  

  // Setup adc pins for moisture and light level as inputs
  pinMode(A3,INPUT);
  pinMode(A2,INPUT);

  // Setup onboard led as output
  pinMode(LedPin,OUTPUT);
  digitalWrite(LedPin,HIGH);
  delay(1000);
  digitalWrite(LedPin,LOW);

  // Setup internal temperature sensor
  temperature_sensor_config_t temp_sensor_config = TEMPERATURE_SENSOR_CONFIG_DEFAULT(-10, 80);
  ESP_ERROR_CHECK(temperature_sensor_install(&temp_sensor_config, &temp_sensor));
  ESP_ERROR_CHECK(temperature_sensor_enable(temp_sensor));

  // Read data from sensors
  Serial.println("Reading sensor data");
  struct SoilData currentData = ReadData();

  if (!LittleFS.begin(FORMAT_LITTLEFS_IF_FAILED)) {
    Serial.println("LittleFS Mount Failed");
    return;
  }

  if (LittleFS.exists(dataFilePath)) {
    Serial.println("File exists! Opening for reading...");
  } else {
    Serial.println("File does not exist! Creating new file...");
    writeFile(LittleFS, dataFilePath, "YYYY,MM,DD,HH,Moisture,Light,Temperature\r\n");
  }

  struct tm timeinfo;

  if (!getLocalTime(&timeinfo)) {
    Serial.println("No time available (yet)");
    SyncTime();
  }

  if (!getLocalTime(&timeinfo)) {
    Serial.println("No time available (yet)");
    SyncTime();
  }


  int year   = timeinfo.tm_year + 1900;
  int month  = timeinfo.tm_mon + 1;
  int day    = timeinfo.tm_mday;
  int hour   = timeinfo.tm_hour;

  char message[128];

  snprintf(message, sizeof(message), "%04d,%02d,%02d,%02d,%d,%d,%.2f\r\n",
           year, month, day, hour,
           currentData.moistureLevel,
           currentData.lightLevel,
           currentData.tempVal);

  appendFile(LittleFS, dataFilePath, message);
  readFile(LittleFS, dataFilePath);

  esp_sleep_enable_timer_wakeup(SLEEP_DURATION_SEC * uS_TO_S_FACTOR);
  Serial.println("Going to sleep now");
  Serial.flush();
  esp_deep_sleep_start();

}

void loop(){

}