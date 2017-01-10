package com.example.blueexpert;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 1;
	public static final int MESSAGE_WRITE = 0;
	public static final int MESSAGE = 2;
	public static final int MESSAGE_READ = 1;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_CONNECTION_LOST = 5;
	public static final String DEVICE_NAME = "device_name";
	private static BluetoothAdapter mBluetoothAdapter;
    // Name of the connected device
    private String mConnectedDeviceName = null;
	private Button button, buttonSzukaj, buttonSkroty;
	private TextView tVstatus;
	private ImageView image;
	private EditText edittext;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	public static BlueExpertService blueExpertService;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private ArrayList<String> wykryte;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		wykryte = new ArrayList<String>();
		button = (Button) findViewById(R.id.button1);
		buttonSzukaj = (Button) findViewById(R.id.button2);
		buttonSkroty = (Button) findViewById(R.id.button3);
		listView = (ListView) findViewById(R.id.listView1);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		listView.setAdapter(adapter);
		edittext=(EditText)findViewById(R.id.editText1);
		
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
		button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				adapter.add("Ja:   "+ edittext.getText().toString());
				String s =edittext.getText().toString()+'#';
				edittext.setText("");
				Log.d("wysy³a",s);
				char[] buffer = s.toCharArray();
				byte[] b = new byte[buffer.length];
				for (int i = 0; i < b.length; i++) {
					b[i] = (byte) buffer[i];
				}

				ByteArrayBuffer bufor = new ByteArrayBuffer(255);
				bufor.append(b, 0, b.length);
				
				blueExpertService.write(bufor);
			}
		});
		
		buttonSzukaj.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				wykryte.clear();				
				doDiscovery();
			}
		});
		
		buttonSkroty.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, Skroty.class);
				startActivity(intent);
				
			}
		});
		tVstatus = (TextView) findViewById(R.id.textView1);
		tVstatus.setText("Niepo³¹czony");
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }			
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}	
	}

	@Override
	protected void onStart() {
		super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        if (blueExpertService == null) blueExpertService = new BlueExpertService(this, mHandler);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        if (blueExpertService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            //if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              blueExpertService.start();
            }
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (blueExpertService != null) blueExpertService.stop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                break;
            case MESSAGE_READ:
                //byte[] readBuf = (byte[]) msg.obj;
                //Bitmap bitmap = BitmapFactory.decodeByteArray(readBuf, 0, msg.arg1);
                image.setImageBitmap((Bitmap) msg.obj);
                // construct a string from the valid bytes in the buffer
                //String readMessage = new String(readBuf, 0, msg.arg1);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                tVstatus.setText("Po³¹czono z " + mConnectedDeviceName);
                break;
            case MESSAGE:
            	String str = (String) msg.obj;
            	adapter.add(str);
                break;                
            case MESSAGE_CONNECTION_LOST:
            	blueExpertService.start();
            	tVstatus.setText("Niepo³¹czony");
            	break;
            }
        }
    };
    
    private void doDiscovery() {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // If we're already discovering, stop it
        if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
        	BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                wykryte.add(device.getName() + "\n" + device.getAddress());
                //mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false); 
                if (wykryte.size() != 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            		builder.setTitle("Wybierz urz¹dzenie");
            		AlertDialog alertDialog = null;
            		String[] lista = wykryte.toArray(new String[wykryte.size()]);
            		builder.setItems(lista, new DialogInterface.OnClickListener() {
    					
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						
    						String address = wykryte.get(which).substring(wykryte.get(which).length() - 17);
    						Log.d("ADRES	", address);
    						BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    						blueExpertService.connect(device);
    						wykryte.clear();
    						dialog.cancel();
    						
    					}
    				});
            		
            		alertDialog = builder.create();
    				alertDialog.show();
                }               
                else {
                    Toast.makeText(MainActivity.this,"Nie znaleziono urzadzen", Toast.LENGTH_SHORT);
                }
            }
        }
    };

}

class IOUtil {

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
}
