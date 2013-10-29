package com.example.monitor;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import redis.clients.jedis.Jedis;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button button01;
	private Button buttonStart;
	private Button buttonStop;
	private Button buttonCall;
	private TextView textView01;
	private TelephonyManager mTelephonyManager; 
	private BroadcastReceiver networkBroadcast=new NetworkListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("Start monitor_______________________22_____________________"); 
		setContentView(R.layout.activity_main);
		button01 = (Button)findViewById(R.id.button1);
		buttonStop = (Button)findViewById(R.id.button2);
		buttonStart = (Button)findViewById(R.id.button3);
		buttonCall = (Button)findViewById(R.id.button4);
		textView01 = (TextView)findViewById(R.id.textView1);
		
		mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//		mTelephonyManager.listen(new MyPhoneStateListener(this), PhoneStateListener.LISTEN_CALL_STATE);  
//		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//	    this.registerReceiver(networkBroadcast, filter);  
		 button01.setOnClickListener(new Button.OnClickListener(){ 
	            @Override
	            public void onClick(View v) {
	            	 Jedis jedis = new Jedis("192.168.103.18");
	        		 jedis.auth("123456redis");
	        		 //jedis.set("java", "http://java.androidwhy.com");
	        		 String value = jedis.get("foo");
	        		 //System.out.println(value);
	                // TODO Auto-generated method stub

	                textView01.setText(value);
	                //new MonitorProcess();
	            }         

	        });     
		 buttonStop.setOnClickListener(new Button.OnClickListener(){ 
	            @Override
	            public void onClick(View v) {
	            	//Intent newIntent = new Intent(this, MyService.class); 
	            	Intent intent = new Intent(MainActivity.this, MyService.class);
	            	stopService(intent);
	            }         

	        });   
		 buttonStart.setOnClickListener(new Button.OnClickListener(){ 
	            @Override
	            public void onClick(View v) {
	            	//Intent newIntent = new Intent(this, MyService.class); 
	            	Intent intent = new Intent(MainActivity.this, MyService.class);
	            	startService(intent);
	            }         

	        });
		 
		 buttonCall.setOnClickListener(new Button.OnClickListener(){ 
	            @Override
	            public void onClick(View v) {
	            	//Intent newIntent = new Intent(this, MyService.class); 
	            	Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:13671605961"));
	            	MainActivity.this.startActivity(intent);
	            	System.out.println("after call................");
	            	try {
						Thread.sleep(45000);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
	            	endCall();
	            }         
	        });
	}
	 public void endCall() {  
	        // 初始化iTelephony  
	        Class<TelephonyManager> c = TelephonyManager.class;  
	        Method getITelephonyMethod = null;  
	        try {  
	            getITelephonyMethod = c.getDeclaredMethod("getITelephony",(Class[]) null);  
	            getITelephonyMethod.setAccessible(true);  
	            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null);  
	            iTelephony.endCall();  
	            System.out.println("endCall......");  
	        } catch (Exception e) {
	        	System.out.println("endCall...... error");
	              
	        }   
	    } 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		this.unregisterReceiver(networkBroadcast);  
	}
}
