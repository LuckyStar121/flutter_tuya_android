import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'device.dart';

void main() => runApp(MainApp());

/*------------------------------ Login Page ------------------------------*/

class MainApp extends StatelessWidget {
  // This widget is the root of your application.
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
      home: MainPage(title: 'Login'),
    );
  }
}

class MainPage extends StatefulWidget {
  MainPage({Key key, this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;
  @override
  _MainPageState createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {

  static const platform = const MethodChannel('flutter.native/login');

  final myNameController = TextEditingController();
  final myPassController = TextEditingController();
  final myPhoneCodeController = TextEditingController();

  String _testString = '';
  @override
  void dispose() {
    // Clean up the controller when the widget is disposed.
    myNameController.dispose();
    myPassController.dispose();
    myPhoneCodeController.dispose();
    super.dispose();
  }

  Future<void> _getTestString() async {
    String testString;

    if (myNameController.text.isEmpty || myPassController.text.isEmpty || myPhoneCodeController.text.isEmpty)
      return showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            // Retrieve the text the that user has entered by using the
            // TextEditingController.
            content: Text('Please type your Email and Password'),
          );
        },
      );
    List<String> result;
    try{
      testString = await platform.invokeMethod('getTestString', {'name': myNameController.text, 'pass': myPassController.text, 'phonecode': myPhoneCodeController.text});
      result = testString.split(",");
      Navigator.push(
          context,
          new MaterialPageRoute(
              builder: (context) => DeviceApp(roomname: result[1],),
          ),
      );
    } on PlatformException catch (e) {
      testString = e.message;
    }
    setState(() {
      _testString = result[0];
    });
  }

  Future<void> _goToRegister() async {
    if (myNameController.text.isEmpty || myPhoneCodeController.text.isEmpty){
      return showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            // Retrieve the text the that user has entered by using the
            // TextEditingController.
            content: Text('Please type your Email and Phone Code'),
          );
        },
      );
    }
    Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => RegisterPage(title: 'Register', strEmail: myNameController.text, strPhoneCode: myPhoneCodeController.text,)
        ),
    );
  }
  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    Widget LRInputSection = Container(
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
                  color: Colors.blue.shade100,
                  child: Text(
                    _testString,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    'Country Code',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: TextField(
                    controller: myPhoneCodeController,
                    decoration: InputDecoration(
                        border: InputBorder.none,
                        hintText: '+357'
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    'Email',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: TextField(
                    controller: myNameController,
                    decoration: InputDecoration(
                        border: InputBorder.none,
                        hintText: '234567890 or example@exam.com'
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    'Password',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: TextField(
                    controller: myPassController,
                    obscureText: true,
                    decoration: InputDecoration(
                        border: InputBorder.none,
                        hintText: 'length is longer than 6'
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
    Widget LRButtonSection = Container(
      color: Colors.red.shade200,
      padding: EdgeInsets.only(top:5, bottom: 5),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          Container(
              child: RaisedButton(
              child: const Text('Login'),
              color: Theme.of(context).accentColor,
              elevation: 4.0,
              splashColor: Colors.blueGrey,
              onPressed: _getTestString,
            ),
          ),
          Container(
            child: RaisedButton(
              child: const Text('Register'),
              color: Theme.of(context).accentColor,
              elevation: 4.0,
              splashColor: Colors.blueGrey,
              onPressed: _goToRegister,
            ),
          ),
        ],
      ),
    );
//    Widget SearchWifiDevice = Container(
//      color: Colors.green.shade200,
//      padding: EdgeInsets.only(top:5, bottom: 5),
//      child: Row(
//        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
//        children: [
//          Container(
//            child: RaisedButton(
//              child: const Text('Search Device'),
//              color: Theme.of(context).accentColor,
//              elevation: 4.0,
//              splashColor: Colors.blueGrey,
//              onPressed: () {
//                return showDialog(
//                  context: context,
//                  builder: (context) {
//                    return AlertDialog(
//                      // Retrieve the text the that user has entered by using the
//                      // TextEditingController.
//                      content: Text('Searching...'),
//                    );
//                  },
//                );
//                // Perform some action
//              },
//            ),
//          ),
//        ],
//      ),
//    );
//    Widget DeviceList = Container(
//
//    );
//    Widget MainForm = Container(
//
//      child: Column(
//        crossAxisAlignment: CrossAxisAlignment.start,
//        children: [
//          AddRoom,
//          SearchWifiDevice,
////          AddWifiDevice,
//        ],
//      ),
//    );
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
              LRInputSection,
              LRButtonSection,
            ],
          ),
        ),
      ),
    );
  }
}

