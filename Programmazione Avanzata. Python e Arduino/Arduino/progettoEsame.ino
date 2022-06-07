// PARAMETRI JOYSTICK CONTROLLER
# define BUTTON_PIN 2
uint8_t buttonPressed = 0;
int UD = 0;
int LR = 0;
int x_position = 0;
int y_position = 7; 

// ARRAY MULTIDIMENSIONALE
# define rows (8)
# define cols (8)
int pattern[rows][cols];

// MATRICE LED
#include "LedControl.h"
LedControl lc = LedControl(8,10,9,1); // LedControl(DIN dataPin, CLK clockPin, CS csPin, numDevices)

void setup() {
  Serial.begin(9600);
  
  // COLLEGAMENTO PULSANTE DEL JOYSTICK AL PIN
  pinMode(BUTTON_PIN, INPUT_PULLUP); // INPUT_PULLUP abilita la resistenza interna di Pull-Up
  // COLLEGAMENTO PULSANTE DI CONFERMA
  pinMode(4, INPUT_PULLUP);
  
  // CONFIGURAZIONE MATRICE LED
  lc.shutdown(0,false); // Disattiva risparmio energia, abilita lo schermo
  lc.setIntensity(0,8); // Impostazione luminosità (0~15)
  lc.clearDisplay(0); // Pulizia matrice led
}

void loop() {
  // LETTURA
  UD = analogRead(A0);
  LR = analogRead(A1);
  buttonPressed = !digitalRead(BUTTON_PIN);

  // PULSANTE DI CONFERMA PREMUTO
  if (digitalRead(4) == LOW) {
    tone(7, 1700, 200);
    delay(200);
    tone(7, 1500, 200);
    delay(200);
    tone(7, 1300, 200);
    delay(200);
    printPattern(pattern); // INVIO PATTERN
  }

  // MAPPATURA VALORI IN INPUT DEL JOYSTICK
  char x_translate = map(LR, 1021, 0, 4, -4); // map(value, fromLow, fromHigh, toLow, toHigh)
  char y_translate = map(UD, 1021, 0, 4, -4);

  // PULIZIA DISPLAY
  lc.clearDisplay(0);
  
  // MODIFICA COORDINATE AL MOVIMENTO DEL JOYSTICK
  if (x_translate != 0) {
    x_position = x_position + x_translate;
  }
  if (y_translate != 0) {
    y_position = y_position + y_translate;
  }

  // RESET COORDINATE NEL RANGE DELLA MATRICE
  if (x_position > 7) {
    x_position = 7;
  }
  if (x_position < 0) {
    x_position = 0;
  }
  if (y_position > 7) {
    y_position = 7;
  }
  if (y_position < 0) {
    y_position = 0;
  }
 
  //Serial.print("X_POSITION = ");
  //Serial.print(x_position);
  //Serial.print(", Y_POSITION = ");
  //Serial.print(y_position);
  //Serial.print(", BUTTON PRESSED = ");
  //Serial.println(buttonPressed);

  // DISPLAY PATTERN
  displayPattern(pattern);

  // PRESSIONE PULSANTE DEL JOYSTICK
  if (buttonPressed){
    if (pattern[7-y_position][x_position] == 0){
      tone(7, 1700, 200);
      pattern[7-y_position][x_position] = 1;
    } else {
      tone(7, 1100, 200);
    }
    delay(200);
  }

  // ALTERNANZA ACCESO-SPENTO PER IL LED PUNTATO
  lc.setLed(0, x_position, y_position, false);
  delay(20);
  lc.setLed(0, x_position, y_position, true);
  delay(80);
}

// DISPLAY PATTERN: Accensione di tutti i led corrispondenti al pattern inserito dall'utente
void displayPattern( const int a[][ cols ] ) {
   for (int i = 0; i < rows; ++i) {
      for (int j = 0; j < cols; ++j){
        if (a[i][j]){
          // accensione led
          lc.setLed(0, j, 7-i, true);
        }
      }
   }
}

// INVIO PATTERN: Stampa nel monitor seriale per l'invio allo script python
void printPattern(const int a[][cols]){
  Serial.print("[");
  for (int i = 0; i < rows; ++i){
    Serial.print("[");
    for (int j = 0; j < cols; ++j){
      Serial.print(a[i][j]);
      if (j < cols-1){
        Serial.print(",");
      }
    }
    Serial.print("]");
    if (i < rows-1){
      Serial.print(",");
    }
  }
  Serial.println("]");
}
