// Status Led (Alerts)
#define LedPin 8

// Internal temp config
#include <driver/temperature_sensor.h>
temperature_sensor_handle_t temp_sensor = NULL;

#include "esp_sleep.h"
#define uS_TO_S_FACTOR 1000000ULL   // Conversion factor for micro seconds to seconds
#define SLEEP_DURATION_SEC  60

#include "driver/gpio.h"
#define WAKEUP_GPIO_PIN GPIO_NUM_3



/*
Connections:
  Moisture Sensor - A3
  Light Sensor - A2

  Need to finalize temperature measurement but use internal temp for testing
*/

// Determined experimentally
#define MoistureMax 3130
#define MoistureMin 1270

// To store old data
RTC_DATA_ATTR uint8_t moistureHist[168] = {0};
RTC_DATA_ATTR uint8_t lightHist[168] = {0};
RTC_DATA_ATTR uint8_t tempHist[168] = {0};

// Configured Alert Values
uint8_t moistHigh = 100;
uint8_t moistLow = 0;
uint8_t tempHigh = 100;
uint8_t tempLow = 0;

bool moistLowAlert = false;
bool moistHighAlert = false;
bool tempLowAlert = false;
bool tempHighAlert = false;

bool deviceConnected = false;
bool oldDeviceConnected = false;


// Function to read new data, calculate values and add save it to memory. (Trigger alert if necessary).
void readData(){

  int moistureRead = analogRead(A3);
  int lightRead = analogRead(A2);
  float tempRead = 0;
  ESP_ERROR_CHECK(temperature_sensor_get_celsius(temp_sensor, &tempRead));
  

  uint8_t moistureVal = map(moistureRead,MoistureMin,MoistureMax,100,0);
  uint8_t lightVal = map(lightRead,0,4095,0,100);
  uint8_t tempVal = round(tempRead);
  
  Serial.print("Moist : ");
  Serial.print(moistureRead);
  Serial.print(" | Light : ");
  Serial.print(lightRead);
  Serial.print(" | Temp : ");
  Serial.println(tempRead);

  for(int i = 1; i < 168; i++){

    moistureHist[i-1] = moistureHist[i];
    lightHist[i-1] = lightHist[i];
    tempHist[i-1] = tempHist[i];

  }

  if (moistureVal < moistLow) {
      moistLowAlert = true;
  } else {
      moistLowAlert = false;
  }
  if (moistureVal > moistHigh) {
      moistHighAlert = true;
  } else {
      moistHighAlert = false;
  }
  if (tempVal < tempLow) {
      tempLowAlert = true;
  } else {
      tempLowAlert = false;
  }
  if (tempVal > tempHigh) {
      tempHighAlert = true;
  } else {
      tempHighAlert = false;
  }

  if (moistLowAlert || moistHighAlert || tempLowAlert || tempHighAlert){
    digitalWrite(LedPin,HIGH);
    Serial.println("Alert");
    //sendAlert();

  } else {
    digitalWrite(LedPin,LOW);
  }

  moistureHist[167] = moistureVal;
  lightHist[167] = lightVal;
  tempHist[167] = tempVal;
}

// WiFi setup code

#include "esp_mac.h"
#include "esp_wifi.h"
#include <WiFi.h>
#include <WiFiMulti.h>

WiFiMulti WiFiMulti;

// Store credentials in RTC memory so they persist through deep sleep
RTC_DATA_ATTR char rtc_ssid[32] = "Your_SSID";
RTC_DATA_ATTR char rtc_password[64] = "Your_PASSWORD";


// BLE setup code

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer* pServer = NULL;

BLECharacteristic* pMoistCharacteristic = NULL;
BLECharacteristic* pTempCharacteristic = NULL;
BLECharacteristic* pLightCharacteristic = NULL;


