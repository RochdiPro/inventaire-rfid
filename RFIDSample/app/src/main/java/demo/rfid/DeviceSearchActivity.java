package demo.rfid;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by NG on 2016-12-14.
 */

public class DeviceSearchActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    final int REQUEST_COARSE_LOCATION_PERMISSIONS = 201;

    final String DOTR3000_UHF_READER = "DOTR3";
    final String RF800_UHF_READER = "RF";

/*Added by
 171101, others device is searching test*/
    final String KEY_NAME = "name";
    final String KEY_SUMMARY = "summary";
    final String KEY_ADDRESS = "address";

    static BluetoothAdapter bluetoothAdapter;

    Button buttonDeviceScan;
    ListView listViewDeviceList;
    ArrayList<HashMap<String, String>> arrayListDevice;

    BaseAdapter baseAdapterDevice;
    ProgressDialog progressDialog;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);

        initControls();
        getDiscoveryPermission();
    }

    private void initControls() {
        buttonDeviceScan = (Button) findViewById(R.id.button_devicesearch_search);
        if (buttonDeviceScan != null) {
            buttonDeviceScan.setOnClickListener(this);
        }

        listViewDeviceList = (ListView) findViewById(R.id.listview_device_list);
        if (listViewDeviceList != null) {
            arrayListDevice = new ArrayList<HashMap<String, String>>();
            baseAdapterDevice = new SimpleAdapter(this, arrayListDevice,
                    android.R.layout.simple_expandable_list_item_2, new String[]{
                    KEY_NAME, KEY_SUMMARY, KEY_ADDRESS
            }, new int[]{
                    android.R.id.text1, android.R.id.text2
            });
            listViewDeviceList.setAdapter(baseAdapterDevice);
            listViewDeviceList.setOnItemClickListener(this);
        }
    }

    boolean _finish;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String deviceName = null;

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                final BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothDevice.getName() != null) {
                    deviceName = bluetoothDevice.getName().toLowerCase();
                    if (!deviceName.isEmpty()) {
                        if (deviceName.contains(DOTR3000_UHF_READER.toLowerCase())
                                ||deviceName.contains(DOTR3000_UHF_READER.toLowerCase())
                                ||deviceName.contains(RF800_UHF_READER.toLowerCase())) {
                            addDeviceToTheList(bluetoothDevice);
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Log.d("ReaderActivity", "ACTION_DISCOVERY_FINISHED");
                _finish = true;
            }
        }
    };

    void getDiscoveryPermission() {
        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            bluetoothDiscovery();
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION_PERMISSIONS);
    }

    void bluetoothDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            showMessageDialog("Bluetooth", "Veuillez activer le Bluetooth.");
            return;
        }
        bluetoothAdapter.startDiscovery();
        showSearchWaitDialog();
    }

    private void addDeviceToTheList(BluetoothDevice bluetoothDevice) {
        HashMap<String, String> item = new HashMap<String, String>();

        for (int i =0; i< arrayListDevice.size(); i++){
            final HashMap<String, String> map = arrayListDevice.get(i);
            final String deviceName = map.get(KEY_NAME);
            if (bluetoothDevice.getName().equalsIgnoreCase(deviceName)){
                //Log.d("Search", "already registered : " + device.getName());
                return;
            }
        }

        item.put(KEY_NAME, bluetoothDevice.getName());

        if (MainActivity.isConnected() &&
                (MainActivity.connectedDeviceMacAddress.equalsIgnoreCase(bluetoothDevice.getAddress()))) {
            item.put(KEY_SUMMARY, bluetoothDevice.getAddress() + " - Liée");
        } else {
            item.put(KEY_SUMMARY, bluetoothDevice.getAddress());
        }
        item.put(KEY_ADDRESS, bluetoothDevice.getAddress());

        arrayListDevice.add(item);
        baseAdapterDevice.notifyDataSetChanged();
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    private void showSearchWaitDialog() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Recherche d'appareils. S'il vous plaît, attendez.");
        progressDialog.setCancelable(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Terminer",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            bluetoothAdapter.cancelDiscovery();
                            progressDialog.dismiss();
                            dialog.cancel();
                            handler.removeCallbacksAndMessages(null);
                            Toast.makeText(DeviceSearchActivity.this, R.string.string_device_search_cancel, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                bluetoothAdapter.cancelDiscovery();
                handler.removeCallbacksAndMessages(null);
                Toast.makeText(getApplicationContext(), R.string.string_device_search_cancel, Toast.LENGTH_SHORT).show();
            }
        });
        progressDialog.show();
        handler.postDelayed(runnable, 15000);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (progressDialog != null && progressDialog.isShowing()) {
                bluetoothAdapter.cancelDiscovery();
                progressDialog.dismiss();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSIONS: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothDiscovery();
                } else {
                    Toast.makeText(this,
                            R.string.string_device_search_permission_denied,
                            Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_devicesearch_search:
                arrayListDevice.clear();
                getDiscoveryPermission();
                baseAdapterDevice.notifyDataSetChanged();
                bluetoothDiscovery();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(broadcastReceiver);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final HashMap<String, String> map = arrayListDevice.get(position);
        if (map == null) {
            return;
        }

        final String deviceMacAddr = map.get(KEY_ADDRESS);
        final String deviceName = map.get(KEY_NAME);

        if (deviceMacAddr == null){
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DeviceSearchActivity.this);

        if (MainActivity.isConnected() &&
                MainActivity.getConnetedDeviceMacAddress().equalsIgnoreCase(deviceMacAddr)) {
            builder.setTitle("Se déconnecter de l'appareil")
                    .setMessage("Voulez-vous vous déconnecter de l'appareil? \n" + "(MAC : " + deviceMacAddr + " )")
                    .setCancelable(false)
                    .setPositiveButton("Déconnecter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            MainActivity.setDisconnect();
                            Handler handler = new Handler();
                            final ProgressDialog progressDialog =
                                    ProgressDialog.show(DeviceSearchActivity.this, "Déconnexion de l'appareil: " + deviceMacAddr, "S'il vous plaît, attendez.");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!MainActivity.isConnected()) {
                                        map.put(KEY_SUMMARY, deviceMacAddr);
                                        baseAdapterDevice.notifyDataSetChanged();
                                    }
                                    progressDialog.dismiss();
                                }
                            }, 3000);
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
        } else { //!< Connect Device
            builder.setTitle("Se connecter à l'appareil")
                    .setMessage("Voulez-vous vous connecter à l'appareil? \n" + "(MAC : " + deviceMacAddr + " )")
                    .setCancelable(false)
                    .setPositiveButton("Relier", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            MainActivity.setConnect(deviceMacAddr, deviceName);
                            Handler handler = new Handler();
                            final ProgressDialog progressDialog =
                                    ProgressDialog.show(DeviceSearchActivity.this, "Connexion de l'appareil: " + deviceMacAddr, "S'il vous plaît, attendez.");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (MainActivity.isConnected()) {
                                        map.put(KEY_SUMMARY, " - Liée");
                                        baseAdapterDevice.notifyDataSetChanged();
                                    }
                                    progressDialog.dismiss();
                                }
                            }, 5000);
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
        }
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
