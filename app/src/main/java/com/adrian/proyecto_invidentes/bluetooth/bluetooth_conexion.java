package com.adrian.proyecto_invidentes.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class bluetooth_conexion
{
    public String UUID_string ="00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    private boolean connected;

    private String host_name;

    private Activity view;

    private String data_stream;

    public bluetooth_conexion(String host_name, Activity view)
    {
        this.host_name = host_name;
        this.view = view;

        this.connected = false;
    }

    public void iniciar()
    {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            Log.i("test","Bluetooth Device Not Available");
            view.finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            view.startActivityForResult(turnBTon,1);
        }
    }

    public void buscar_host(String host_name)
    {
        pairedDevices = myBluetooth.getBondedDevices();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                Log.i("test",bt.getName() + "\n" + bt.getAddress());

                if(bt.getName().equals(host_name))
                {
                    mmDevice = bt;
                    break;
                }
            }
        }
        else
        {
            Log.i("test","No Paired Bluetooth Devices Found.");
        }
    }

    public void conectar() throws IOException
    {
        UUID uuid = UUID.fromString(UUID_string); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        iniciar_busqueda();

        Log.i("test","Bluetooth Opened");
    }

    private void iniciar_busqueda()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10;

        this.connected = true;
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "UTF-8");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            //Log.i("test",data);
                                            data_stream=data;
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    public void enviar_data(String msg) throws IOException
    {
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        Log.i("test","Data Sent");
    }

    public void desconectar() throws IOException
    {
        this.connected = false;
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();

        Log.i("test","Bluetooth Closed");
    }

    public boolean status()
    {
        return this.connected;
    }

    public String get_data()
    {
        return this.data_stream;
    }


}
