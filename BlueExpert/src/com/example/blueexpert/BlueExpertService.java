package com.example.blueexpert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.http.util.ByteArrayBuffer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BlueExpertService {
	
    private BluetoothAdapter mAdapter;
	private Handler mHandler;
	private AcceptThread acceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread connectedThread;
	private BluetoothServerSocket mmServerSocket;
	private String remoteDeviceName;

	public BlueExpertService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
    }
	
	
    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        //if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        //setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }
    
    public synchronized void stop() {

//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        //setState(STATE_NONE);
    }
    
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        // Cancel the thread that completed the connection
        //if (connectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
    	remoteDeviceName = device.getName();
        // Cancel any thread currently running a connection
        if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        //setState(STATE_CONNECTED);
    }
    
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    public void write(ByteArrayBuffer out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = connectedThread;
        }
        
        
        byte[] bytes = out.toByteArray();
        byte[] ile = {(byte)out.length()};
        
        // Perform the write unsynchronized
        r.write(bytes);
    }
    
    public synchronized void connect(BluetoothDevice device) {

        // Cancel any thread attempting to make a connection
        
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        

        // Cancel any thread currently running a connection
        if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }
	
	private class AcceptThread extends Thread {		 
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	        	
	            tmp = mAdapter.listenUsingRfcommWithServiceRecord("BlueExpert", UUID.fromString("00112233-4455-6677-8899-aabbccddeeff"));
	        } catch (IOException e) { }
	        mmServerSocket = tmp;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	            } catch (IOException e) {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
                    connected(socket, socket.getRemoteDevice());
	                try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;
	            }
	        }
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) { }
	    }
	}

	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    private Bitmap bitmap;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        //byte[] buffer = new byte[500];  // buffer store for the stream
	        ByteArrayBuffer bufor = new ByteArrayBuffer(150000);
	        while (true) {
//	            try {
//	            	Log.d("ILE PRZYCH: ", Integer.toString(mmInStream.available()));
//	            	if(mmInStream.available() > 0)
//	            	{
//	            		//bufor.append(mmInStream.read());
//	            		bytes = mmInStream.read(buffer);
//	            		bufor.append(buffer, bufor.length(), bytes);
//	            	}
//	            	else{
//	            		if(bufor.length() > 100000){
//	            			Log.d("BUFOR", Integer.toString(bufor.length()));
//	            			Log.d("BUFOR", Integer.toString(bufor.byteAt(0)));
//	            			Log.d("BUFOR", Integer.toString(bufor.byteAt(1)));
//	            			Log.d("BUFOR", Integer.toString(bufor.byteAt(2)));
//		            		bitmap = BitmapFactory.decodeByteArray(bufor.toByteArray(), 0, bufor.length());		            		
//		            		mHandler.obtainMessage(MainActivity.MESSAGE_READ, bitmap).sendToTarget();
//		            		bufor.clear();
//	            		}
//	            	}
//	            } catch (IOException e) {
//	                Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_CONNECTION_LOST);
//	                mHandler.sendMessage(msg);
//	                break;
//	            }
	        	
				try {
					int a = mmInStream.read();
					switch(a){
					case 1:
						byte[] ile = new byte[4];
						for(int i = 0; i< 4; ++i){
							ile[i] = (byte) mmInStream.read();
						}
						
						ByteBuffer wrapped = ByteBuffer.wrap(ile); // big-endian by default
						int num = wrapped.getInt(); // 1
						Log.d("ILE BAJTOW??", Integer.toString(num));
						
						for(int i =0; i< num;++i){
							bufor.append(mmInStream.read());
						}
						bitmap = BitmapFactory.decodeByteArray(bufor.toByteArray(),
								0, bufor.length());
						mHandler.obtainMessage(MainActivity.MESSAGE_READ,
								bitmap).sendToTarget();
						bufor.clear();
						break;						
					
					case 2:
						String string = new String();
						this.sleep(200);						
						Log.d("ILE BAJTOW: ", Integer.toString(mmInStream.available()));
						while(mmInStream.available()>0){
							Log.d("ILE BAJTOW: ", Integer.toString(mmInStream.available()));
							string += Character.toString((char) mmInStream.read());
					    }
						String msg = new String();
						msg = remoteDeviceName + ":   " + string;
						mHandler.obtainMessage(MainActivity.MESSAGE,msg).sendToTarget();
						break;
					}
//					if(mmInStream.read() == 1)
//					bufor.append(mmInStream.read());
//					bitmap = BitmapFactory.decodeByteArray(bufor.toByteArray(),
//							0, bufor.length());
//					if (bitmap != null) {
//						mHandler.obtainMessage(MainActivity.MESSAGE_READ,
//								bitmap).sendToTarget();
//						bufor.clear();
//					}

				} catch (IOException e) {
					Message msg = mHandler
							.obtainMessage(MainActivity.MESSAGE_CONNECTION_LOST);
					mHandler.sendMessage(msg);
					break;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                    tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00112233-4455-6677-8899-aabbccddeeff"));
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BlueExpertService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
            
            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.DEVICE_NAME, mmDevice.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
	
	
	
	
	
	
	
	
	
	
	
	
}
