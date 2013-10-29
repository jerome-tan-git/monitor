package com.example.monitor;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;

public class MyService extends Service{

	private static final String TAG = "MyService";
	private BroadcastReceiver networkBroadcast=new NetworkListener();
	private TelephonyManager mTelephonyManager; 
    //这里定义吧一个Binder类，用在onBind()有方法里，这样Activity那边可以获取到  
    private MyBinder mBinder = new MyBinder();
    private MonitorProcess mp = new MonitorProcess(this);
    @Override  
    public IBinder onBind(Intent intent) {  
        Log.e(TAG, "start IBinder~~~");  
        return mBinder;  
    }  
    @Override  
    public void onCreate() {  
    	mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(new MyPhoneStateListener(this), PhoneStateListener.LISTEN_CALL_STATE);  
        Log.e(TAG, "start onCreate~~~");  
        super.onCreate();  
    }  
      
    @Override  
    public void onStart(Intent intent, int startId) {  
        Log.e(TAG, "start onStart~~~"); 
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(networkBroadcast, filter);
        mp.setHoldFlag(0);
        Thread t = new Thread(mp);
        t.start();
        super.onStart(intent, startId);   
    }  
      
    @Override  
    public void onDestroy() {  
        Log.e(TAG, "start onDestroy~~~");  
        mp.setHoldFlag(1);
        this.unregisterReceiver(networkBroadcast);  
        super.onDestroy();  
    }  
      
      
    @Override  
    public boolean onUnbind(Intent intent) {  
        Log.e(TAG, "start onUnbind~~~");  
        return super.onUnbind(intent);  
    }  
      
    //这里我写了一个获取当前时间的函数，不过没有格式化就先这么着吧  
    public String getSystemTime(){  
          
        Time t = new Time();  
        t.setToNow();  
        return t.toString();  
    }  
      
    public class MyBinder extends Binder{  
        MyService getService()  
        {  
            return MyService.this;  
        }  
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
}
