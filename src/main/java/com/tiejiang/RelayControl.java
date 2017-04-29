package com.tiejiang;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


/**
 * xunfei speak method
 * int code = mTts.startSpeaking(text, mTtsListener);
 * */

public class RelayControl extends XunFeiActivity{
	public static boolean isRecording = false;
	private Button releaseCtrl,btBack,distance_display;
	private Button car_left, car_right, car_back;
	private OutputStream outStream = null;	
	private EditText _txtRead;	
	private ConnectedThread manageThread;
	private Handler mHandler;	
	private String  encodeType ="GBK";
	private Vibrator mVibrator;
	private String readStr1 = "";

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.relaycontrol);			
		manageThread = new ConnectedThread();
		mHandler = new MyHandler();
		manageThread.Start();		
		findMyView();		
		setMyViewListener();
		setTitle("返回前需先关闭socket连接");
		_txtRead.setCursorVisible(false);
		_txtRead.setFocusable(false);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

//		int code = mTts.startSpeaking("语音合成成功", mTtsListener);
			
	}

	private void findMyView() {			
		car_left=(Button)findViewById(R.id.car_left);
		car_right=(Button)findViewById(R.id.car_right);
		car_back=(Button)findViewById(R.id.car_back);		
		releaseCtrl=(Button)findViewById(R.id.button1);
		btBack=(Button) findViewById(R.id.button2);	
		_txtRead = (EditText) findViewById(R.id.etShow);
	}

	private void setMyViewListener() {	
		car_left.setOnClickListener(new ClickEvent());
		car_right.setOnClickListener(new ClickEvent());
		car_back.setOnClickListener(new ClickEvent());
		releaseCtrl.setOnClickListener(new ClickEvent());
		btBack.setOnClickListener(new ClickEvent());
	}
	
	@Override
	public void onDestroy()  
    {  		
		try {
			testBlueTooth.btSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        super.onDestroy();  
        mVibrator.cancel();
    }
	
	
	private	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {			
			if (v == releaseCtrl)
			{
				try {
					testBlueTooth.btSocket.close();
					manageThread.Stop();
					setTitle("socket连接已关闭");
				} catch (IOException e) {
					setTitle("关闭连接失败");
				}				
			}else if (v == car_left) {
				car_left.setBackgroundColor(Color.WHITE);
				mVibrator.cancel();
			}else if (v == car_right) {
				car_right.setBackgroundColor(Color.WHITE);
				mVibrator.cancel();
			}else if (v == car_back) {
				car_back.setBackgroundColor(Color.WHITE);
				mVibrator.cancel();
			}
			else if (v == btBack) {// ����				
				RelayControl.this.finish();					
			} 		
		}
	}	
	  public static void setEditTextEnable(TextView view,Boolean able){
	        if (view instanceof android.widget.EditText){
	            view.setCursorVisible(able);
	            view.setFocusable(able);
	            view.setFocusableInTouchMode(able);
	        }
	  }	
	public void sendMessage(String message) {		
		//����ģ��
		try {
			outStream = testBlueTooth.btSocket.getOutputStream();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
			Toast.makeText(getApplicationContext(), " Output stream creation failed.", Toast.LENGTH_SHORT).show();		
		}	
		byte[] msgBuffer = null;		
		try {
			msgBuffer = message.getBytes(encodeType);//����
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("write", "Exception during write encoding GBK ", e1);
		}			
		//while(true){
		try {
			outStream.write(msgBuffer);
			Log.d("TIEJIANG", "send command to MCU");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Log.e(TAG, "ON RESUME: Exception during write.", e);
			Toast.makeText(getApplicationContext(), "发送数据失败", Toast.LENGTH_SHORT).show();
		}			
	}	
	 class ConnectedThread extends Thread {		
		private InputStream inStream = null;
		private long wait;
		private Thread thread;
		
		public ConnectedThread() {
			isRecording = false;
			this.wait=50;
			thread =new Thread(new ReadRunnable());
		}
		public void Stop() {
			isRecording = false;			
			}		
		public void Start() {
			isRecording = true;
			State aa = thread.getState();
			if(aa==State.NEW){
			thread.start();
			}else thread.resume();
		}		
		private class ReadRunnable implements Runnable {
			public void run() {			
				while (isRecording) {				
					try {					
						inStream = testBlueTooth.btSocket.getInputStream();						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//Log.e(TAG, "ON RESUME: Output stream creation failed.", e);
						Toast.makeText(getApplicationContext(), " input stream creation failed.", Toast.LENGTH_SHORT).show();					
					}						
					//char[]dd= new  char[40]; 		                      
					int length=20;
					byte[] temp = new byte[length];
					//String readStr="";
					//keep listening to InputStream while connected
					if (inStream!= null) {
					try{
						int len = inStream.read(temp,0,length-1);	
						Log.e("available", String.valueOf(len));
						//setTitle("available"+len);
						if (len > 0) {
							byte[] btBuf = new byte[len];
							System.arraycopy(temp, 0, btBuf, 0, btBuf.length);
							String sendToUI = readStr1;
							//������
				            readStr1 = new String(btBuf,encodeType);
							sendToUI = sendToUI + readStr1;
							Log.d("TIEJIANG", "sendToUI: " + sendToUI + " length= " + readStr1.length());
							if ( readStr1.length() > 15 && readStr1.length() < 20){
								mHandler.obtainMessage(01,len,-1,sendToUI).sendToTarget();
								sendToUI = "";
							}else if(readStr1.trim().length() > 7) {
								sendToUI = "";
							}
//				            mHandler.obtainMessage(01,len,-1,readStr1).sendToTarget();
						}			             
			             Thread.sleep(wait);
						}catch (Exception e) {
							// TODO Auto-generated catch block
							mHandler.sendEmptyMessage(00);
						}	
					}
				}
			}
		}	
	}

	/*
	* the received data type is as flows:
	* MSG.OBJ= D, 16, 5, 12, N, H
	*     right  left  back left right
	* **/
	 private class MyHandler extends Handler{ 
    	@Override		    
        public void dispatchMessage(Message msg) { 
    		switch(msg.what){
    		case 00:
    			isRecording=false;
    			_txtRead.setText("");
    			_txtRead.setHint("socket已经关闭");
    			//_txtRead.setText("inStream establishment Failed!");
    			break;
    			
    		case 01:
    			//_txtRead.setText("");
    			String info = (String) msg.obj;
				Log.d("TIEJIANG", "MSG.OBJ= " + info + ",  length= " + info.length());
//				int code = mTts.startSpeaking("播放", mTtsListener);
    			_txtRead.append(info);   
    			AnalyzeData(info);
    			break;    			

            default:	            
                break;
    		}
    	}
    	public void AnalyzeData(String data){
    		
    		String[] tempData = new String[6];
			String leftAlarm = " ";
			String rightAlarm = " ";
    		int right, left, back;

    		tempData = data.split(",");
			for (int i = 0; i < tempData.length; i ++){
				tempData[i] = tempData[i].trim();
				Log.d("TIEJIANG", "tempData[]= " + tempData[i]);
			}
			right = Integer.parseInt(tempData[1]);
			left = Integer.parseInt(tempData[2]);
			back = Integer.parseInt(tempData[3]);
//			leftAlarm = tempData[4];
//			rightAlarm = tempData[5];
			// 上一次播报结束后才进行到第二次数据判断的播报
//			mTts.startSpeaking("10", mTtsListener); // test
			Log.d("TIEJIANG", "isPlayEnd= " + isPlayEnd);
			if (isPlayEnd){
				if (right < 10){
					mTts.startSpeaking("距离右边小于" + tempData[1] + "厘米， 左转", mTtsListener);
					car_right.setBackgroundColor(Color.RED);
				}else if (left < 10){
					mTts.startSpeaking("距离左边小于" + tempData[2] + "厘米， 右转", mTtsListener);
					car_left.setBackgroundColor(Color.RED);
				}else if (back < 10){
					car_back.setBackgroundColor(Color.RED);
					mTts.startSpeaking("停止倒车，修正位置", mTtsListener);
				}else {
					mTts.startSpeaking("继续倒车， 注意两侧距离", mTtsListener);
					car_back.setBackgroundColor(Color.WHITE);
					car_left.setBackgroundColor(Color.WHITE);
					car_right.setBackgroundColor(Color.WHITE);
				}
				isPlayEnd = false;
			}

    	}
	 }	
}