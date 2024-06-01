package dev.amitb.a24b_10234_hw1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ShapeableImageView IMG_lock, IMG_bluetooth, IMG_nfc, IMG_battery;
    private MaterialButton BTN_login;
    private MaterialTextView TXT_title;
    private boolean LOCKED = true;

    private final String BLUETOOTH_DEVICE_ADDRESS = "F8:AB:E5:83:7F:A6";

    private NfcAdapter nfcAdapter;

    private STATE state = STATE.NA;

    private enum STATE {
        NA,
        NOT_CHARGING,
        NOT_CONNECTED,
        NO_NFC,
        SETTINGS_OK
    }

    private ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    tryToLogin();
                } else {
                    boolean showDialog = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH);
                    if (showDialog){
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Bluetooth Permission Required")
                                .setCancelable(false)
                                .setMessage("Please grant Bluetooth Permission to use this app")
                                .setPositiveButton("To App Info", (dialog, which) -> grantBluetoothPermission())
                                .show();
                    } else{
                        grantBluetoothPermission();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        findViews();
        initViews();
    }

    private void initViews() {
        BTN_login.setOnClickListener(v -> tryToLogin());
    }

    private void tryToLogin() {
        boolean isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
        if (!isGranted) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH);
        }
        else {
            if (!isCharging()) {
                state = STATE.NOT_CHARGING;
                Toast.makeText(this, "Please charge your phone", Toast.LENGTH_LONG).show();
            } else if (!isDeviceConnectedToSpecific(this)) {
                IMG_battery.setColorFilter(ContextCompat.getColor(this, R.color.verified));
                state = STATE.NOT_CONNECTED;
                Toast.makeText(this, "Please enable Bluetooth and connect your phone to " + BLUETOOTH_DEVICE_ADDRESS, Toast.LENGTH_LONG).show();
            } else if (!isNFCEnabled()) {
                state = STATE.NO_NFC;
                IMG_bluetooth.setColorFilter(ContextCompat.getColor(this, R.color.verified));
                Toast.makeText(this, "Please enable NFC", Toast.LENGTH_LONG).show();
            } else {
                state = STATE.SETTINGS_OK;
                IMG_nfc.setColorFilter(ContextCompat.getColor(this, R.color.verified));
                LOCKED = false;
                IMG_lock.setBackgroundResource(R.drawable.ic_lock_open);
                TXT_title.setText("App Unlocked");
                BTN_login.setVisibility(View.GONE);
            }
        }

    }

    private void findViews() {
        IMG_lock = findViewById(R.id.IMG_lock);
        IMG_bluetooth = findViewById(R.id.IMG_bluetooth);
        IMG_nfc = findViewById(R.id.IMG_nfc);
        IMG_battery = findViewById(R.id.IMG_battery);
        BTN_login = findViewById(R.id.BTN_login);
        TXT_title = findViewById(R.id.TXT_title);
    }

    private boolean isCharging() {
        BatteryManager bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        int status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
        return status == BatteryManager.BATTERY_STATUS_CHARGING;
    }

    private boolean isDeviceConnectedToSpecific(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                grantBluetoothPermission();
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(BLUETOOTH_DEVICE_ADDRESS)) {
                    return true;
                }
            }
            for (BluetoothDevice device : bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)) {
                Log.d("BLUETOOTH_DEVICE", device.getName());
                if (device.getAddress().equals(BLUETOOTH_DEVICE_ADDRESS)) {
                    return true;
                }
            }
        }
        return false;
        }

    private void grantBluetoothPermission() {
        boolean showDialog = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.NFC);
        if (showDialog) {
            startActivity(new Intent()
                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", getPackageName(), null)));
        }
    }

    private boolean isNFCEnabled(){
        return nfcAdapter.isEnabled() && nfcAdapter != null;
    }

}