package com.example.monitor;

import java.util.List;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;

import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

public class MonitorProcess implements Runnable {
	private MyService parent;
	public MonitorProcess(MyService _parent)
	{
		this.parent = _parent;
	}
	private int holdFlag = 0;

	public int getHoldFlag() {
		return holdFlag;
	}

	public void setHoldFlag(int holdFlag) {
		this.holdFlag = holdFlag;
	}



	@Override
	public void run() {
//		System.out.println("In running:" + this.holdFlag);
		while (true) {
			if (this.holdFlag == 0) {
				try {
					if (Network.network != 2)
					{
						Thread.sleep(5000);
						continue;
					}
					
					Jedis jedis = new Jedis("192.168.103.18");
					jedis.auth("123456redis");
					String value = jedis.lpop("sendQueue");
					System.out.println("read from to send");

					if (value == null) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					} else {
						System.out.println(value);
						MessageObject o = JSON.parseObject(value,
								MessageObject.class);
						if (o.getProperty() != null
								&& o.getPhoneNumber() != null
								&& o.getType() != null) {
							if (o.getType().equals("call")) {
				            	Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+o.getPhoneNumber()));
				            	System.out.println(this.parent);
				            	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				            	this.parent.startActivity(intent);
				            	System.out.println("after call................");
				            	try {
									Thread.sleep(30000);
								} catch (InterruptedException e) {

									e.printStackTrace();
								}
				            	this.parent.endCall();
//				            	endCall();
							} else if (o.getType().equals("sms-info")) {
								String msg = o.getMessage();
								if (msg == null) {
									msg = "[empty message]";
								}
								SmsManager sms = SmsManager.getDefault();
								List<String> texts = sms.divideMessage(msg);
								for (String text : texts) {
									sms.sendTextMessage(o.getPhoneNumber(),
											null, text, null, null);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Redis error:" + e.getMessage());
					if (e instanceof redis.clients.jedis.exceptions.JedisDataException) {
						Jedis jedis = new Jedis("192.168.103.18");
						jedis.auth("123456redis");
						jedis.del("sendQueue");
						System.out.println("delete wrong data");
						continue;
					}
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
					}
				}
			} else {
				break;
			}
		}

	}
}
