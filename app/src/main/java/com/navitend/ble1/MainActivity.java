package com.navitend.ble1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.ParcelUuid;
import android.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements multipleChoiceDialogFragment.onMultiChoiceListener {
    //-------------------------------------regarding debugging
    private final String tag = "We said that: ";

    //------------------------------------regarding view
    private WebView browser = null;
    private Handler mHandler = null;
    private TextView battery_view;
    private String battery = "100";

    //-----------------------------------regarding communication
    private final int NOTCONNECTED = 0, SEARCHING = 1, FOUND = 2, CONNECTED = 3, DISCOVERING = 4,
            COMMUNICATING = 5, CONFIGURE = 6, DISCONNECTING = 7, INTERROGATE = 8, RECORDING = 9;
    private BluetoothAdapter bluetoothAdapter;
    private Byte msg_value = 0x02;//MSB is record mode and LSB is vibration strength
    private ArrayList<Float> data_y = new ArrayList<Float>();
    private ArrayList<Integer> data_x = new ArrayList<Integer>();
    //-----------------------------------regarding preferences
    private boolean first_time_login = true;
    private boolean configured = false;
    private boolean record = false;
    private boolean read1 = false, read2 = false, read3 = false, read4 = false, read5 = false, read6 = false, read7 = false, read8 = false;

    private final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        battery_view = findViewById(R.id.battery);
        requestLocationPermission();
        Intent login_intent = getIntent();
        Log.i(tag, "email: "+ login_intent.getStringExtra("email")+" password: "+
                login_intent.getStringExtra("password"));
        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case NOTCONNECTED:
                        browser.loadUrl("javascript:setStatus('Not Connected');");
                        browser.loadUrl("javascript:setClassOnArduino('notconnected');");
                        browser.loadUrl("javascript:setActuating('notconnected');");
                        break;
                    case SEARCHING:
                        browser.loadUrl("javascript:setStatus('Searching');");
                        browser.loadUrl("javascript:setClassOnArduino('searching');");
                        break;
                    case FOUND:
                        browser.loadUrl("javascript:setStatus('Found');");
                        break;
                    case CONNECTED:
                        browser.loadUrl("javascript:setStatus('Connected');");
                        browser.loadUrl("javascript:setClassOnArduino('discovering');");
                        break;
                    case DISCOVERING:
                        browser.loadUrl("javascript:setStatus('Discovering');");
                        browser.loadUrl("javascript:setClassOnArduino('discovering');");
                        break;
                    case COMMUNICATING:
                        browser.loadUrl("javascript:setStatus('Communicating');");
                        browser.loadUrl("javascript:setClassOnArduino('communicating');");
                        break;
                    case RECORDING:
                        browser.loadUrl("javascript:setStatus('Recording');");
                        browser.loadUrl("javascript:setClassOnArduino('recording');");
                        break;
                    case CONFIGURE:
                        browser.loadUrl("javascript:setActuating('communicating');");
                        break;
                    case DISCONNECTING:
                        browser.loadUrl("javascript:setStatus('Disconnecting');");
                        break;
                }
            }
        };

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        browser = (WebView) this.findViewById(R.id.browser);
        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // set a webview client to override the default functionality
        browser.setWebViewClient(new wvClient());
        // get settings so we can config our WebView instance
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);
        browser.clearCache(true); // clear cache
        // this is necessary for "alert()" to work
        browser.setWebChromeClient(new WebChromeClient());
        // add our custom functionality to the javascript environment
        browser.addJavascriptInterface(new BLEUIHandler(), "bleui");
        // load a page to get things started
        browser.loadUrl("file:///android_asset/index.html");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter(); // Initializes Bluetooth adapter.

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                DialogFragment multipleChoiceDialog = new multipleChoiceDialogFragment();
//                multipleChoiceDialog.setCancelable(false);
//
//                multipleChoiceDialog.show("MultiChoice Dialog");

            }
        }, 1000);//time to wait before the pop up is coming
        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(tag, "record+vibration:" + msg_value);
            }
        }, 5000);

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.i(tag, "No BLE ??");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }


    }

    @Override
    public void onPositiveButtonClicked(String[] list, ArrayList<String> selectedItemList) {


        if (selectedItemList.contains("Record Data")) {
            record = true;
            if (selectedItemList.contains("No vibration")) {
                msg_value = 0x10;
            } else if (selectedItemList.contains("Soft vibration"))
                msg_value = 0x11;
            else if (selectedItemList.contains("Medium vibration"))
                msg_value = 0x12;
            else if (selectedItemList.contains("Strong vibration"))
                msg_value = 0x13;
        } else {
            record = false;
            if (selectedItemList.contains("No vibration")) {
                msg_value = 0x00;
            } else if (selectedItemList.contains("Soft vibration"))
                msg_value = 0x01;
            else if (selectedItemList.contains("Medium vibration"))
                msg_value = 0x02;
            else if (selectedItemList.contains("Strong vibration"))
                msg_value = 0x03;
        }


    }

    @Override
    public void onNegativeButtonClicked() {

    }


    final class wvClient extends WebViewClient {
        public void onPageFinished(WebView view, String url) {
            // when our web page is loaded, let's call a function that is contained within the page
            // this is functionally equivalent to placing an onload attribute in the <body> tag
            // whenever the loadUrl method is used, we are essentially "injecting" code into the page when it is prefixed with "javascript:"
            browser.loadUrl("javascript:startup()");
        }
    }

    // Javascript handler
    final class BLEUIHandler {
        @JavascriptInterface
        public void interrogate() {

            Log.i("BLEUI", "Initialize Scan");
            mHandler.sendEmptyMessage(SEARCHING);
            bluetoothAdapter.getBluetoothLeScanner().startScan(new BLEFoundDevice(INTERROGATE));
        }

        @JavascriptInterface
        public void configure() {
            Log.i("BLEUI", "Initialize Scan");
            mHandler.sendEmptyMessage(SEARCHING);
            bluetoothAdapter.getBluetoothLeScanner().startScan(new BLEFoundDevice(CONFIGURE));
        }
    }


    final class BLERemoteDevice extends BluetoothGattCallback {
        private final String tag = "BLEDEVICE";
        UUID serviceWeWant = new UUID(0x0000FA0100001000L, 0x800000805f9b34fbL);
        UUID batteryUUID = new UUID(0x0000210100001000L, 0x800000805f9b34fbL);
        UUID testUUID = new UUID(0x0000210200001000L, 0x800000805f9b34fbL);
        UUID data1UUID = new UUID(0x0000310100001000L, 0x800000805f9b34fbL);
        UUID data2UUID = new UUID(0x0000310200001000L, 0x800000805f9b34fbL);
        UUID data3UUID = new UUID(0x0000310300001000L, 0x800000805f9b34fbL);
        UUID data4UUID = new UUID(0x0000310400001000L, 0x800000805f9b34fbL);
        UUID data5UUID = new UUID(0x0000310500001000L, 0x800000805f9b34fbL);
        UUID data6UUID = new UUID(0x0000310600001000L, 0x800000805f9b34fbL);
        UUID data7UUID = new UUID(0x0000310700001000L, 0x800000805f9b34fbL);
        UUID data8UUID = new UUID(0x0000310800001000L, 0x800000805f9b34fbL);


        byte msgValue[] = {msg_value};

        Queue<BLEQueueItem> taskQ = new LinkedList<BLEQueueItem>();
        private int mode = INTERROGATE;

        BLERemoteDevice(int mode) {
            this.mode = mode;
        }

        private void doNextThing(BluetoothGatt gatt) {
            Log.i(tag, "doNextThing");
            try {
                BLEQueueItem thisTask = taskQ.poll();
                if (thisTask != null) {
                    Log.i(tag, "processing " + thisTask.toString());
                    switch (thisTask.getAction()) {
                        case BLEQueueItem.READCHARACTERISTIC:
                            gatt.readCharacteristic((BluetoothGattCharacteristic) thisTask.getObject());
                            break;
                        case BLEQueueItem.WRITECHARACTERISTIC:
                            Log.i(tag, "Write out this Characteristic");
                            mHandler.sendEmptyMessage(CONFIGURE);
                            BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) thisTask.getObject();
                            Log.i(tag, "Value to be written is [" + c.getStringValue(0) + "]");
                            // c.setValue("U");
                            gatt.writeCharacteristic(c);
                            break;
                        case BLEQueueItem.READDESCRIPTOR:
                            gatt.readDescriptor((BluetoothGattDescriptor) thisTask.getObject());
                            break;
                        case BLEQueueItem.DISCONNECT:
                            Log.i(tag, "data_y   :" + data_y.toString());
                            Log.i(tag, "data_x   :" + data_x.toString());
                            mHandler.sendEmptyMessage(DISCONNECTING);
                            gatt.disconnect();
                            break;
                        case BLEQueueItem.RECORDING:
                            Thread.sleep(5000);
                            gatt.readCharacteristic((BluetoothGattCharacteristic) thisTask.getObject());
                            break;
                    }
                } else {
                    Log.i(tag, "no more tasks, peace out");
                }
            } catch (Exception e) {
                Log.i(tag, "Error in doNextThing " + e.getMessage());
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(tag, "onConnectionStatChange [" + status + "][" + newState + "]");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.i(tag, "Connected to [" + gatt.toString() + "]");
                    mHandler.sendEmptyMessage(DISCOVERING);
                    gatt.discoverServices();
                } else if (status == BluetoothGatt.STATE_DISCONNECTED) {
                    mHandler.sendEmptyMessage((NOTCONNECTED));
                }
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(tag, "OnServiceDiscovered [" + status + "] " + gatt.toString());
            if (mode == INTERROGATE) {
                List<BluetoothGattService> services = gatt.getServices();
                //int size = services.size();
                //Log.i(tag,"here is the size"+size );

                for (int i = 0; i < services.size(); i++) {
                    Log.i(tag, "service [" + i + "] is [" + services.get(i).getUuid().toString() + "]");
                    if (serviceWeWant.equals(services.get(i).getUuid())) {
                        Log.i(tag, "************COOL, found it!!!");
                    }
                    UUID serviceUUID = services.get(i).getUuid();
                    List<BluetoothGattCharacteristic> schars = services.get(i).getCharacteristics();
                    for (int j = 0; j < schars.size(); j++) {
                        Log.i(tag, "characteristic [" + j + "] [" + schars.get(j).getUuid() + "] properties [" + schars.get(j).getProperties() + "]");
                        if ((schars.get(j).getProperties() & 2) == 2) {
                            Log.i(tag, "We said: we read this:");
                            taskQ.add(new BLEQueueItem(BLEQueueItem.READCHARACTERISTIC, schars.get(j).getUuid(), "Read Characteristic of Available Service", schars.get(j)));
                        } else {
                            Log.i(tag, "This Characteristic cannot be Read");
                        }
                        List<BluetoothGattDescriptor> scdesc = schars.get(j).getDescriptors();
                        for (int k = 0; k < scdesc.size(); k++) {
                            Log.i(tag, "Descriptor [" + k + "] [" + scdesc.get(k).toString() + "]");
                            Log.i(tag, "Descriptor UUID [" + scdesc.get(k).getUuid() + "]");
                            Log.i(tag, "Descriptor Permissions [" + scdesc.get(k).getPermissions() + "]");
                            //Log.i(tag,"Attempting to read this Descriptor");
                            taskQ.add(new BLEQueueItem(BLEQueueItem.READDESCRIPTOR, scdesc.get(k).getUuid(), "Read Descriptor of Characteristic", scdesc.get(k)));
                        }
                    }
                }
            }

            if (mode == CONFIGURE) {
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                if (ourBLEService != null) {
                    Log.i(tag, "Got it, woo hoo!!!");
                    BluetoothGattCharacteristic setRecordMode = ourBLEService.getCharacteristic(testUUID);
                    if (setRecordMode != null) {
                        Log.i(tag, "starting send record");
                        //Log.i(tag, "value of record is [" + setRecordMode.getStringValue(0) + "]");
                        setRecordMode.setValue(msgValue);
                        Log.i(tag, "value of record to be written [" + setRecordMode.getStringValue(0) + "]");
                        taskQ.add(new BLEQueueItem(BLEQueueItem.WRITECHARACTERISTIC, setRecordMode.getUuid(), "Write Characteristic to record mode", setRecordMode));

                    } else {
                        Log.i(tag, "No button");
                    }

                    BluetoothGattCharacteristic getBattery = ourBLEService.getCharacteristic(batteryUUID);
                    if (getBattery != null) {

                        Log.i(tag, "value is [" + getBattery.getStringValue(0) + "]");
                        //battery = getBattery.getValue().toString();
                        taskQ.add(new BLEQueueItem(BLEQueueItem.READCHARACTERISTIC, getBattery.getUuid(), "Read battery", getBattery));
                        if (record) {
                            BluetoothGattCharacteristic getData1 = ourBLEService.getCharacteristic(data1UUID);
                            taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData1.getUuid(), "Read data1", getData1));
                            Log.i(tag, "dataaaaa");
                        }
                    } else {
                        Log.i(tag, "No button");
                    }

                } else {
                    Log.i(tag, "No Service");
                }
            }
            Log.i(tag, "OK, let's go^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            //taskQ.add(new BLEQueueItem(BLEQueueItem.DISCONNECT, new UUID(0, 0), "Disconnect", null));

            if (record) {
                mHandler.sendEmptyMessage(RECORDING);

            } else {
                mHandler.sendEmptyMessage(COMMUNICATING);
            }
            doNextThing(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(tag, "characteristic written [" + status + "]");
            if (characteristic.getUuid().equals(testUUID)) {
                Log.i(tag, "value is [" + characteristic.getStringValue(0) + "]");
                if (characteristic.getStringValue(0).equals(("U"))) {
                    Log.i(tag, "We're done here!");
                }
            }
            doNextThing(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(tag, "onCharacteristicChanged " + characteristic.getUuid());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (characteristic.getUuid().equals(batteryUUID)) {
                Log.i(tag, "characteristic read [" + characteristic.getUuid() + "] [" + characteristic.getStringValue(0) + "]");
                int bat = characteristic.getValue()[0];
                Log.i(tag, "batty read " + characteristic.getValue().length + "value is" + bat);
                battery_view.setText("Battery: " + String.valueOf(bat) + "%");

            }
            if (characteristic.getUuid().equals(data1UUID) && !read1) { //read the data
                Log.i(tag, "started writing the data1" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read1 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                BluetoothGattCharacteristic getData2 = ourBLEService.getCharacteristic(data2UUID);
                taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData2.getUuid(), "Read data2", getData2));

            }
            if (characteristic.getUuid().equals(data2UUID) && !read2) { //read the data
                Log.i(tag, "started writing the data2" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read2 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                BluetoothGattCharacteristic getData3 = ourBLEService.getCharacteristic(data3UUID);
                taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData3.getUuid(), "Read data3", getData3));

            }
            if (characteristic.getUuid().equals(data3UUID) && !read3) { //read the data
                Log.i(tag, "started writing the data3" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read3 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                BluetoothGattCharacteristic getData4 = ourBLEService.getCharacteristic(data4UUID);
                taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData4.getUuid(), "Read data4", getData4));

            }
            if (characteristic.getUuid().equals(data4UUID) && !read4) { //read the data
                Log.i(tag, "started writing the data4" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read4 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                BluetoothGattCharacteristic getData5 = ourBLEService.getCharacteristic(data5UUID);
                taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData5.getUuid(), "Read data5", getData5));

            }
            if (characteristic.getUuid().equals(data5UUID) && !read5) { //read the data
                Log.i(tag, "started writing the data5" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read5 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                BluetoothGattCharacteristic getData6 = ourBLEService.getCharacteristic(data6UUID);
                taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData6.getUuid(), "Read data6", getData6));

            }
            if (characteristic.getUuid().equals(data6UUID) && !read6) { //read the data
                Log.i(tag, "started writing the data6" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read6 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                BluetoothGattCharacteristic getData7 = ourBLEService.getCharacteristic(data7UUID);
                taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData7.getUuid(), "Read data7", getData7));

            }
            if (characteristic.getUuid().equals(data7UUID) && !read7) { //read the data
                Log.i(tag, "started writing the data7" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read7 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                BluetoothGattCharacteristic getData8 = ourBLEService.getCharacteristic(data8UUID);
                taskQ.add(new BLEQueueItem(BLEQueueItem.RECORDING, getData8.getUuid(), "Read data8", getData8));

            }
            if (characteristic.getUuid().equals(data8UUID) && !read8) { //read the data
                Log.i(tag, "started writing the data8" + characteristic.getValue().toString());
                for (int i = 0; i < 64; i++) { //read 64 pairs of float,unsigned long
                    Float ang_vel = new Float(ByteBuffer.wrap(characteristic.getValue(), i * 8, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getFloat());
                    data_y.add(ang_vel);
                    Integer time = new Integer(ByteBuffer.wrap(characteristic.getValue(), i * 8 + 4, 4).order
                            (ByteOrder.LITTLE_ENDIAN).getInt());
                    data_x.add(time);
                }
                read8 = true;
                BluetoothGattService ourBLEService = gatt.getService(serviceWeWant);
                mHandler.sendEmptyMessage(DISCONNECTING);
                taskQ.add(new BLEQueueItem(BLEQueueItem.DISCONNECT, new UUID(0, 0), "Disconnect", null));

            }

            doNextThing(gatt);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            try {
                Log.i(tag, "onDescriptorRead status is [" + status + "]");
                Log.i(tag, "descriptor read [" + descriptor.getCharacteristic().getUuid() + "]");
                Log.i(tag, "descriptor value is [" + new String(descriptor.getValue(), "UTF-8") + "]");
                doNextThing(gatt);
            } catch (Exception e) {
                Log.e(tag, "Error reading descriptor " + e.getStackTrace());
                doNextThing(gatt);
            }
        }

        public void ReadData(UUID uuid_to_read_from) {


        }

    }

    final class BLEFoundDevice extends ScanCallback {
        private final String tag = "BLEDEVICE";
        private int mode = INTERROGATE;

        BLEFoundDevice(int mode) {
            this.mode = mode;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.i(tag,"Found a device ==> " + result.toString());
            ScanRecord sr = result.getScanRecord();
            if (sr != null) {
                if (sr.getDeviceName() != null) {
                    if (sr.getDeviceName().equals("BLE Garage Opener")) {
                        bluetoothAdapter.getBluetoothLeScanner().stopScan(this);
                        mHandler.sendEmptyMessage(FOUND);
                        Log.i(tag, "Found our Garage Door Opener!");
                        BluetoothDevice remoteDevice = result.getDevice();
                        if (remoteDevice != null) {
                            String nameOfDevice = result.getDevice().getName();
                            if (nameOfDevice != null) {
                                Log.i(tag, "device is [" + nameOfDevice + "]");
                            }
                        }
                        Log.i(tag, "Advertise Flags [" + sr.getAdvertiseFlags() + "]");
                        List<ParcelUuid> solicitationInfo = sr.getServiceUuids();
                        for (int i = 0; i < solicitationInfo.size(); i++) {
                            ParcelUuid thisone = solicitationInfo.get(i);
                            Log.i(tag, "solicitationinfo [" + i + "] uuid [" + thisone.getUuid() + "]");
                        }
                        ParcelUuid[] services = remoteDevice.getUuids();
                        if (services != null) {
                            Log.i(tag, "length of services is [" + services.length + "]");
                        }
                        // attempt to connect here
                        remoteDevice.connectGatt(getApplicationContext(), true, new BLERemoteDevice(mode));
                        Log.i(tag, "after connect GATT");
                    } else {
                        Log.i(tag, "Not for us [" + sr.getDeviceName() + "]");
                    }
                }
            } else {
                Log.i(tag, "Null ScanRecord??");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(tag, "Error Scanning [" + errorCode + "]");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i(tag, "onBatchScanResults " + results.size());
            for (int i = 0; i < results.size(); i++) {
                Log.i(tag, "Result [" + i + "]" + results.get(i).toString());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast t = Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT);
            t.show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }
}

