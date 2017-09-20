#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

// wifi credentials
#define WIFI_SSID   ("LEDE")
#define WIFI_PASSWD ("1qaz2wsx3edcEsp")

// the port the UDP will use.
#define UDP_PORT (5556)

// UDP connection
WiFiUDP udp;

#define NUMBER_OF_OUTPUTS (9)
int outputsPins[9] = {13, 12, 14, 16, 15, 2, 0, 4, 5};
int outputPinsState[9];
int outputPinsDuration[9];
unsigned long outputPinsRemainingDuration[9];

unsigned long prevMillis = 0;

// Function Declarations
void parseUdpData(char* _data, int _length);
void setPin(int _pin, int _state, int _duration);
void sendPinInfo();

void setup()
{
    // Enable the Serial Connection
    Serial.begin(115200);
    delay(100);

    // We start by connecting to a WiFi network
    Serial.println();
    Serial.println();
    Serial.print("Connecting to ");
    Serial.println(WIFI_SSID);

    // WiFi.begin(ssid, password);
    WiFi.begin(WIFI_SSID, WIFI_PASSWD);


    while (WiFi.status() != WL_CONNECTED)
    {
        delay(500);
        Serial.print(".");
    }

    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());

    // start the UDP on the specific port
    udp.begin(UDP_PORT);

    // Setup the pins for output
    for (int i = 0; i < NUMBER_OF_OUTPUTS; ++i)
    {
        pinMode(outputsPins[i], OUTPUT);
        digitalWrite(outputsPins[i], LOW);
        outputPinsState[i] = 0;
    }
}


void loop()
{
    // Check if there is a packet available
    int packetSize = udp.parsePacket();

    // there is data
    if (packetSize > 0)
    {
        IPAddress remoteIp = udp.remoteIP();

        //buffer to hold incoming packet
        char packetBuffer[1024];
        memset(packetBuffer, 0, 1024);

        int len = udp.read(packetBuffer, 255);
        parseUdpData(packetBuffer, len);
    }

    if (prevMillis == 0)
    {
        prevMillis = millis();
        return;
    }

    unsigned long currentMillis = millis();

    // Handle rollover
    if (prevMillis >= currentMillis)
    {
        prevMillis = currentMillis;
        return;
    }

    // get the millisecond difference
    unsigned long difference = (currentMillis - prevMillis);

    // wait 1 second
    if ( difference > 1000)
    {
        // update the previous millis to current millis
        prevMillis = currentMillis;

        for (int i = 0; i < NUMBER_OF_OUTPUTS; i++)
        {
            if ((outputPinsState[i] == 1) && (outputPinsDuration[i] != -1))
            {
                outputPinsRemainingDuration[i] -= difference;

                if (outputPinsRemainingDuration[i] <= 0)
                {
                    setPin(i, 0, -1);
                }
            }
        }
    }
}



void parseUdpData(char* _data, int _length)
{
    // Packet is not large enough for any valid command
    if (_length < 3)
    {
        return;
    }

    Serial.println("packetDataArrived");

    // convert into a String object for easy processing
    char dataTmp[_length + 1];
    dataTmp[_length] = 0;
    memcpy(dataTmp, _data, _length);
    String data = String(dataTmp);

    String commandType = data.substring(0, 3);

    if (commandType.equals("GET"))
    {
        Serial.println("GET Command");
        sendPinInfo();
    }
    else if (commandType.equals("SET"))
    {
        Serial.println("SET Command");

        // count the number of commas present
        int commaCount = 0;
        for (int i = 0; i < data.length(); i++)
        {
            if (data.charAt(i) == ',')
            {
                commaCount++;
            }
        }

        // data is incorrectly formated
        if (commaCount != 3)
        {
            return;
        }

        // find the comma positions
        int commaPos[3];
        for (int i = 0; i < 3; i++)
        {
            if (i == 0)
            {
                commaPos[i] = data.indexOf(',');
            }
            else
            {
                commaPos[i] = data.indexOf(',', commaPos[i - 1] + 1);
            }
        }

        // split the string into the value positions based on the commas
        String a = data.substring(commaPos[0], commaPos[1]);
        String b = data.substring(commaPos[1], commaPos[2]);
        String c = data.substring(commaPos[2], data.length());

        // remove the commas from the string
        a = a.substring(1, a.length());
        b = b.substring(1, b.length());
        c = c.substring(1, c.length());

        // get rid of the leading and trailing white spaces
        a.trim();
        b.trim();
        c.trim();

        // Convert to an integer
        int aInt = a.toInt();
        int bInt = b.toInt();
        int cInt = c.toInt();

        // set the pins
        // convert pin number from starting at 1 to starting at 0
        setPin(aInt - 1, bInt, cInt);
    }
}



void setPin(int _pin, int _state, int _duration)
{
    // Serial.print(_pin);
    // Serial.print(" ,");
    // Serial.print(outputsPins[_pin]);
    // Serial.print(" ,");
    // Serial.print(_state);
    // Serial.print(" ,");
    // Serial.print(_duration);
    // Serial.println();


    // update the pin state info
    outputPinsState[_pin] = _state;
    outputPinsDuration[_pin] = _duration;
    outputPinsRemainingDuration[_pin] = _duration;

    // set the pin mode
    if (_state == 0)
    {
        digitalWrite(outputsPins[_pin], LOW);
    }
    else
    {
        digitalWrite(outputsPins[_pin], HIGH);
    }
}



void sendPinInfo()
{
    String retString = "";

    for (int i = 0; i < NUMBER_OF_OUTPUTS; i++)
    {
        retString += (i + 1);
        retString += ", ";
        retString += outputPinsState[i];
        retString += ", ";
        retString += outputPinsDuration[i];
        retString += "\n";
    }

    udp.beginPacket(udp.remoteIP(), udp.remotePort());
    udp.write(retString.c_str());
    udp.endPacket();

    Serial.println(retString);
}




