BLECharacteristic* pMoistMaxCharacteristic = NULL;
BLECharacteristic* pMoistMinCharacteristic = NULL;
BLECharacteristic* pTempMaxCharacteristic = NULL;
BLECharacteristic* pTempMinCharacteristic = NULL;


BLECharacteristic* pAlertSSIDCharacteristic = NULL;
BLECharacteristic* pAlertPassCharacteristic = NULL;

#define SERVICE_UUID        "0d8d71f3-afea-4903-a389-d2db0912c1b1"

#define MOIST_CHAR_UUID     "48ea775b-a582-4b00-b1b9-cf789c55887f"
#define TEMP_CHAR_UUID      "2b2c8a8b-25cd-4804-a2de-b3a27c64758c"
#define LIGHT_CHAR_UUID     "18ad5c89-a11b-4a1e-b655-6ee4c820ce4a"

#define MOIST_MAX_CHAR_UUID "4711d9f9-c38e-4f0f-9ef9-4e89e1f88391"
#define MOIST_MIN_CHAR_UUID "7470e5d5-00ee-49a4-87ba-88ad813b287b"
#define TEMP_MAX_CHAR_UUID  "5f59d498-e642-426f-b63f-3e9dd58465f3"
#define TEMP_MIN_CHAR_UUID  "014930eb-902d-4fbc-b7be-1f85da76fa37"

#define ALERT_SSID_UUID  "d93c1417-f814-41c6-b38c-6d145b4df240"
#define ALERT_PASS_UUID  "74d2f3b5-9553-409f-81d2-b2670feecaec"

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      // send data?
      deviceConnected = true;
    };
    void onDisconnect(BLEServer* pServer) {
      // go back to sleep
      deviceConnected = false;
    }
};

class SharedCharacteristicCallbacks : public BLECharacteristicCallbacks {

  void onWrite(BLECharacteristic* pCharacteristic) override {
    uint8_t* rxData = pCharacteristic->getData();
    size_t rxLength = pCharacteristic->getLength();

    // Compare by pointer

    if (pCharacteristic == pMoistMaxCharacteristic) {
      Serial.print("Moisture Max Characteristic written: ");
      Serial.println(*rxData);
      moistHigh = *rxData;
      pMoistMaxCharacteristic->setValue(&moistHigh, 1);
    }

    if (pCharacteristic == pMoistMinCharacteristic) {
      Serial.print("Moisture Min Characteristic written: ");
      Serial.println(*rxData);
      moistLow = *rxData;
      pMoistMinCharacteristic->setValue(&moistLow, 1);
    }

    if (pCharacteristic == pTempMaxCharacteristic) {
      Serial.print("Temperature Max Characteristic written: ");
      Serial.println(*rxData);
      tempHigh = *rxData;
      pTempMaxCharacteristic->setValue(&tempHigh, 1);
    }

    if (pCharacteristic == pTempMinCharacteristic) {
      Serial.print("Temperature Min Characteristic written: ");
      Serial.println(*rxData);
      tempLow = *rxData;
      pTempMinCharacteristic->setValue(&tempLow, 1);
    }

    if (pCharacteristic == pAlertSSIDCharacteristic) {
      Serial.print("Alert SSID Characteristic written: ");
      String rxValue = pCharacteristic->getValue();
      Serial.println(rxValue.c_str());
      //rtc_ssid = rxValue.c_str();
      memcpy(rtc_ssid, rxValue.c_str(), rxValue.length());
      rtc_ssid[sizeof(rtc_ssid) - 1] = '\0';
      pAlertSSIDCharacteristic->setValue(rtc_ssid);
    }

    if (pCharacteristic == pAlertPassCharacteristic) {
      Serial.print("Alert Pass Characteristic written: ");
      String rxValue = pCharacteristic->getValue();
      Serial.println(rxValue.c_str());
      //rtc_password = rxValue.c_str();
      memcpy(rtc_password, rxValue.c_str(), rxValue.length());
      rtc_password[sizeof(rtc_password) - 1] = '\0';
      pAlertPassCharacteristic->setValue(rtc_password);
    }
    
  }
};

