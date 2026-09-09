// Internal temp config

#include <driver/temperature_sensor.h>
temperature_sensor_handle_t temp_sensor = NULL;


/*
Connections:
  Moisture Sensor - A3
  Light Sensor - A2

  Need to finalize temperature measurement but use internal temp for testing
*/

// To be determined experimentally
#define MoistureMax 4095
#define MoistureMin 0

// To store old data
uint8_t moistureHist[168] = {0};
uint8_t lightHist[168] = {0};
uint8_t tempHist[168] = {0};

// Configured Alert Values
uint8_t moistHigh = 100;
uint8_t moistLow = 0;
uint8_t tempHigh = 100;
uint8_t tempLow = 0;

bool deviceConnected = false;
bool oldDeviceConnected = false;


// Function to read new data, calculate values and add save it to memory. (Trigger alert if necessary).
void readData(){

  int moistureRead = analogRead(A3);
  int lightRead = analogRead(A2);
  float tempRead = 0;
  ESP_ERROR_CHECK(temperature_sensor_get_celsius(temp_sensor, &tempRead));
  

  uint8_t moistureVal = map(moistureRead,MoistureMin,MoistureMax,0,100);
  uint8_t lightVal = map(lightRead,0,4095,0,100);
  uint8_t tempVal = round(tempRead);
  
  for(int i = 1; i < 168; i++){

    moistureHist[i-1] = moistureHist[i];
    lightHist[i-1] = lightHist[i];
    tempHist[i-1] = tempHist[i];

  }

  moistureHist[167] = moistureVal;
  lightHist[167] = lightVal;
  tempHist[167] = tempVal;
}


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

#define SERVICE_UUID        "0d8d71f3-afea-4903-a389-d2db0912c1b1"

#define MOIST_CHAR_UUID     "48ea775b-a582-4b00-b1b9-cf789c55887f"
#define TEMP_CHAR_UUID      "2b2c8a8b-25cd-4804-a2de-b3a27c64758c"
#define LIGHT_CHAR_UUID     "18ad5c89-a11b-4a1e-b655-6ee4c820ce4a"

#define MOIST_MAX_CHAR_UUID "4711d9f9-c38e-4f0f-9ef9-4e89e1f88391"
#define MOIST_MIN_CHAR_UUID "7470e5d5-00ee-49a4-87ba-88ad813b287b"
#define TEMP_MAX_CHAR_UUID  "5f59d498-e642-426f-b63f-3e9dd58465f3"
#define TEMP_MIN_CHAR_UUID  "014930eb-902d-4fbc-b7be-1f85da76fa37"

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
    }

    if (pCharacteristic == pMoistMinCharacteristic) {
      Serial.print("Moisture Min Characteristic written: ");
      Serial.println(*rxData);
      moistLow = *rxData;
    }

    if (pCharacteristic == pTempMaxCharacteristic) {
      Serial.print("Temperature Max Characteristic written: ");
      Serial.println(*rxData);
      tempHigh = *rxData;
    }

    if (pCharacteristic == pTempMinCharacteristic) {
      Serial.print("Temperature Min Characteristic written: ");
      Serial.println(*rxData);
      tempLow = *rxData;
    }
  }
};

SharedCharacteristicCallbacks* myWriteCallbacks = new SharedCharacteristicCallbacks();

void StartComms(){
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
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pMoistCharacteristic->addDescriptor(new BLE2902());

  // Create the Characteristic Temperature
  pTempCharacteristic = pService->createCharacteristic(
                      TEMP_CHAR_UUID,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pTempCharacteristic->addDescriptor(new BLE2902());
  
  // Create the Characteristic Light
  pLightCharacteristic = pService->createCharacteristic(
                      LIGHT_CHAR_UUID,
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


  // Create the Characteristic Min moisture
  pMoistMinCharacteristic = pService->createCharacteristic(
                      MOIST_MIN_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pMoistMinCharacteristic->addDescriptor(new BLE2902());
  pMoistMinCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  
  // Create the Characteristic Max Temperature
  pTempMaxCharacteristic = pService->createCharacteristic(
                      TEMP_MAX_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pTempMaxCharacteristic->addDescriptor(new BLE2902());
  pTempMaxCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK
  
  // Create the Characteristic Min Temperature
  pTempMinCharacteristic = pService->createCharacteristic(
                      TEMP_MIN_CHAR_UUID,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE 
                    );
  pTempMinCharacteristic->addDescriptor(new BLE2902());
  pTempMinCharacteristic->setCallbacks(myWriteCallbacks); // ATTACH CALLBACK

  pService->start();
  
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();
  
  Serial.println("Waiting for a client connection...");
}

void setup(){

  Serial.begin(115200);

  // Setup adc pins for moisture and light level as inputs
  pinMode(A3,INPUT);
  pinMode(A2,INPUT);

  // Setup internal temperature sensor
  temperature_sensor_config_t temp_sensor_config = TEMPERATURE_SENSOR_CONFIG_DEFAULT(-10, 80);
  ESP_ERROR_CHECK(temperature_sensor_install(&temp_sensor_config, &temp_sensor));
  ESP_ERROR_CHECK(temperature_sensor_enable(temp_sensor));

  StartComms();
}

// testing 
unsigned long previousDataMillis = 0;

void loop(){
  // for testing

  unsigned long currentMillis = millis();

  // 1. THE NON-BLOCKING TIMER
  if (currentMillis - previousDataMillis >= 10000) {
    // Save the last time you read the data
    previousDataMillis = currentMillis;

    // Trigger the sensor reading
    readData();
    Serial.println("10 s passed: Read new sensor data.");
  }

  if (deviceConnected && !oldDeviceConnected) {
      
      // Brief delay to let the Android app finish negotiating the MTU
      delay(500); 

      Serial.println("Sending Moisture Data...");
      pMoistCharacteristic->setValue(moistureHist, sizeof(moistureHist));
      pMoistCharacteristic->notify();
      delay(50); // Small pause to prevent radio buffer overflow

      Serial.println("Sending Temperature Data...");
      pTempCharacteristic->setValue(tempHist, sizeof(tempHist));
      pTempCharacteristic->notify();
      delay(50);

      Serial.println("Sending Light Data...");
      pLightCharacteristic->setValue(lightHist, sizeof(lightHist));
      pLightCharacteristic->notify();

      oldDeviceConnected = deviceConnected;
  }
  
  // 2. If a device just disconnected, restart advertising
  if (!deviceConnected && oldDeviceConnected) {
      delay(500); // Give the radio a moment to reset
      pServer->startAdvertising(); 
      Serial.println("Client disconnected. Restarting advertising...");
      oldDeviceConnected = deviceConnected;
  }
}

