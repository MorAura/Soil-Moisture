#include <Arduino.h>
#include "FS.h"
#include <LittleFS.h>

#define FORMAT_LITTLEFS_IF_FAILED true

// Status Led
#define LedPin 8

// Internal temp config
#include <driver/temperature_sensor.h>
temperature_sensor_handle_t temp_sensor = NULL;

#include "esp_sleep.h"
#define uS_TO_S_FACTOR 1000000ULL

const uint64_t SLEEP_DURATION_SEC = 3600;

/*
Connections:
  Moisture Sensor - A3
  Light Sensor - A2

  Need to finalize temperature measurement but use internal temp for testing
*/

// Determined experimentally
#define MoistureMax 3130
#define MoistureMin 1270

const char* filePath = "/data.csv";

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
  struct SoilData currentData = ReadData();

  if (!LittleFS.begin(FORMAT_LITTLEFS_IF_FAILED)) {
    Serial.println("LittleFS Mount Failed");
    return;
  }
  if (LittleFS.exists(filePath)) {
    Serial.println("File exists! Opening for reading...");
  } else {
    Serial.println("File does not exist! Creating new file...");
  }


  esp_sleep_enable_timer_wakeup(SLEEP_DURATION_SEC * uS_TO_S_FACTOR);
  Serial.println("Going to sleep now");
  Serial.flush();
  esp_deep_sleep_start();

}

void loop(){

}