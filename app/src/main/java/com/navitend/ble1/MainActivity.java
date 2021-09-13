package com.navitend.ble1;

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
import android.os.ParcelUuid;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements multipleChoiceDialogFragment.onMultiChoiceListener {
    private final String tag = "ble1";
    private WebView browser = null;
    private Handler mHandler = null;
    private int howmany = 0;
    private BluetoothAdapter bluetoothAdapter;

    private TextView battery_view;
    private final int NOTCONNECTED = 0;
    private final int SEARCHING = 1;
    private final int FOUND = 2;
    private final int CONNECTED = 3;
    private final int DISCOVERING = 4;
    private final int COMMUNICATING = 5;
    private final int CONFIGURE = 6;
    private final int DISCONNECTING = 7;
    private final int INTERROGATE = 8;
    private final int READTEST = 9;
    private Byte msg_value = 0x02;
    private String battery = "100";
    //private ArrayList<Float> data = new ArrayList<Float>();




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        battery_view = findViewById(R.id.battery);
        mHandler = new Handler() {
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
                    case CONFIGURE:
                        browser.loadUrl("javascript:setActuating('communicating');");
                        break;
                    case DISCONNECTING:
                        browser.loadUrl("javascript:setStatus('Disconnecting');");
                        break;
                }
            }
        };


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

        // JavaScript?  Of course!
        settings.setJavaScriptEnabled(true);

        // clear cache
        browser.clearCache(true);

        // this is necessary for "alert()" to work
        browser.setWebChromeClient(new WebChromeClient());

        // add our custom functionality to the javascript environment
        browser.addJavascriptInterface(new BLEUIHandler(), "bleui");

        // load a page to get things started
        browser.loadUrl("file:///android_asset/index.html");

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DialogFragment multipleChoiceDialog = new multipleChoiceDialogFragment();
                multipleChoiceDialog.setCancelable(false);

                multipleChoiceDialog.show(getSupportFragmentManager(), "MultiChoice Dialog");

            }
        }, 1000);//time to wait before the pop up is coming
        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(tag, "record+vibration:"+msg_value);
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



        if (selectedItemList.contains("Record Data")){
            if (selectedItemList.contains("No vibration")) {
                msg_value = 0x10;
            } else if (selectedItemList.contains("Soft vibration"))
                msg_value = 0x11;
            else if (selectedItemList.contains("Medium vibration"))
                msg_value = 0x12;
            else if (selectedItemList.contains("Strong vibration"))
                msg_value = 0x13;
        }
        else{
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
        UUID dataUUID = new UUID(0x0000210400001000L, 0x800000805f9b34fbL);


        byte msgValue []={msg_value};

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
                            mHandler.sendEmptyMessage(DISCONNECTING);
                            gatt.disconnect();
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
                            Log.i(tag, "We said: we read this:" );
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
                        //gatt.writeCharacteristic(setRecordMode);

                    } else {
                        Log.i(tag, "No button");
                    }

                } else {
                    Log.i(tag, "No Service");
                }
            }
            Log.i(tag, "OK, let's go^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            //taskQ.add(new BLEQueueItem(BLEQueueItem.DISCONNECT, new UUID(0, 0), "Disconnect", null));
            mHandler.sendEmptyMessage(COMMUNICATING);
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
            if(characteristic.getUuid().equals(batteryUUID)) {
                Log.i(tag, "characteristic read [" + characteristic.getUuid() + "] [" + characteristic.getStringValue(0) + "]");
                int bat = characteristic.getValue()[0];
                Log.i(tag, "batty read " + characteristic.getValue().length + "value is" + bat);
                battery_view.setText("Battery: "+String.valueOf(bat)+"%");
                doNextThing(gatt);
            }
//            if(characteristic.getUuid().equals(dataUUID)) { //read the data
//                Log.i(tag, "characteristic read [" + characteristic.getUuid() + "] [" + characteristic.getStringValue(0) + "]");
//                String s = "";
//                for(int j = 0; j<16; j++) {
//                    for (int i = 0; i < 4; i++) {
//                        s += characteristic.getValue()[i + j * 16];
//                    }
//                    Float ang_vel = new Float(Float.parseFloat(s));
//                    s="";
//                    data.add(ang_vel);
//                    for (int i = 4; i < 8; i++) {
//                        s += characteristic.getValue()[i+j*16];
//                    }
//                    Float time = new Float(new Float(Float.parseFloat(s)));
//                    s="";
//                    data.add(time);
//                }
//                Log.i(tag, data.toString());
//            }

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
}
