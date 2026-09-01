#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// Standard 128-bit UUIDs for SoilSense service & characteristic
#define SERVICE_UUID        "4fa8691a-1360-4642-9985-476208221e56"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

BLECharacteristic *pCharacteristic;
bool deviceConnected = false;

class ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) override {
      deviceConnected = true;
    }

    void onDisconnect(BLEServer* pServer) override {
      deviceConnected = false;
      // Restart advertising so the app can rediscover & reconnect
      BLEDevice::startAdvertising();
    }
};

void setup() {
  Serial.begin(115200);

  // 1. Initialize BLE with broadcast device name
  BLEDevice::init("SoilSense-Sensor");

  // 2. Create BLE Server and register callbacks
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  // 3. Create GATT Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // 4. Create Characteristic with READ + NOTIFY properties
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );

  // Client Characteristic Configuration Descriptor (CCCD) for Notifications
  pCharacteristic->addDescriptor(new BLE2902());

  // 5. Start Service & Advertising
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06); // Helps with iOS connection stability
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();

  Serial.println("SoilSense BLE Sensor ready & advertising!");
}

void loop() {
  if (deviceConnected) {
    // Replace these simulated values with analogRead() / digital sensor logic
    int moisture = random(35, 75);              // Moisture percentage (0-100%)
    int light = random(1200, 4500);             // Light level in Lux
    float temp = 21.0 + (random(0, 80) / 10.0);  // Temperature in Celsius
    int waterLevel = random(300, 700);          // Reservoir water level in ml

    // Format JSON string strictly matching `@section:ble-data-format`
    char jsonBuffer[128];
    snprintf(jsonBuffer, sizeof(jsonBuffer),
      "{\"moisture_percent\":%d,\"light_lux\":%d,\"temperature_celsius\":%.1f,\"water_level_ml\":%d}",
      moisture, light, temp, waterLevel
    );

    // Notify connected mobile client
    pCharacteristic->setValue(jsonBuffer);
    pCharacteristic->notify();

    Serial.print("BLE Transmitted: ");
    Serial.println(jsonBuffer);

    delay(3000); // Sampling rate: every 3 seconds
  } else {
    delay(1000);
  }
}