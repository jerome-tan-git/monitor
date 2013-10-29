package com.example.monitor;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyPhoneStateListener extends PhoneStateListener{
	private MyService parent;
	public MyPhoneStateListener(MyService _parent)
	{
		this.parent = _parent;
	}
	 @Override  
     public void onCallStateChanged(int state, String incomingNumber) {  
         switch (state) {  
         case TelephonyManager.CALL_STATE_IDLE:  
             System.out.println("手机空闲起来了  ");  
             break;  
         case TelephonyManager.CALL_STATE_RINGING:
        	 System.out.println("手机铃声响了，来电号码:"+incomingNumber);
        	 this.parent.endCall();
             break;  
         case TelephonyManager.CALL_STATE_OFFHOOK:  
        	 System.out.println("电话被挂起了 ");
         default:  
             break;  
         }  
           
     }  
}
