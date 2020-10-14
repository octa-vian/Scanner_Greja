package com.leonardus.irfan.bluetoothprinter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothPrinter {
    /*
        BLUETOOTH PRINTER
        Library untuk menggunakan bluetooth printer. Langkah menggunakan :
        1. Buat objek BluetoothPrinter dengan menggunakan keyword new dengan parameter input context
            (ex : btPrint = new BluetoothPrinter(this))
        2. panggil method startService untuk menginisialisasi object bluetooth printer
            (ex : btnPrint.startService())
        3. panggil method showDevices untuk melakukan koneksi dengan device bluetooth printer
            (ex : btPrint.showDevices())
        4. panggil method print dengan parameter input pesan untuk mencetak pesan di device
            (ex : btPrint.print("Test Printing"))
        5. panggil method stopService untuk mengakhiri koneksi, saran : gunakan di method onDestroy Activity
            (ex : btPrint.stopService())
    */

    private final String LOG = "bluetooth_log";

    private final UUID BLUETOOTH_PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final int PERMISSION_LOCATION = 901;

    Context context;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    public BluetoothDevice bluetoothDevice;
    public OutputStream outputStream;
    private InputStream inputStream;
    private ProgressBar progressbar;
    private Button btn_devices;

    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    private Dialog dialogDevices;
    private Dialog dialogBluetooth;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;

    private ArrayAdapter<String> deviceAdapter;
    private List<String> listDevicesData = new ArrayList<>();
    private List<BluetoothDevice> listDevices = new ArrayList<>();

    private ArrayAdapter<String> discoveredAdapter;
    private List<String> listDiscoveredData = new ArrayList<>();
    private List<BluetoothDevice> listDiscovered = new ArrayList<>();

    private BluetoothListener listener;

    public BluetoothPrinter(Context context){
        this.context = context;
    }

    public void  StartPermission(){
        //Inisialisasi UI
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bluetooth tidak menyala");
        builder.setMessage("Bluetooth anda tidak menyala. Nyalakan bluetooth sekarang?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Meminta user menyalakan bluetooth
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                BluetoothPrinter.this.context.startActivity(intentOpenBluetoothSettings);
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialogBluetooth = builder.create();

        //Inisialisasi Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(context, "Adapter Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!bluetoothAdapter.isEnabled()) {
            dialogBluetooth.show();
            return;
        }
       // showDevices();

    }

    public void startService(){
       /* if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPermission();
            return;
        }*/

        //Inisialisasi UI
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bluetooth tidak menyala");
        builder.setMessage("Bluetooth anda tidak menyala. Nyalakan bluetooth sekarang?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Meminta user menyalakan bluetooth
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                BluetoothPrinter.this.context.startActivity(intentOpenBluetoothSettings);
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialogBluetooth = builder.create();

        //Inisialisasi Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(context, "Adapter Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!bluetoothAdapter.isEnabled()) {
            dialogBluetooth.show();
            return;
        }

        //init dialog UI
        dialogDevices = new Dialog(context, R.style.CustomDialogStyle);
        dialogDevices.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDevices.setContentView(R.layout.popup_devices);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        int device_TotalWidth = metrics.widthPixels;
        int device_TotalHeight = metrics.heightPixels;

        if(dialogDevices.getWindow() != null){
            dialogDevices.getWindow().setLayout(device_TotalWidth * 80 / 100 , device_TotalHeight * 80 / 100);
            dialogDevices.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ListView list_devices = dialogDevices.findViewById(R.id.list_devices);
        final ListView list_discovered = dialogDevices.findViewById(R.id.list_discovered);
        btn_devices = dialogDevices.findViewById(R.id.btn_devices);
        progressbar = dialogDevices.findViewById(R.id.progressbar);

        deviceAdapter = new ArrayAdapter<>(context, R.layout.item_devices,
                R.id.txt_device, listDevicesData);
        list_devices.setAdapter(deviceAdapter);
        list_devices.setOnItemClickListener(new DeviceClicked());

        discoveredAdapter = new ArrayAdapter<>(context, R.layout.item_devices,
                R.id.txt_device, listDiscoveredData);
        list_discovered.setAdapter(discoveredAdapter);
        list_discovered.setOnItemClickListener(new DiscoveredClicked());

        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, filter);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //System.out.println("ACTION " + action);
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if(!listDiscovered.contains(device)){
                            listDiscovered.add(device);
                            listDiscoveredData.add(device.getName() + "\n" + device.getAddress());
                            discoveredAdapter.notifyDataSetChanged();
                        }
                    }
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    progressbar.setVisibility(View.GONE);
                    btn_devices.setText(R.string.cari_device);
                    //Toast.makeText(context, "Pencarian Device Selesai", Toast.LENGTH_SHORT).show();
                }
                else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    if(state == BluetoothDevice.BOND_BONDED){
                        Toast.makeText(context, "Pairing device berhasil", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        btn_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isDiscovering()) {
                    progressbar.setVisibility(View.GONE);
                    bluetoothAdapter.cancelDiscovery();
                    btn_devices.setText(R.string.cari_device);
                    //mBluetoothAdapter.startDiscovery();
                }
                else {
                    listDiscovered.clear();
                    listDiscoveredData.clear();
                    discoveredAdapter.notifyDataSetChanged();

                    btn_devices.setText(R.string.berhenti);
                    progressbar.setVisibility(View.VISIBLE);
                    bluetoothAdapter.startDiscovery();
                    context.registerReceiver(broadcastReceiver, filter);
                }
            }
        });
        showDevices();
    }

    public void showDevices(){
        if(!bluetoothAdapter.isEnabled()) {
            dialogBluetooth.show();
            return;
        }

        listDiscovered.clear();
        listDiscoveredData.clear();
        discoveredAdapter.notifyDataSetChanged();

        initDevices();
        dialogDevices.show();
    }

    public void stopService(){
        try {
            closeBT();
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
            }

            context.unregisterReceiver(broadcastReceiver);
        }
        catch (Exception e){
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void createBond(BluetoothDevice device)throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        if(!returnValue){
            Toast.makeText(context, "Pairing device gagal!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initDevices(){
        listDevices.clear();
        listDevicesData.clear();

        final Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        Object[] objectList = paired.toArray();

        if(objectList.length > 0) {
            for (Object device : objectList) {
                BluetoothDevice bluetooth = (BluetoothDevice) device;
                if(bluetooth.getUuids() != null){
                    if(bluetooth.getUuids()[0].getUuid().equals(BLUETOOTH_PRINTER_UUID)){
                        listDevicesData.add(bluetooth.getName() + "\n" + bluetooth.getAddress());
                        listDevices.add(bluetooth);
                    }
                }
            }
        }

        deviceAdapter.notifyDataSetChanged();
    }

    public void setListener(BluetoothListener listener){
        this.listener = listener;
    }

    private void connectBluetooth() throws IOException {
        try {

            socket = bluetoothDevice.createRfcommSocketToServiceRecord(BLUETOOTH_PRINTER_UUID);
            //socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(BLUETOOTH_PRINTER_UUID);
            socket.connect();
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            beginListenForData();

            if(listener != null){
                listener.onBluetoothConnected();
            }
            else{
                Toast.makeText(context, "Device Bluetooth Printer tersambung", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Gagal menyambungkan. Coba restart bluetooth/printer anda", Toast.LENGTH_SHORT).show();
            /*try {
                //Fallback
                Log.d(LOG, "fallback");

                socket =(BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(bluetoothDevice,1);
                socket.connect();

            }
            catch (Exception e2) {
                if(listener != null){
                    listener.onBluetoothFailed("Device Bluetooth Printer gagal tersambung");
                }
                else{
                    Toast.makeText(context, "Device Bluetooth Printer gagal tersambung", Toast.LENGTH_SHORT).show();
                }

                Log.e(LOG, e2.getMessage());
                e2.printStackTrace();
            }*/
        }
    }

    private void beginListenForData(){
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                //myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
        }
    }

    // this will send text data to be printed by the bluetooth printer
    public void print(String msg){
        if(bluetoothDevice == null){
            Toast.makeText(context, "Sambungkan ke device printer terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            msg += "\n";
            outputStream.write(new byte[]{29, 33, 35});
            outputStream.write(msg.getBytes());
        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
        }
    }

    // close the connection to bluetooth printer.
    private void closeBT() throws IOException {
        try {
            stopWorker = true;
            if(outputStream != null){
                outputStream.close();
            }
            if(inputStream != null){
                inputStream.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
        }
    }

    private class DeviceClicked implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice selected_device = listDevices.get(position);
            if(selected_device.getUuids()[0].getUuid().equals(BLUETOOTH_PRINTER_UUID)){
                try{
                    bluetoothDevice = selected_device;
                    connectBluetooth();
                    dialogDevices.dismiss();
                }
                catch (IOException e){
                    Log.e(LOG, e.getMessage());
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(context, "Device bukan Device Printer, coba Bluetooth lain", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DiscoveredClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Pairing dengan Device
            bluetoothAdapter.cancelDiscovery();
            progressbar.setVisibility(View.GONE);
            btn_devices.setText(R.string.cari_device);

            BluetoothDevice device = listDiscovered.get(position);

            try {
                createBond(device);
                dialogDevices.dismiss();
            } catch (Exception e) {
                Log.e(LOG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void askPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_COARSE_LOCATION)){

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Izin Lokasi");
            builder.setMessage("Aplikasi membutuhkan izin lokasi untuk dapat berjalan dengan benar.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_LOCATION);
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }
        else{
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_LOCATION);
        }
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public Context getContext() {
        return context;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public interface BluetoothListener{
        void onBluetoothConnected();
        void onBluetoothFailed(String message);
    }

}
