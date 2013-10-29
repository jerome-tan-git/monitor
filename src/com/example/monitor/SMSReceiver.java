package com.example.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction()

		.equals("android.provider.Telephony.SMS_RECEIVED")) {

			// 不再往下传播；

			this.abortBroadcast();

			StringBuffer sb = new StringBuffer();

			String sender = null;

			String content = null;

			String sendtime = null;

			Bundle bundle = intent.getExtras();

			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] mges = new SmsMessage[pdus.length];

				for (int i = 0; i < pdus.length; i++) {

					mges[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

				}

				for (SmsMessage mge : mges) {
					String msgFrom = mge.getDisplayOriginatingAddress();
					String msgContent = mge.getMessageBody();
					
//					Date date = new Date(mge.getTimestampMillis());
//					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					sendtime = format.format(date);// 获取短信发送时间；
				}
				System.out.println("receive message:" + sb);

			}

		}
	}
}
