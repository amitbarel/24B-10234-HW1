package dev.amitb.a24b_10234_hw1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ShapeableImageView IMG_lock, IMG_bluetooth, IMG_nfc, IMG_battery;
    private MaterialButton BTN_login;
    private MaterialTextView TXT_title;
    private boolean isLocked = true;

    private final String bluetoothDevice = "JBL WAVE200TWS";

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private NfcAdapter nfcAdapter;

    private STATE state = STATE.NA;

    private enum STATE {
        NA,
        NOT_CHARGING,
        NO_BLUETOOTH,
        NOT_CONNECTED,
        NO_NFC,
        SETTINGS_OK
    }

    ActivityResultLauncher<Intent> appSettingsResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
                tryToLogin();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        findViews();
        initViews();
    }

    private String checkPermissionsStatus(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED)
            return android.Manifest.permission.NFC;
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
            return android.Manifest.permission.BLUETOOTH;
        return null;
    }

    private void initViews() {
        BTN_login.setOnClickListener(v->tryToLogin());
    }

    private void tryToLogin() {
        if (!isCharging()) {
            state = STATE.NOT_CHARGING;
            Toast.makeText(this, "Please charge your phone", Toast.LENGTH_LONG).show();
            return;
        }else
            IMG_battery.setColorFilter(ContextCompat.getColor(this,R.color.verified));
        if (!isBluetoothEnabled(this) || !isDeviceConnectedToSpecific(bluetoothDevice)) {
            if (!isBluetoothEnabled(this)){
                state = STATE.NO_BLUETOOTH;
                Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_LONG).show();
                return;
            }
            else{
                state = STATE.NOT_CONNECTED;
                Toast.makeText(this, "Please connect your phone to " + bluetoothDevice, Toast.LENGTH_LONG).show();
                return;
            }
        }else
            IMG_bluetooth.setColorFilter(ContextCompat.getColor(this,R.color.verified));
        if (!isNFCEnabled(this)){
            state = STATE.NO_NFC;
            Toast.makeText(this, "Please enable NFC", Toast.LENGTH_LONG).show();
        }
        else {
            IMG_nfc.setColorFilter(ContextCompat.getColor(this,R.color.verified));
            state = STATE.SETTINGS_OK;
            isLocked = false;
            if (!isLocked){
                IMG_lock.setBackgroundResource(R.drawable.ic_lock_open);
                TXT_title.setText("App Unlocked");
                BTN_login.setVisibility(View.GONE);
                IMG_nfc.setVisibility(View.GONE);
                IMG_bluetooth.setVisibility(View.GONE);
                IMG_battery.setVisibility(View.GONE);
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

    private boolean isDeviceConnectedToSpecific(String name) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            enableBluetoothService();
        }
        List<BluetoothDevice> connectedDevices = btManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice device : connectedDevices)
            if (device.getAddress().equals(name))
                return true;
        return false;
    }

    private void enableBluetoothService() {
        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
    }

    private boolean isBluetoothEnabled(Context context){
        return btAdapter.isEnabled();
    }

    private boolean isNFCEnabled(Context context){
        return nfcAdapter.isEnabled();
    }

}