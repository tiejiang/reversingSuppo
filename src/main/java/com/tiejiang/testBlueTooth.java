package com.tiejiang;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class testBlueTooth extends Activity {
	static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private UUID uuid ;
	private static final String TAG = "BluetoothTest";	
	private static final boolean STATE_CONNECTED = true;
	
	private Button btnSearch, btnDis, btnExit;
	private ToggleButton tbtnSwitch;
	
	private ListView lvBTDevices;
	private ArrayAdapter<String> adtDevices;
	private List<String> lstDevices = new ArrayList<String>();	
	private BluetoothAdapter btAdapt;
	
	public static BluetoothSocket socket = null;
	public static BluetoothSocket btSocket;
	public static AcceptThread serverThread;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Button ����
		btnSearch = (Button) this.findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(new ClickEvent());
		
		btnExit = (Button) this.findViewById(R.id.btnExit);
		btnExit.setOnClickListener(new ClickEvent());
		
		btnDis = (Button) this.findViewById(R.id.btnDis);
		btnDis.setOnClickListener(new ClickEvent());

		// ToogleButton����
		tbtnSwitch = (ToggleButton) this.findViewById(R.id.tbtnSwitch);
		tbtnSwitch.setOnClickListener(new ClickEvent());

		// ListView��������Դ ������
		lvBTDevices = (ListView) this.findViewById(R.id.lvDevices);
		adtDevices = new ArrayAdapter<String>(testBlueTooth.this,
				android.R.layout.simple_list_item_1, lstDevices);
		lvBTDevices.setAdapter(adtDevices);
		lvBTDevices.setOnItemClickListener(new ItemClickEvent());			

		btAdapt = BluetoothAdapter.getDefaultAdapter();
		uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		
		if(btAdapt == null){
			Log.e(TAG, "No BlueToothDevice!");
			finish();
			return;
		}
		else{		
			if (btAdapt.getState() == BluetoothAdapter.STATE_OFF)// ��ȡ����״̬����ʾ
				{
				tbtnSwitch.setChecked(true);
					Toast.makeText(testBlueTooth.this, "蓝牙尚未打开,服务端需先打开蓝牙", Toast.LENGTH_LONG).show();
				}
			else if (btAdapt.getState() == BluetoothAdapter.STATE_ON){			
				tbtnSwitch.setChecked(false);
				serverThread=new AcceptThread();
				serverThread.start();				
			}	
			IntentFilter intent = new IntentFilter();
			intent.addAction(BluetoothDevice.ACTION_FOUND);
			intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
			intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			registerReceiver(searchDevices, intent);		
		}			
	}
	private void manageConnectedSocket() {
		btSocket=socket;
		Intent intent = new Intent();
		intent.setClass(testBlueTooth.this, RelayControl.class);
		startActivity(intent);
	}
	private BroadcastReceiver searchDevices = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle b = intent.getExtras();
			Object[] lstName = b.keySet().toArray();
			for (int i = 0; i < lstName.length; i++) {
				String keyName = lstName[i].toString();
				Log.e(keyName, String.valueOf(b.get(keyName)));
			}
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String str= device.getName() + "|" + device.getAddress();				
				if (lstDevices.indexOf(str) == -1)
					lstDevices.add(str);
				adtDevices.notifyDataSetChanged();
			}
		}
	};

	@Override
	protected void onDestroy() {
	    this.unregisterReceiver(searchDevices);
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
		serverThread.cancel();
		serverThread.destroy();		
	}

	class ItemClickEvent implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {			
			String str = lstDevices.get(arg2);
			String[] values = str.split("\\|");
			String address=values[1];
			Log.e("address",values[1]);			
		    uuid = UUID.fromString(SPP_UUID);
			Log.e("uuid",uuid.toString());			
			BluetoothDevice btDev = btAdapt.getRemoteDevice(address);//"00:11:00:18:05:45"
			//int sdk = Integer.parseInt(Build.VERSION.SDK);			
			/*
			int sdk = Integer.parseInt(Build.VERSION.SDK);
			if (sdk >= 10) {
			     try {
					btSocket = btDev.createInsecureRfcommSocketToServiceRecord(uuid);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, " High: Connection failed.", e);		
				}
			} else {
			      try {
					btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "Low: Connection failed.", e);		
				}
			}*/			
			Method m;
			try {
				m = btDev.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
				btSocket = (BluetoothSocket) m.invoke(btDev, Integer.valueOf(1));
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//				 try {
//						btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						Log.e(TAG, "Low: Connection failed.", e);		
//					}
				
				
//				btSocket = InsecureBluetooth.listenUsingRfcommWithServiceRecord(btAdapt, "", uuid, true);
//				btSocket = InsecureBluetooth.createRfcommSocketToServiceRecord(btDev, uuid, true);
				
			btAdapt.cancelDiscovery();
			try {
				//btSocket = btDev.createRfcommSocketToServiceRecord(uuid);				
				btSocket.connect();				
				Log.e(TAG, " BT connection established, data transfer link open.");

				Toast.makeText(testBlueTooth.this, "连接成功,进入控制界面", Toast.LENGTH_SHORT).show();

				Intent intent = new Intent();
				intent.setClass(testBlueTooth.this, RelayControl.class);
				startActivity(intent);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, " Connection failed.", e);	
				setTitle("连接失败..");
			}		
		}
	}
	
	private	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == btnSearch)
			{
				if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {// ���������û����
					Toast.makeText(testBlueTooth.this, "请先打开蓝牙", 1000).show();
					return;
				}
				setTitle("本机蓝牙地址：" + btAdapt.getAddress());
				lstDevices.clear();
				btAdapt.startDiscovery();
			} else if (v == tbtnSwitch) {
				if (tbtnSwitch.isChecked() == false)				
				{					
					btAdapt.enable();
				    try {
						Thread.sleep(5 * 1000);//��ʱ5s
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					serverThread=new AcceptThread();
					serverThread.start();
					Toast.makeText(testBlueTooth.this, "服务端监听已打开", 1000).show();
				}				
				else if (tbtnSwitch.isChecked() == true)
					btAdapt.disable();
			} else if (v == btnDis)// �������Ա�����
			{
				Intent discoverableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(
						BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
			} else if (v == btnExit) {
				try {
					if (btSocket != null)
						btSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				testBlueTooth.this.finish();

				//test code begin
//				manageConnectedSocket();
				//test code end
			}
		}
	}

class AcceptThread extends Thread {
	private final BluetoothServerSocket serverSocket;
    public AcceptThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final	
    	BluetoothServerSocket tmp=null;
    	try {
			//tmp = btAdapt.listenUsingRfcommWithServiceRecord("MyBluetoothApp", uuid);
			
			Log.e(TAG, "++BluetoothServerSocket established!++");
			Method listenMethod = btAdapt.getClass().getMethod("listenUsingRfcommOn", new Class[]{int.class});
			tmp = ( BluetoothServerSocket) listenMethod.invoke(btAdapt, Integer.valueOf( 1));			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		serverSocket=tmp;
    }   
    
    public void run() {       
        // Keep listening until exception occurs or a socket is returned
       	//mState!=STATE_CONNECTED
        while(true) {
            try {
                socket = serverSocket.accept();
                Log.e(TAG, "++BluetoothSocket established! DataLink open.++");
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                manageConnectedSocket();     
                try {
					serverSocket.close();
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
	            serverSocket.close();
	        } catch (IOException e) { }
	    }
	}
}