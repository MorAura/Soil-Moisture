/*
Connections:
  Moisture Sensor - A3
  Light Sensor - A2
*/

// To be determined experimentally
#define MoistureMax 4095
#define MoistureMin 0

// To store old data
int moistureHist[168] = {0};
int lightHist[168] = {0};

// Configured Alert Values
int moistHigh = 100;
int moistLow = 0;


// Function to read new data, calculate values and add save it to memory. (Trigger alert if necessary).
void readData(){

  int moistureRead = analogRead(A3);
  int lightRead = analogRead(A2);

  int moistureVal = map(moistureRead,MoistureMin,MoistureMax,0,100);
  int lightVal = map(lightRead,0,4095,0,1000);
  
  for(int i = 1; i < 168; i++){

    moistureHist[i-1] = moistureHist[i];
    lightHist[i-1] = lightHist[i];

  }

  moistureHist[167] = moistureVal;
  lightHist[167] = lightVal;

}

void setup(){

  Serial.begin(115200);

  pinMode(A3,INPUT);
  pinMode(A2,INPUT);

}

void loop(){
  
}

