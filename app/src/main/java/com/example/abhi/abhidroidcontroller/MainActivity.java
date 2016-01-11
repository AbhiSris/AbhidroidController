package com.example.abhi.abhidroidcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "LEDOnOff";

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    BluetoothDevice device = null;
    final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket = null;

    OutputStream mmOutputStream;
    InputStream mmInputStream;
    String address;
    boolean connected;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Populates XML File
        setContentView(R.layout.fragment_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        //defines List View for Addresses
        ListView pairedListView = (ListView) findViewById(R.id.paired_listview);

        //defines the list inside the List View
        List<String> pairedList = new ArrayList<String>();

        //define adapter for array
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(
                this,                  //parent activity
                R.layout.list_item_view_layour,        //ID of list item layout
                R.id.list_item1,   //ID of text view to populate
                pairedList);                        //data

        //links adapter to the list view
        pairedListView.setAdapter(mArrayAdapter);

        //defines button for getting all the addresses
        final Button getAddressButton = (Button) findViewById(R.id.button);

        //When address button gets clicked
        getAddressButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device.getAddress());
                    }
                }
            }
        });

        //When any item in the listview gets clicked
        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                address = mArrayAdapter.getItem(i);

                connectToBT(address, connected);
                if(connected){
                    ToastMessage("CONNECTION STATUS", "...CONNECTED TO DEVICE " + address, 1000);
                }
                else{
                    ToastMessage("CONNECTION STATUS", "...FAILED CONNECTION TO DEVICE " + address, 1000);
                }

            }
        });

        final Button motorOffButton = (Button) findViewById(R.id.button4);

        motorOffButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("N");
            }
        });

        final Button motorOnButton = (Button) findViewById(R.id.button2);

        motorOnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("Y");
            }
        });
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            ToastMessage("Fatal Error", "Bluetooth Not supported. Aborting.", 2000);
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void connectToBT(String message, boolean connected){

        Log.d("Address received is : ", address);
        device = btAdapter.getRemoteDevice(address);

        //Step 1: Create RF comm with the UUID
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch(IOException e) {
            ToastMessage("Fatal Error", "IN RFROMM : " + e.getMessage() + ".", 2000);
        }
        //Step 2: Connection to the device. If fails, close connection to device
        try{
            btSocket.connect();
            connected = true;
        } catch(IOException e){
            try{
                btSocket.close();
                connected = false;
            } catch(IOException e2){
                ToastMessage("Fatal Error", "IN CONNECT : " + e.getMessage() + ".", 2000);
            }
        }
        //Output stream connection
        try {
            mmOutputStream = btSocket.getOutputStream();
        } catch(IOException e){
            ToastMessage("Fatal Error", "IN OUTPUT STREAM : " + e.getMessage() + ".", 2000);
        }
        //Input stream connection
        try {
            mmInputStream = btSocket.getInputStream();
        } catch(IOException e){
            ToastMessage("Fatal Error", "IN OUTPUT STREAM : " + e.getMessage() + ".", 2000);
        }
    }
    private void sendData(String message){
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Sending data: " + message + "...");

        try {
            mmOutputStream.write(msgBuffer);
            ToastMessage("SENT MESSAGE :", message, 1000);

        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            ToastMessage("Fatal Error", msg, 2000);
        }
    }

    private void ToastMessage (String title, String message, int toastTime){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, toastTime);
        msg.show();
        finish();
    }

}
