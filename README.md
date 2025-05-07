apk安装时可能会报毒，可能是因为udp通信需要监听6543端口导致的，我不清楚到底是什么原因，介意的话用源码自己build一个apk就行。
The apk may report a virus when you install it. This may be because UDP communication needs to listen to port 6543. I don't know the exact reason. If you mind, just build an apk yourself using the source code.
安装apk然后打开wifi热点，热点名wifiplane，密码wifiplane1234。把bin文件下载到WiFi模块，模块会自动连WiFi，从tx引脚输出crsf信号。
Install the apk and open the WiFi hotspot, set the hotspot name to WiFiplane, and the password to WiFiplane1234. Download the bin file to the WiFi module, the module will automatically connect to WiFi and output the crsf signal from the TX pin.