/*------------------------------ Register Page ------------------------------*/

class RegisterPage extends StatefulWidget {

  RegisterPage({Key key, this.title, @required this.strEmail, @required this.strPhoneCode}) : super(key: key);

  final String title;
  final String strEmail;
  final String strPhoneCode;

  @override
  _RegisterPageState createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {

  static const platform = const MethodChannel('flutter.register/register');

  final myVerificationCodeController = TextEditingController();
  final myPassController = TextEditingController();

  bool isCode = false;
  @override
  void dispose() {
    // Clean up the controller when the widget is disposed.
    myPassController.dispose();
    myVerificationCodeController.dispose();
    super.dispose();
  }

  Future<void> _goToLogin() async {
    String strResult;
    if (myPassController.text.isEmpty || myVerificationCodeController.text.isEmpty){
      return showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            // Retrieve the text the that user has entered by using the
            // TextEditingController.
            content: Text('Please type your Verification Code and Password'),
          );
        },
      );
    }
    if (isCode) {
      try{
        strResult = await platform.invokeMethod("goToLogin", {"email": widget.strEmail, "code": myVerificationCodeController.text, "pass": myPassController.text, 'phonecode': widget.strPhoneCode});
        if (strResult == "success") {
          isCode = true;
          Navigator.pop(context);
        }
      } on PlatformException catch (e) {
        strResult = e.message;

        myVerificationCodeController.text = "";
        myPassController.text = "";
        return showDialog(
          context: context,
          builder: (context) {
            return AlertDialog(
              // Retrieve the text the that user has entered by using the
              // TextEditingController.
              content: Text(strResult),
            );
          },
        );
      }
    }
  }

  Future<void> _getVerificationCode() async {
    String strResult;
    try{
      strResult = await platform.invokeMethod('getVerification', {'email': widget.strEmail, 'phonecode': widget.strPhoneCode});
      if (strResult == "success")
        isCode = true;
      else
        isCode = false;
    } on PlatformException catch (e) {
      strResult = e.message;
      isCode = false;
      return showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            // Retrieve the text the that user has entered by using the
            // TextEditingController.
            content: Text(strResult),
          );
        },
      );

    }
  }

  @override
  Widget build(BuildContext context) {



    Widget RegisterSection = Container(
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
                  child: Row(
                    children: [
                      Container(
                        width: 200.0,
                        padding: EdgeInsets.only(right: 10),
                        child: TextField(
                          controller: myVerificationCodeController,
                          decoration: InputDecoration(
                            filled: true,
                            fillColor: Colors.white,
                            border: OutlineInputBorder(
                              borderSide: BorderSide(color: Colors.white),
                              borderRadius: BorderRadius.circular(15),
                            ),
                            hintText: 'Type your room name'
                          ),
                        ),
                      ),
                      Container(
                        child: RaisedButton(
                          child: const Text('Get'),
                          color: Theme.of(context).accentColor,
                          elevation: 4.0,
                          splashColor: Colors.blueGrey,
                          onPressed: _getVerificationCode,
                        ),
                      )
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: Text(
                    'Password',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 20,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.only(bottom: 6),
                  child: TextField(
                    controller: myPassController,
                    obscureText: true,
                    decoration: InputDecoration(
                        border: InputBorder.none,
                        hintText: 'length is longer than 6'
                    ),
                  ),
                ),
                Container(
                  child: RaisedButton(
                    child: const Text('Register'),
                    color: Theme.of(context).accentColor,
                    elevation: 4.0,
                    splashColor: Colors.blueGrey,
                    onPressed: _goToLogin,
                  ),
                ),
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
              RegisterSection,
            ],
          ),
        ),
      ),
    );
  }
}


