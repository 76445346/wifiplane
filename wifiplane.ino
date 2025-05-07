#include <WiFi.h>
#include <WiFiUdp.h>
#这个文件适用于dx-wf25模块 This file is suitable for dx-wf25 module
#define P_ID 1
#define ST_LED  9
#define L_MOTOR 8
#define R_MOTOR 7
#define DC_RSSI 500  // Time in mS for send RSSI
#define DC_RX   1300   // Time in mS for tx inactivity 200 old problem of motor stopping flickring
#define CRSF_SERIAL Serial
#define CRSF_TX_PIN 17
#define CRSF_BAUD 420000  // CRSF 默认波特率
#define remoteIP3 255

unsigned int localPort = 6543;      // local port to listen on
unsigned int remotPort = 6543;      // remot port to talk on

// ADC_MODE(ADC_VCC);

unsigned int l_speed = 0;
unsigned int r_speed = 0;

unsigned long premillis_rssi = 0;
unsigned long premillis_rx   = 0;
unsigned long RXCNT,RXCNT_od = 0;

int status = WL_IDLE_STATUS;
char ssid[] = "wifiplane";   //  your network SSID (name)
char pass[] = "wifiplane1234";    // your network password (use for WPA, or use as key for WEP)
int keyIndex = 0;            // your network key Index number (needed only for WEP)
IPAddress remotIp; 
uint8_t  packetBuffer[10]; //buffer to hold incoming packet
uint8_t  replyBuffer[]={P_ID,0x01,0x01,0x00}; // a string to send back
WiFiUDP Udp;
uint16_t channels[16] = {992};  // CRSF通道值范围: 172 ~ 1811

uint8_t crsfPacket[26];  // 最大26字节（Header+Length+Type+Payload+CRC）

// 计算 CRC8 (使用 Crossfire 标准多项式)
uint8_t crsfCrc8(uint8_t* data, uint8_t len) {
  uint8_t crc = 0;
  for (uint8_t i = 0; i < len; i++) {
    crc ^= data[i];
    for (uint8_t j = 0; j < 8; j++) {
      if (crc & 0x80)
        crc = (crc << 1) ^ 0xD5;
      else
        crc <<= 1;
    }
  }
  return crc;
}

// 构建 CRSF RC 通道数据帧
void buildCrsfPacket() {
  crsfPacket[0] = 0xC8;  // Destination: receiver
  crsfPacket[1] = 24;    // Payload length: 1 (type) + 22 (channels) + 1 (CRC)
  crsfPacket[2] = 0x16;  // Type: RC Channels

  // Pack 16x11bit channels into 22 bytes
  uint32_t bitBuffer = 0;
  uint8_t bitCount = 0;
  int outIndex = 3;

  for (int i = 0; i < 16; i++) {
    bitBuffer |= ((uint32_t)(channels[i] & 0x07FF)) << bitCount;
    bitCount += 11;

    while (bitCount >= 8) {
      crsfPacket[outIndex++] = bitBuffer & 0xFF;
      bitBuffer >>= 8;
      bitCount -= 8;
    }
  }

  if (bitCount > 0) {
    crsfPacket[outIndex++] = bitBuffer & 0xFF;
  }

  // CRC (从 type 到最后一个通道字节)
  uint8_t crc = crsfCrc8(&crsfPacket[2], crsfPacket[1] - 1);
  crsfPacket[outIndex++] = crc;
}

// the setup function runs once when you press reset or power the board
void setup() {
  WiFi.mode(WIFI_STA);
  //WiFi.setOutputPower(2.5);
  // analogWriteRange(255);
  pinMode(L_MOTOR, OUTPUT);
  pinMode(R_MOTOR, OUTPUT);
  analogWrite(L_MOTOR,0);
  analogWrite(R_MOTOR,0);
  pinMode(ST_LED, OUTPUT);
  digitalWrite(ST_LED,HIGH);
  WiFi.begin(ssid, pass);
  while (WiFi.status() != WL_CONNECTED) 
  {
    digitalWrite(ST_LED,LOW);
    delay(60);
    digitalWrite(ST_LED,HIGH);
    delay(1000);
  }
  remotIp=WiFi.localIP();
  remotIp[3] = remoteIP3;
  Udp.begin(localPort);
  CRSF_SERIAL.begin(CRSF_BAUD);
  for (int i = 0; i < 16; i++) {
    channels[i] = 992;  // 中位值
  }
}

// the loop function runs over and over again forever
void loop() {
  // delay(1);
  if(WiFi.status() == WL_CONNECTED)
  {
    digitalWrite(ST_LED,LOW);
    // if there's data available, read a packet
    int packetSize = Udp.parsePacket();
    if (packetSize) 
    {
      // read the packet into packetBufffer
      RXCNT+=1;
      int len = Udp.read(packetBuffer, 10);
      if (len > 1) 
      {
          channels[0] =992 + ((unsigned int)packetBuffer[0]-127)*6;
          channels[1] =992 - ((unsigned int)packetBuffer[1]-127)*6;
          channels[2] =992 - ((unsigned int)packetBuffer[2]-127)*6;
          channels[3] =992 + ((unsigned int)packetBuffer[3]-127)*6;
          if(((unsigned int)(packetBuffer[4])) & 1)channels[4]=1800; else channels[4]=180;
          if(((unsigned int)(packetBuffer[4])) & 2)channels[5]=1800; else channels[5]=180;
          if(((unsigned int)(packetBuffer[4])) & 4)channels[6]=1800; else channels[6]=180;
          if(((unsigned int)(packetBuffer[4])) & 8)channels[7]=1800; else channels[7]=180;
          premillis_rx = millis();
          buildCrsfPacket();
          CRSF_SERIAL.write(crsfPacket, 26);
      }
      
    }
    if(millis()-premillis_rssi > DC_RSSI)
    {
       premillis_rssi = millis();
       long rssi = abs(WiFi.RSSI());
       float vcc = 0;
      //  float vcc = (((float)ESP.getVcc()/(float)1024.0)+0.75f)*10;
       replyBuffer[1] = (unsigned char)rssi;
       replyBuffer[2] = (unsigned char)vcc;
       
       Udp.beginPacket(remotIp, remotPort);
       Udp.write(replyBuffer,(size_t)3);
       Udp.endPacket();
       RXCNT_od = RXCNT;
     }
     if(millis()-premillis_rx > DC_RX)
     {
       analogWrite(L_MOTOR,0);
       analogWrite(R_MOTOR,0);
     }
  }
  else
  {
    analogWrite(L_MOTOR,0);
    analogWrite(R_MOTOR,0);
    digitalWrite(ST_LED,LOW);
    delay(20);
    digitalWrite(ST_LED,HIGH);
    delay(100);
  }
}