SharedCharacteristicCallbacks* myWriteCallbacks = new SharedCharacteristicCallbacks();

void StartBLE(){
  // Turn on BLE

  BLEDevice::init("SoilSense");
  
  // REQUIRED: Expand MTU so the 168-byte arrays fit in a single packet
  BLEDevice::setMTU(517);

  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);
  
  // Create the Characteristic Moisture
  pMoistCharacteristic = pService->createCharacteristic(
                      MOIST_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pMoistCharacteristic->addDescriptor(new BLE2902());

  // Create the Characteristic Temperature
  pTempCharacteristic = pService->createCharacteristic(
                      TEMP_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pTempCharacteristic->addDescriptor(new BLE2902());
  
  // Create the Characteristic Light
  pLightCharacteristic = pService->createCharacteristic(
                      LIGHT_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pLightCharacteristic->addDescriptor(new BLE2902());

  // Create the Characteristic Max moisture
  pMoistMaxCharacteristic = pService->createCharacteristic(
                      MOIST_MAX_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pMoistMaxCharacteristic->addDescriptor(new BLE2902());
  pMoistMaxCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  pMoistMaxCharacteristic->setValue(&moistHigh, 1);


  // Create the Characteristic Min moisture
  pMoistMinCharacteristic = pService->createCharacteristic(
                      MOIST_MIN_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pMoistMinCharacteristic->addDescriptor(new BLE2902());
  pMoistMinCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  pMoistMinCharacteristic->setValue(&moistLow, 1);
  
  // Create the Characteristic Max Temperature
  pTempMaxCharacteristic = pService->createCharacteristic(
                      TEMP_MAX_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pTempMaxCharacteristic->addDescriptor(new BLE2902());
  pTempMaxCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  pTempMaxCharacteristic->setValue(&tempHigh, 1);
  
  // Create the Characteristic Min Temperature
  pTempMinCharacteristic = pService->createCharacteristic(
                      TEMP_MIN_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pTempMinCharacteristic->addDescriptor(new BLE2902());
  pTempMinCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  pTempMinCharacteristic->setValue(&tempLow, 1);

  // Create the Characteristic Alert SSID Temperature
  pAlertSSIDCharacteristic = pService->createCharacteristic(
                      ALERT_SSID_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pAlertSSIDCharacteristic->addDescriptor(new BLE2902());
  pAlertSSIDCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  pAlertSSIDCharacteristic->setValue(&tempHigh, 1);

  // Create the Characteristic Alert Pass Temperature
  pAlertPassCharacteristic = pService->createCharacteristic(
                      ALERT_PASS_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pAlertPassCharacteristic->addDescriptor(new BLE2902());
  pAlertPassCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  pAlertPassCharacteristic->setValue(&tempHigh, 1);
  

  pService->start();
  
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();
  
  Serial.println("Waiting for a client connection...");

  unsigned long currentMillis = millis();
  unsigned long previousMillis = currentMillis;

  while (deviceConnected || currentMillis - previousMillis < 60000) {
    currentMillis = millis();

    if (deviceConnected) {
      previousMillis = currentMillis; // keep resetting the 60s timer while the device is connected
      // so when it disconnects, it's a 60s timer

      // FOR TESTING  
      readData(); 

      delay(500);
      Serial.println("Sending Moisture Data...");
      pMoistCharacteristic->setValue(moistureHist, sizeof(moistureHist));
      pMoistCharacteristic->notify();
      delay(500);  
      Serial.println("Sending Temperature Data...");
      pTempCharacteristic->setValue(tempHist, sizeof(tempHist));
      pTempCharacteristic->notify();
      delay(500);
      Serial.println("Sending Light Data...");
      pLightCharacteristic->setValue(lightHist, sizeof(lightHist));
      pLightCharacteristic->notify();

      oldDeviceConnected = deviceConnected;
    }

    if (!deviceConnected && oldDeviceConnected) {
      delay(500);  
      pServer->startAdvertising();
      Serial.println("Client disconnected. Restarting advertising...");
      oldDeviceConnected = deviceConnected;
    } 
    delay(10);
  }  
}

void sendAlert(){
  esp_wifi_start();

  WiFiMulti.addAP(rtc_ssid, rtc_password);

  Serial.println();
  Serial.println();
  Serial.print("Waiting for WiFi... ");

  while (WiFiMulti.run() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  delay(500);

  const uint16_t port = 80;
  const char *host = "ntfy.sh";

  Serial.print("Connecting to ");
  Serial.println(host);

  // Use NetworkClient class to create TCP connections
  NetworkClient client;

  if (!client.connect(host, port)) {
    Serial.println("Connection failed.");
    Serial.println("Waiting 5 seconds before retrying...");
    delay(5000);
    return;
  }

  String message = "{\"id\":";
  message += ESP.getEfuseMac();
  message += ", \"moistLow\":";
  message += moistLowAlert;
  message += ", \"moistHigh\":";
  message += moistHighAlert;
  message += ", \"tempLow\":";
  message += tempLowAlert;
  message += ", \"tempHigh\":";
  message += tempHighAlert;
  message += "}";


  client.print("POST /oiiaioiiiai HTTP/1.1\r\n");
  client.print("Host: ntfy.sh\r\n");
  client.print("Content-Length: ");
  client.print(message.length());
  client.print("\r\n\r\n");
  client.print(message);
  Serial.println(message);

  int maxloops = 0;

  //wait for the server's reply to become available
  while (!client.available() && maxloops < 1000) {
    maxloops++;
    delay(1);  //delay 1 msec
  }
  if (client.available() > 0) {
    //read back one line from the server
    String line = client.readStringUntil('\r');
    Serial.println(line);
  } else {
    Serial.println("client.available() timed out ");
  }

  Serial.println("Closing connection.");
  client.stop();

  esp_wifi_stop();

}

void setup(){
  Serial.begin(115200);
  Serial.println("Starting Sensor");

  // Setup adc pins for moisture and light level as inputs
  pinMode(A3,INPUT);
  pinMode(A2,INPUT);

  // Setup onboard led as output
  pinMode(LedPin,OUTPUT);
  digitalWrite(LedPin,HIGH);
  delay(500);
  digitalWrite(LedPin,LOW);

  // Setup internal temperature sensor
  temperature_sensor_config_t temp_sensor_config = TEMPERATURE_SENSOR_CONFIG_DEFAULT(-10, 80);
  ESP_ERROR_CHECK(temperature_sensor_install(&temp_sensor_config, &temp_sensor));
  ESP_ERROR_CHECK(temperature_sensor_enable(temp_sensor));
  delay(5000);

  // Check for wakeup cause
  esp_sleep_wakeup_cause_t wakeup_reason = esp_sleep_get_wakeup_cause();
  if (wakeup_reason == ESP_SLEEP_WAKEUP_TIMER) {
    Serial.println("Woke up from timer");
    readData();
  } else {
    Serial.println("Woke up from PIN");
    StartBLE();
  }

  // Setup wake pin and sleep timer
  pinMode(WAKEUP_GPIO_PIN, INPUT_PULLUP);
  uint64_t pin_mask = (1ULL << WAKEUP_GPIO_PIN);
  esp_deep_sleep_enable_gpio_wakeup(pin_mask, ESP_GPIO_WAKEUP_GPIO_LOW);
  esp_sleep_enable_timer_wakeup(SLEEP_DURATION_SEC * uS_TO_S_FACTOR);

  Serial.println("Entering deep sleep...");
  Serial.flush();
  esp_deep_sleep_start();
}

void loop(){

}

