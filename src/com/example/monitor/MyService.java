package com.example.monitor;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
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
    //è¿™é‡Œå®šä¹‰å�§ä¸€ä¸ªBinderç±»ï¼Œç”¨åœ¨onBind()æœ‰æ–¹æ³•é‡Œï¼Œè¿™æ ·Activityé‚£è¾¹å�¯ä»¥èŽ·å�–åˆ°  
    private MyBinder mBinder = new MyBinder();
//    private MonitorProcess mp = new MonitorProcess(this);
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
    public void getWIFI()
    {
    	WifiManager wifimanager = (WifiManager)this.getSystemService(Service.WIFI_SERVICE);
    	System.out.println("Get wifi stats: " + wifimanager.getWifiState());
    }
    public void restartWIFI()
    {
    	WifiManager wifimanager = (WifiManager)this.getSystemService(Service.WIFI_SERVICE);
    	wifimanager.setWifiEnabled(false);
    	System.out.println("after set wif disable");
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	wifimanager.setWifiEnabled(true);
    	System.out.println("after set wif enable");
    }
    public int getNetworkStatus ()
    {
    	ConnectivityManager mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo == null) return -1;
        return mNetworkInfo.getType();
    }
    @Override  
    public void onStart(Intent intent, int startId) {  
        Log.e(TAG, "start onStart~~~"); 
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(networkBroadcast, filter);
        super.onStart(intent, startId);
        StartJobThread sjt = new StartJobThread(this);
        Thread t = new Thread(sjt);
        t.start();
//        while(true)
//        {
        	
        }
//    }  
      
    @Override  
    public void onDestroy() {  
        Log.e(TAG, "start onDestroy~~~");  
//        mp.setHoldFlag(1);
        this.unregisterReceiver(networkBroadcast);  
        super.onDestroy();  
    }  
      
      
    @Override  
    public boolean onUnbind(Intent intent) {  
        Log.e(TAG, "start onUnbind~~~");  
        return super.onUnbind(intent);  
    }  
      
    //è¿™é‡Œæˆ‘å†™äº†ä¸€ä¸ªèŽ·å�–å½“å‰�æ—¶é—´çš„å‡½æ•°ï¼Œä¸�è¿‡æ²¡æœ‰æ ¼å¼�åŒ–å°±å…ˆè¿™ä¹ˆç�€å�§  
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
        // åˆ�å§‹åŒ–iTelephony  
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
