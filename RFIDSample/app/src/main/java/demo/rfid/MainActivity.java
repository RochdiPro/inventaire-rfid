package demo.rfid;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import device.common.rfid.RecvPacket;
import device.common.rfid.RFIDCallback;
import device.common.rfid.RFIDConst;
import device.common.rfid.ParamOfInvent;
import device.common.rfid.ModeOfInvent;
import device.common.rfid.TxCycle;
import device.common.rfid.CustomIntentConfig;
import device.sdk.RFIDManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener  {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String TAG = "RFID_Demo.MainActivity";
    public static final String CUSTOM_INTENT = "Custom.Intent.Test";
    Button buttonConnectToDevice;
    Button buttonSearchDevice;
    Button buttonOpen;
    Button buttonConnectedDevice;
    Button buttonScanRFID;
    Button buttonScanDelete;
    TextView textViewConnectedDevice;
    TextView textViewTotalScanCount;
    Button btnexporterdata;

    private final int MSG_COMMAND_SET_RFID_DEFAULT = 1;
    private final int MSG_COMMAND_SET_RFID_INVENTORY_PARAM = 2;
    private final int MSG_COMMAND_SET_RFID_TX_POWER = 3;
    private final int MSG_COMMAND_SET_RFID_TX_CYCLE = 4;
    private final int MSG_COMMAND_SET_RFID_PREFIX = 5;
    private final int MSG_COMMAND_SET_RFID_SUFFIX = 6;
    private final int MSG_COMMAND_SET_RFID_TX_DATA_FORMAT = 7;
    private final int MSG_COMMAND_SET_RFID_RESULT_TYPE = 8;
    private final int MSG_COMMAND_SET_RFID_INVENTORY_MODE = 9;

    private final int DEVICE_BT = 1;
    private final int DEVICE_USB = 2;
    private final int DEVICE_UART = 3;
    private int mConnectedDevice = DEVICE_BT;

    private static RFIDManager rfidManager;
    private static ParamOfInvent paramOfInvent;
    private static ModeOfInvent modeOfInvent;
    private static TxCycle txCycle;
    private static CustomIntentConfig customIntentConfig;

    static String connectedDeviceMacAddress = "-";
    static String deviceName = "";
    static boolean deviceConnected = false;
    boolean isRfidRunning = false;
    boolean isOpened = false;
    ProgressDialog progressDialog;
    Handler dialogHandler;

    BaseAdapter_Inventory_ListView baseAdapterInventoryListView;
    ListView inventoryListView;

    Handler handler = new Handler(new IncomingHandlerCallback());

    class IncomingHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            int iErr;
            Log.i(TAG, "handleMessage" +msg );
            switch (msg.what) {
                case MSG_COMMAND_SET_RFID_DEFAULT:
                    iErr = rfidManager.SetDefaultConfig();
                    if (iErr != RFIDConst.CommandErr.SUCCESS) {
                        Log.e(TAG, "SetDefaultConfig() is failed : "+iErr);
                    }
                    break;
                case MSG_COMMAND_SET_RFID_INVENTORY_PARAM:
                    paramOfInvent.session = 1;
                    paramOfInvent.q = 5;
                    paramOfInvent.inventoryFlag = 2;
                    iErr = rfidManager.SetInventoryParam(paramOfInvent);
                    if (iErr != RFIDConst.CommandErr.SUCCESS) {
                        Log.e(TAG, "SetInventoryParam() is failed : "+iErr);
                    }
                    break;
                case MSG_COMMAND_SET_RFID_TX_POWER:
                    iErr = rfidManager.SetTxPower(0);
                    if (iErr != RFIDConst.CommandErr.SUCCESS) {
                        Log.e(TAG, "SetTxPower() is failed : "+iErr);
                    }
                    Log.d(TAG, "GetTxPower() : " + rfidManager.GetTxPower());
                    Log.d(TAG, "GetOemInfo() : " + rfidManager.GetOemInfo());
                    break;
                case MSG_COMMAND_SET_RFID_TX_CYCLE:
                    txCycle.onTime = 100;
                    txCycle.offTime = 10;
                    iErr = rfidManager.SetTxCycle(txCycle);
                    if (iErr != RFIDConst.CommandErr.SUCCESS) {
                        Log.e(TAG, "SetTxCycle() is failed : "+iErr);
                    }
                    break;
                case MSG_COMMAND_SET_RFID_PREFIX:
                    String prefix = "Prefix_";
                    //String prefix = "";
                    /*iErr = rfidManager.SetPrefix(prefix);
                    if (iErr != RFIDConst.CommandErr.SUCCESS) {
                        Log.e(TAG, "SetPrefix() is failed : "+iErr);
                    }*/
                    break;
                case MSG_COMMAND_SET_RFID_SUFFIX:
                    String suffix = "_Suffix";
                    //String suffix = "";
                    /*iErr = rfidManager.SetSuffix(suffix);
                    if (iErr != RFIDConst.CommandErr.SUCCESS) {
                        Log.e(TAG, "SetSuffix1() is failed : "+iErr);
                    }*/
                    break;
                case MSG_COMMAND_SET_RFID_TX_DATA_FORMAT:
                    /*
                     * public static final int TX_FORMAT_TAG_DATA = 0;
                     * public static final int TX_FORMAT_PREFIX_TAG_DATA = 1;
                     * public static final int TX_FORMAT_TAG_DATA_SUFFIX = 2;
                     * public static final int TX_FORMAT_PREFIX_TAG_DATA_SUFFIX = 3;
                     */
                    iErr = rfidManager.SetTxDataFormat(RFIDConst.RFIDConfig.TX_FORMAT_TAG_DATA);
                    if (iErr != RFIDConst.CommandErr.SUCCESS) {
                        Log.e(TAG, "SetTxDataFormat() is failed : "+iErr);
                    }
                    Log.d(TAG, "Data format : " + rfidManager.GetDataFormat());
                    break;
                case MSG_COMMAND_SET_RFID_RESULT_TYPE:
                    /*
                     * public static final int RFID_RESULT_CALLBACK = 0;
                     * public static final int RFID_RESULT_KBDMSG = 1;
                     * public static final int RFID_RESULT_COPYPASTE = 2;
                     * public static final int RFID_RESULT_USERMSG = 3;
                     * public static final int RFID_RESULT_EVENT = 4;
                     * public static final int RFID_RESULT_CUSTOM_INTENT = 5;
                     */
                    rfidManager.SetResultType(RFIDConst.ResultType.RFID_RESULT_CALLBACK);
                    //rfidManager.SetResultType(RFIDConst.ResultType.RFID_RESULT_CUSTOM_INTENT);
                    break;
                case MSG_COMMAND_SET_RFID_INVENTORY_MODE:
                    modeOfInvent.single = 0;
                    modeOfInvent.select = 0;
                    modeOfInvent.timeout = 0;
                    rfidManager.SetOperationMode(modeOfInvent);
                    Log.d(TAG, "Device name : " + rfidManager.GetBtDevice());
                    break;
            }
            return true;
        }
    }

    Handler mhandler = new Handler();
    private RFIDCallback mRFIDCallback = new RFIDCallback(mhandler) {
        public void onNotifyReceivedPacket(RecvPacket recvPacket){
            Log.i("RFID_callbacks","onNotifyReceivedPacket");
            if (!isConnected()) {
                Log.i("MainActivity", "Device not connected.");
                return;
            }

            addScanData(recvPacket.RecvString);
        };

        public void onNotifyDataWriteFail()
        {
            Log.i("RFID_callbacks","onNotifyDataWriteFail");

        };

        public void onNotifyChangedState(int state)
        {
            Log.i("RFID_callbacks","onNotifyChangedState");
            switch(state) {
                case 1: // RFIDConst.DeviceState.BT_CONNECTED:
                    Log.i("RFID_callbacks","onNotifyChangedState BT_CONNECTED : ["+state+"]");
                    break;
                case 2: // RFIDConst.DeviceState.BT_DISCONNECTED:
                    Log.i("RFID_callbacks","onNotifyChangedState BT_CONNECT_FAILED : ["+state+"]");
                    break;
                case 3: // RFIDConst.DeviceState.BT_OPENED:
                    Log.i("RFID_callbacks","onNotifyChangedState BT_OPENED : ["+state+"]");
                    break;
                case 4: // RFIDConst.DeviceState.BT_CLOSED:
                    Log.i("RFID_callbacks","onNotifyChangedState BT_CLOSED : ["+state+"]");
                    break;
                case 5: // RFIDConst.DeviceState.USB_OPENED:
                    Log.i("RFID_callbacks","onNotifyChangedState USB_OPENED : ["+state+"]");
                    break;
                case 6: // RFIDConst.DeviceState.USB_CLOSED:
                    Log.i("RFID_callbacks","onNotifyChangedState USB_CLOSED : ["+state+"]");
                    break;
                case 7: // RFIDConst.DeviceState.UART_OPENED:
                    Log.i("RFID_callbacks","onNotifyChangedState UART_OPENED : ["+state+"]");
                    break;
                case 8: // RFIDConst.DeviceState.UART_CLOSED:
                    Log.i("RFID_callbacks","onNotifyChangedState UART_CLOSED : ["+state+"]");
                    break;
                case 9: // RFIDConst.DeviceState.TRIGGER_MODE_RFID:
                    Log.i("RFID_callbacks","onNotifyChangedState TRIGGER_MODE_RFID : ["+state+"]");
                    break;
                case 10: // RFIDConst.DeviceState.TRIGGER_MODE_SCAN:
                    Log.i("RFID_callbacks","onNotifyChangedState TRIGGER_MODE_SCAN : ["+state+"]");
                    break;
                case 11: // RFIDConst.DeviceState.TRIGGER_RFID_KEYDOWN:
                    Log.i("RFID_callbacks","onNotifyChangedState TRIGGER_RFID_KEYDOWN : ["+state+"]");
                    startRfidScan();
                    break;
                case 12: // RFIDConst.DeviceState.TRIGGER_RFID_KEYUP:
                    Log.i("RFID_callbacks","onNotifyChangedState TRIGGER_RFID_KEYUP : ["+state+"]");
                    stopRfidScan();
                    break;
                case 13: // RFIDConst.DeviceState.TRIGGER_SCAN_KEYDOWN:
                    Log.i("RFID_callbacks","onNotifyChangedState TRIGGER_SCAN_KEYDOWN : ["+state+"]");
                    break;
                case 14: // RFIDConst.DeviceState.TRIGGER_SCAN_KEYUP:
                    Log.i("RFID_callbacks","onNotifyChangedState TRIGGER_SCAN_KEYUP : ["+state+"]");
                    break;
                case 15: // RFIDConst.DeviceState.LOW_BATT:
                    Log.i("RFID_callbacks","onNotifyChangedState LOW_BATT : ["+state+"]");
                    break;
                case 16: // RFIDConst.DeviceState.POWER_OFF:
                    Log.i("RFID_callbacks","onNotifyChangedState POWER_OFF : ["+state+"]");
                    break;
                default:
                    break;
            }
        };
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControls();

        dialogHandler = new Handler();
        rfidManager = new RFIDManager();
        paramOfInvent = new ParamOfInvent();
        modeOfInvent = new ModeOfInvent();
        txCycle = new TxCycle();
        customIntentConfig = new CustomIntentConfig();

        rfidManager.RegisterRFIDCallback(mRFIDCallback);

        customIntentConfig.action = CUSTOM_INTENT;
        customIntentConfig.category = "RFID";
        customIntentConfig.extraRfidData = "DATA";
        rfidManager.SetCustomIntentConfig(customIntentConfig);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(RFIDConst.ResultType.INTENT_EVENT);
        registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(CUSTOM_INTENT);
        filter.addCategory(customIntentConfig.category);
        registerReceiver(broadcastReceiver, filter);

        SharedPreference sharedPreference = new SharedPreference(this);
        connectedDeviceMacAddress = sharedPreference.getValue(SharedPreference.PREF_LAST_DEVICE_MAC, "");
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i("MainActivity", "BroadcastReceiver()-Bluetooth Disconnect ");
                setDisconnect();
                updateUI();
            } else if (RFIDConst.ResultType.INTENT_EVENT.equals(action)) {
                Log.d(TAG, "get INTENT_EVENT");
                addScanData(intent.getExtras().getString(RFIDConst.ResultType.EXTRA_EVENT_RFID_DATA));
            } else if (CUSTOM_INTENT.equals(action)) {
                Log.d(TAG, "get CustomIntent");
                addScanData(intent.getExtras().getString(customIntentConfig.extraRfidData));

            }
        }
    };

    private void initControls() {
        buttonConnectToDevice = (Button) findViewById(R.id.button_device_connect);
        if (buttonConnectToDevice != null) {
            buttonConnectToDevice.setOnClickListener(this);
        }
        buttonSearchDevice = (Button) findViewById(R.id.button_device_search);
        if (buttonSearchDevice != null) {
            buttonSearchDevice.setOnClickListener(this);
        }
        buttonOpen = (Button) findViewById(R.id.button_open);
        if (buttonOpen != null) {
            buttonOpen.setOnClickListener(this);
        }
        buttonConnectedDevice = (Button) findViewById(R.id.button_connected_device);
        if (buttonConnectedDevice != null) {
            buttonConnectedDevice.setOnClickListener(this);
        }
        buttonScanRFID = (Button) findViewById(R.id.button_scan_rfid);
        if (buttonScanRFID != null) {
            buttonScanRFID.setOnClickListener(this);
        }
        buttonScanDelete = (Button) findViewById(R.id.button_scan_delete);
        if (buttonScanDelete != null) {
            buttonScanDelete.setOnClickListener(this);
        }
        textViewConnectedDevice = (TextView) findViewById(R.id.textView_connected_device);
        textViewTotalScanCount = (TextView) findViewById(R.id.textView_total_count_ea);

        baseAdapterInventoryListView = new BaseAdapter_Inventory_ListView(this);
        inventoryListView = (ListView) findViewById(R.id.listview_inventory);
        if (inventoryListView != null) {
            inventoryListView.setAdapter(baseAdapterInventoryListView);
        }
        btnexporterdata = (Button) findViewById(R.id.button_exporter);
        btnexporterdata.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String  date = df.format(Calendar.getInstance().getTime());
                //generate data
                StringBuilder data = new StringBuilder();
                data.append( "Code Tag" );
                data.append( "," );
                data.append(  "Nom" );

                for (int i  =0 ; i<baseAdapterInventoryListView.getCount(); i++) {
                    Item_Inventory u = (Item_Inventory) baseAdapterInventoryListView.getItem(i);
                    String res = u.getDataValue().substring(4, 8);
                    String res2 = u.getDataValue().substring(0, 4);
                    String art ="inconnu";
                    if(res.equals("0001"))
                    {
                        art="Article a";
                    }else if(res.equals("0002"))
                    {
                        art="Article b";
                    }else if(res.equals("0003"))
                    {
                        art="Article c";
                    }
                   // System.out.println(art+"-"+res+"-  -"+res2+"  :  "+art);
                    data.append("\n"+u.getDataValue()+","+art);
                }

                try{
                    //saving the file into device
                    FileOutputStream out = openFileOutput("inv:"+date+".csv", Context.MODE_PRIVATE);
                    out.write((data.toString()).getBytes());
                    out.close();

                    //exporting
                    Context context = getApplicationContext();
                    File filelocation = new File(getFilesDir(), "inv:"+date+".csv");
                    Uri path = FileProvider.getUriForFile(context, "com.example.exportcsv.fileprovider", filelocation);
                    Intent fileIntent = new Intent(Intent.ACTION_SEND);
                    fileIntent.setType("text/csv");
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "inv:"+date);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(fileIntent, "Send mail"));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                btnexporterdata.setBackgroundResource(R.drawable.ic_exporter_foreground);

            }
        });


    }


    private void deviceConfigSetting() {
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_DEFAULT, 0, 0, null), 200);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_INVENTORY_PARAM, 0, 0, null), 400);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_TX_POWER, 0, 0, null), 600);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_TX_CYCLE, 0, 0, null), 800);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_PREFIX, 0, 0, null), 1000);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_SUFFIX, 0, 0, null), 1200);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_TX_DATA_FORMAT, 0, 0, null), 1400);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_RESULT_TYPE, 0, 0, null), 1600);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_COMMAND_SET_RFID_INVENTORY_MODE, 0, 0, null), 1800);
    }

    static public String getConnetedDeviceMacAddress() {
        return connectedDeviceMacAddress;
    }

    static public boolean isConnected() {
        return deviceConnected;
    }

    static public void setConnect(String macAddress, String deviceName) {
       deviceConnected = true;
        connectedDeviceMacAddress = macAddress;
        rfidManager.ConnectBTDevice(macAddress, deviceName);
    }

    static public void setDisconnect() {
       deviceConnected = false;
       //rfidManager.Close();
       rfidManager.DisconnectBTDevice();
    }

    private void runDeviceSearchActivity() {
        Intent intent;
        intent = new Intent(getApplicationContext(), DeviceSearchActivity.class);
        startActivity(intent);
    }

    private void updateUI() {
        StringBuilder stringBuilder = new StringBuilder();
        if (isConnected()) {
            stringBuilder.append("Appareil : ");
            stringBuilder.append(connectedDeviceMacAddress);
            textViewConnectedDevice.setText(stringBuilder);
          //  buttonConnectToDevice.setText("DISCONNECT");

            buttonConnectToDevice.setBackgroundResource(R.drawable.ic_deconnecter_foreground);
        } else {
            stringBuilder.append("Appareil : -");
            textViewConnectedDevice.setText(stringBuilder);
         //   buttonConnectToDevice.setText("CONNECT");
        }
    }

    private void addScanData(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int count = 1;
                Item_Inventory item;
                if (data != null) {
                    item = new Item_Inventory(
                            isRfidRunning ? "RFID" : "CONFIG",
                            data,
                            count
                    );
                    baseAdapterInventoryListView.addInventoryItem(item);
                    updateData();
                }
            }
        });
    }

    private void clearScanData() {
        baseAdapterInventoryListView.clear();
        updateData();
    }

    private void updateData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseAdapterInventoryListView.getCount());
        stringBuilder.append("");
        textViewTotalScanCount.setText(stringBuilder);
        baseAdapterInventoryListView.notifyDataSetChanged();
    }

    private void showWaitDialog() {
        progressDialog = ProgressDialog.show(
                MainActivity.this,
                "Connexion de l'appareil: " + connectedDeviceMacAddress,
                "S'il vous plaît, attendez.");
        dialogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeWaitDialog();
            }
        }, 5000);
    }

    private void closeWaitDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            dialogHandler.removeCallbacksAndMessages(null);
            updateUI();

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateUI();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_device_search:
                runDeviceSearchActivity();
                break;
            case R.id.button_device_connect:
                if (isConnected()) {
                    if(isRfidRunning == true)
                        Toast.makeText(this, getString(R.string.string_rfid_stop), Toast.LENGTH_SHORT).show();
                    else
                        setDisconnect();
                } else {
                    if (connectedDeviceMacAddress != null) {
                        showWaitDialog();
                        Log.i("MainActivity", "connectedDeviceMacAddress : ["+connectedDeviceMacAddress+"]");
                        setConnect(connectedDeviceMacAddress, deviceName);
                    }
                }
                updateUI();
                break;
            case R.id.button_open:
                if (isOpened) {
                    rfidManager.Close();
                    if (mConnectedDevice != DEVICE_BT) {
                        deviceConnected = false;
                    }
                    isOpened = false;
                 //   setTextOpenCloseButton("Open");
                    buttonOpen.setBackgroundResource(R.drawable.ic_open_foreground);

                } else {
                    if (RFIDConst.CommandErr.SUCCESS == rfidManager.Open(mConnectedDevice)) {
                        if (mConnectedDevice != DEVICE_BT) {
                            deviceConnected = true;
                        }
                        isOpened = true;
                    //    setTextOpenCloseButton("Close");
                        buttonOpen.setBackgroundResource(R.drawable.ic_deconnecter_foreground);

                        deviceConfigSetting();
                    } else {
                        Log.e(TAG, "Open failed!!!");
                    }
                }
                //updateUI();
                break;
            case R.id.button_connected_device:
                if (mConnectedDevice == DEVICE_BT) {
                    setTextConnectedDevice("USB");
                    mConnectedDevice = DEVICE_USB;
                    buttonConnectToDevice.setVisibility(View.INVISIBLE);
                    buttonSearchDevice.setVisibility(View.INVISIBLE);
                } else if (mConnectedDevice == DEVICE_USB) {
                    setTextConnectedDevice("UART");
                    mConnectedDevice = DEVICE_UART;
                    buttonConnectToDevice.setVisibility(View.INVISIBLE);
                    buttonSearchDevice.setVisibility(View.INVISIBLE);
                } else {
                    setTextConnectedDevice("BT");
                    mConnectedDevice = DEVICE_BT;
                    buttonConnectToDevice.setVisibility(View.VISIBLE);
                    buttonSearchDevice.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.button_scan_rfid:
                if (isConnected()) {
                    if (isRfidRunning) {
                        stopRfidScan();
                    } else {
                        startRfidScan();
                    }
                }
                break;
            case R.id.button_scan_delete:
                clearScanData();
                buttonScanDelete.setBackgroundResource(R.drawable.ic_delete_foreground);

                break;
        }
    }

    private void startRfidScan() {
        if (!isRfidRunning) {
            isRfidRunning = true;
            //rfidManager.StartInventory_ext(1, 0, 0);
            rfidManager.StartInventory();

            // for Tag functions test
            //rfidManager.ReadTag(1, 2, 0, "0");
            //rfidManager.SetBtDefault();
            //rfidManager.WildcardSearch(5, "3000*");
            //rfidManager.SingleSearch(28, "30003000a0100000000000000039", 9, 3);
            //30003000a0100000000000000039
            //rfidManager.SingleSearch(24, "3000e2005024990200362230", 9, 3);
            //3000e2005024990200362230
            //rfidManager.GetSearchList();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //setTextRFIDButton("STOP");
                    buttonScanRFID.setBackgroundResource(R.drawable.ic_close_rfid_foreground);
                }
            });
        }
    }

    private void stopRfidScan() {
        if (isRfidRunning) {
            isRfidRunning = false;
            rfidManager.Stop();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // setTextRFIDButton("RFID");
                    buttonScanRFID.setBackgroundResource(R.drawable.ic_rfid_foreground);
                }
            });
        }
    }



    private void setTextRFIDButton(String text) {
        buttonScanRFID.setText(text);
    }

    private void setTextOpenCloseButton(String text) {
        buttonOpen.setText(text);
    }

    private void setTextConnectedDevice(String text) {
        buttonConnectedDevice.setText(text);
    }

    private void showYesNoDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Êtes-vous sûr de vouloir quitter l'application ?").setCancelable(
                false).setPositiveButton("OUI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isConnected()) {
                            setDisconnect();
                            unregisterReceiver(broadcastReceiver);
                        }
                        finish();
                    }
                }).setNegativeButton("NON",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialog.create();
        alert.setTitle("Quitter l'application");
        alert.show();
    }

    @Override
    public void onBackPressed() {
        showYesNoDialog();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }
}

