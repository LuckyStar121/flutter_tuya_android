import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class DeviceItem{
  String strName;
  String strID;
  DeviceItem();
}

class DeviceApp extends StatelessWidget {

  DeviceApp ({Key key, this.roomname}) : super (key: key);
  final String roomname;
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Tuya Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: DevicePage(title: 'Device Test Page', FamilyName: roomname,),
    );
  }
}

class DevicePage extends StatefulWidget {

  DevicePage({Key key, this.title, this.FamilyName}) : super (key : key);

  final String title;
  final String FamilyName;

  @override
  _DevicePageState createState() => _DevicePageState();
}

class _DevicePageState extends State<DevicePage> {

  static const platform = const MethodChannel('flutter.wifi/getssid');
  static const searchDevice = const MethodChannel('flutter.wifi/searchdevice');

  final myWifiPassController = TextEditingController();

  String _strSSID = "Please confirm wifi Setting";

  String _strDeviceName = "";
  String _strDeviceID = "";
  Future<void> _getSSIDFromPlatform() async {
    String strResult;
    try{
      strResult = await platform.invokeMethod('getSSID');
    } on PlatformException catch (e) {
      strResult = e.message;
    }
    setState(() {
      _strSSID = strResult;
    });
  }

  Future<void> _searchDevice() async {
    String strResult;
    if (myWifiPassController.text.isEmpty){
      return showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            // Retrieve the text the that user has entered by using the
            // TextEditingController.
            content: Text("Please type wifi password"),
          );
        },
      );
    }
    try {
      strResult = await searchDevice.invokeMethod('getDevice', {'wifipass': myWifiPassController.text});
      List<String> device = strResult.split(",");
      setState(() {
        _strDeviceName = device[0];
        _strDeviceID = device[1];
      });
    } on PlatformException catch (e) {
      strResult = e.message;
      setState(() {
        _strDeviceName = "";
        _strDeviceID = "";
      });
    }
  }

  void main() async {
    _getSSIDFromPlatform();
  }

  @override
  Widget build(BuildContext context) {

    main();

    Widget DeviceList = Container (
      padding: EdgeInsets.only(left: 10),
      color: Colors.red.shade100,
      child: Row(
        children: <Widget>[
          Expanded (
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[

                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    _strDeviceName,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    _strDeviceID,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
    Widget WifiSection = Container(
      padding: EdgeInsets.only(left: 10),
      color: Colors.green.shade100,
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  color: Colors.red.shade100,
                  child: Text(
                    "FamilyName:  " + widget.FamilyName,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  color: Colors.blue.shade100,
                  child: Text(
                    _strSSID,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    "Verification Code",
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  color: Colors.red.shade100,
                  child: Text(
                    "",
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: EdgeInsets.only(right: 10),
                  child: TextField(
                    controller: myWifiPassController,
                    decoration: InputDecoration(
                        filled: true,
                        fillColor: Colors.white,
                        border: OutlineInputBorder(
                          borderSide: BorderSide(color: Colors.white),
                          borderRadius: BorderRadius.circular(15),
                        ),
                        hintText: 'Type your wifi password'
                    ),
                  ),
                ),

                Container(
                  child: RaisedButton(
                    child: const Text('Search Device'),
                    color: Theme.of(context).accentColor,
                    elevation: 4.0,
                    splashColor: Colors.blueGrey,
                    onPressed: _searchDevice,
                  ),
                ),
                DeviceList,
              ],
            ),
          ),
        ],
      ),
    );

    return Scaffold(
      appBar: AppBar(
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Center(
        child: Container(
          padding: const EdgeInsets.all(10),
          child: Column(
            children: [
              WifiSection,
            ],
          ),
        ),
      ),
    );
  }
}

