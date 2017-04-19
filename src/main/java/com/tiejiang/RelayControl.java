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
	public static boolean isRecording = false;// �߳̿��Ʊ��		
	private Button releaseCtrl,btBack,distance_display;
	private Button car_left, car_right, car_back;
	private OutputStream outStream = null;	
	private EditText _txtRead;	
	private ConnectedThread manageThread;
	private Handler mHandler;	
	private String  encodeType ="GBK";
	private Vibrator mVibrator;
	private String readStr1 = "";
	long [] pattern = {100,400,100,400};   // ֹͣ ���� ֹͣ ����   
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.relaycontrol);			
		//�����߳�����
		manageThread = new ConnectedThread();
		mHandler = new MyHandler();
		manageThread.Start();		
		findMyView();		
		setMyViewListener(); 		
		setTitle("����ǰ���ȹر�socket����");
		//���������ɼ�
		_txtRead.setCursorVisible(false);      //����������еĹ�겻�ɼ�
		_txtRead.setFocusable(false);           //�޽���
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

//		int code = mTts.startSpeaking("语音合成成功", mTtsListener);
			
	}

	private void findMyView() {			
		car_left=(Button)findViewById(R.id.car_left);
		car_right=(Button)findViewById(R.id.car_right);
		car_back=(Button)findViewById(R.id.car_back);		
		releaseCtrl=(Button)findViewById(R.id.button1);
		btBack=(Button) findViewById(R.id.button2);	
		distance_display=(Button)findViewById(R.id.distance_display);
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
			if (v == releaseCtrl)// �ͷ�����
			{
				try {
					testBlueTooth.btSocket.close();
					manageThread.Stop();
					//testBlueTooth.serverThread.cancel();					
					//Toast.makeText(getApplicationContext(), "socket�����ѹر�", Toast.LENGTH_SHORT);
					setTitle("socket�����ѹر�");
				} catch (IOException e) {
					//Log .e(TAG,"ON RESUME: Unable to close socket during connection failure", e2);
					//Toast.makeText(getApplicationContext(), "�ر�����ʧ��", Toast.LENGTH_SHORT);
					setTitle("�ر�����ʧ��");
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
	       // view.setTextColor(R.color.read_only_color);   //����ֻ��ʱ��������ɫ
	        if (view instanceof android.widget.EditText){
	            view.setCursorVisible(able);      //����������еĹ�겻�ɼ�
	            view.setFocusable(able);           //�޽���
	            view.setFocusableInTouchMode(able);     //����ʱҲ�ò�������
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
			//Toast.makeText(getApplicationContext(), "����������..", Toast.LENGTH_SHORT);
			setTitle("�ɹ�����ָ��:"+message);				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Log.e(TAG, "ON RESUME: Exception during write.", e);
			Toast.makeText(getApplicationContext(), "��������ʧ��", Toast.LENGTH_SHORT).show();				
		}			
	}	
	 class ConnectedThread extends Thread {		
		private InputStream inStream = null;// ��������������
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
				            String readStr1 = new String(btBuf,encodeType);
							sendToUI = sendToUI + readStr1;
							Log.d("TIEJIANG", "sendToUI: " + sendToUI);
							if (readStr1.contains("N") || readStr1.contains("H")){
								mHandler.obtainMessage(01,len,-1,sendToUI).sendToTarget();
								sendToUI = "";
							}else if(readStr1.trim().length() > 7) {
								sendToUI = "";
							}
//				            mHandler.obtainMessage(01,len,-1,readStr1).sendToTarget();
						}			             
			             Thread.sleep(wait);// ��ʱһ��ʱ�仺������
						}catch (Exception e) {
							// TODO Auto-generated catch block
							mHandler.sendEmptyMessage(00);
						}	
					}
				}
			}
		}	
	}	
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
    			String info=(String) msg.obj;
				Log.d("TIEJIANG", "MSG.OBJ= " + info);
				int code = mTts.startSpeaking(info, mTtsListener);
    			_txtRead.append(info);   
//    			AnalyzeData(info);
    			break;    			

            default:	            
                break;
    		}
    	}
    	public void AnalyzeData(String data){
    		
    		String[] tempData = new String[3];    		
    		String[] ArrayDistance = new String[3];    		
    		int distance = 0;
    		tempData = data.split(",");
    		System.out.println("ԭʼ����Ϊ��"+data);
    		System.out.println("����ĳ���Ϊ��"+tempData.length);
    		System.out.println("���鳤��Ϊ1ʱ��-Data=" + data);
//    		if (data.equals("1")) {
//    			car_left.setBackgroundColor(Color.RED);
//				mVibrator.vibrate(pattern,2);
//				Log.d("EQUALS", "111");
//			}else if (data.equals("3")) {
//    			car_right.setBackgroundColor(Color.RED);
//				mVibrator.vibrate(pattern,2);
//				Log.d("EQUALS", "333");
//			}
//    		if (tempData.length>1) {
//    			ArrayDistance = tempData[1].split("\\."); //'.'Ϊ����ͨ��ת���ַ��ķ�ʽ���ܹ�ʹ��split����
//				Log.d("distance=", ArrayDistance[0]);
//				distance = Integer.parseInt(ArrayDistance[0].trim());//ע��ȥ��ǰ��ո�
//				distance_display.setText(distance+" cm");				
//
//				Log.d("����ڶ�λ��ֵ��", tempData[1]);					
//				if (distance>0 && distance<20) {
//					car_back.setBackgroundColor(Color.RED);
//					mVibrator.vibrate(pattern,2);   //�ظ����������pattern ���ֻ����һ�Σ�index��Ϊ-1	
//				}			
//			}
    	}
	 }	
}