package com.example.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {
	private boolean ifValidNumber(String _phoneNumber) {
		Jedis _jedis = new Jedis("192.168.103.18");
		_jedis.auth("123456redis");
		if (_jedis.sismember("validPhone", _phoneNumber)) {
			return true;
		}
		if (_jedis.sismember("validPhone", "+86" + _phoneNumber)) {
			return true;
		}
		if (_phoneNumber.startsWith("+86")) {
			if (_jedis.sismember("validPhone",
					_phoneNumber.replaceAll("\\+86", ""))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction()

		.equals("android.provider.Telephony.SMS_RECEIVED")) {
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
					String json = "{message:'" + msgContent + "',phoneNumber:'"
							+ msgFrom + "'}";
					System.out.println("Rec: " + json);

					Pattern pattern = Pattern.compile("^z(\\d*([.]\\d+)?)");
					Matcher matcher = pattern.matcher(msgContent.toLowerCase()
							.trim());
					if (matcher.find()) {
						System.out.println("snooze msg");
						float time = Float.parseFloat(matcher.group(1));
						if (this.ifValidNumber(msgFrom)) {
							Jedis jedis = new Jedis("192.168.103.18");
							jedis.auth("123456redis");
							long snoozeTime = (long) (time * 60 * 1000);
							long toTime = System.currentTimeMillis()
									+ snoozeTime;
							Map<String, String> timeToSnooze = new HashMap<String, String>();

							timeToSnooze.put(msgFrom, "" + toTime);
							jedis.hmset("snooze", timeToSnooze);
							System.out.println("receive q: " + msgFrom + " | "
									+ new Date(toTime).toLocaleString());
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss [Z]");
							String msg = "Snooze to: \n" + sdf.format(new Date(toTime));
							SmsManager sms = SmsManager
									.getDefault();
							List<String> texts = sms
									.divideMessage(msg);
							for (String text : texts) {
								sms.sendTextMessage(
										msgFrom, null,
										text, null, null);
							}
						}
					} else {
						Jedis jedis = new Jedis("192.168.103.18");
						jedis.auth("123456redis");
						jedis.rpush("receiveQueue", json);	
						jedis.incr("tmpphone:" + msgFrom);
					}
				}


			}

		}
	}
}
