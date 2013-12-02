package com.example.monitor;

import java.util.List;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.telephony.SmsManager;

public class MonitorProcess implements Runnable {
	private MyService parent;
	private int tryTimes = 100;

	public MonitorProcess(MyService _parent, int _times) {
		this.parent = _parent;
		this.tryTimes = _times;
	}

	private int holdFlag = 0;

	public int getHoldFlag() {
		return holdFlag;
	}

	public void setHoldFlag(int holdFlag) {
		this.holdFlag = holdFlag;
	}

	private boolean inSleepTime(Jedis _jedis, String _type, String _phoneNumber) {
		boolean result = false;
		if (_type.trim().equals("call")) {
			List<String> lastEvent = _jedis.lrange("access_call:"
					+ _phoneNumber, 0, 2);
			if (lastEvent == null || lastEvent.size() == 0) {
				lastEvent = _jedis.lrange("access_call:+86" + _phoneNumber, 0,
						2);
			}
			if (lastEvent != null && lastEvent.size() >= 3) {
				String lastStr = lastEvent.get(lastEvent.size() - 1);
				long lastStrTime = System.currentTimeMillis();
				try {
					lastStrTime = Long.parseLong(lastStr);
				} catch (Exception e) {
				}
				if (System.currentTimeMillis() - lastStrTime < 3 * 60 * 1000) {
					System.out.println("overload, last msg : " + lastStr
							+ " : "
							+ (System.currentTimeMillis() - lastStrTime));
					result = true;
				}
			}
		} else if (_type.trim().equals("msg")) {
			List<String> lastEvent = _jedis.lrange(
					"access_msg:" + _phoneNumber, 0, 2);
			if (lastEvent == null || lastEvent.size() == 0) {
				lastEvent = _jedis
						.lrange("access_msg:+86" + _phoneNumber, 0, 2);
			}
			if (lastEvent != null && lastEvent.size() >= 3) {
				String lastStr = lastEvent.get(lastEvent.size() - 1);
				long lastStrTime = System.currentTimeMillis();
				try {
					lastStrTime = Long.parseLong(lastStr);
				} catch (Exception e) {
				}
				if (System.currentTimeMillis() - lastStrTime < 10 * 60 * 1000) {
					System.out.println("overload, last msg : " + lastStr
							+ " : "
							+ (System.currentTimeMillis() - lastStrTime));
					result = true;
				}

			}
		}

		return result;
	}

	private boolean ifValidNumber(Jedis _jedis, String _phoneNumber) {

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

	private boolean isTmpPhone(Jedis _jedis, String _phoneNumber) {
		int tmpCount = 0;
		if (_jedis.get("tmpphone:+86" + _phoneNumber) != null) {
			tmpCount = Integer.parseInt(_jedis.get("tmpphone:+86"
					+ _phoneNumber));
		} else if (_jedis.get("tmpphone:" + _phoneNumber) != null) {
			tmpCount = Integer.parseInt(_jedis.get("tmpphone:" + _phoneNumber));
		}
		if (tmpCount <= 0)
			return false;
		else
			return true;
	}

	private boolean isQueued(Jedis _jedis, String _phoneNumber) {
		String getStr = _jedis.hget("snooze", _phoneNumber);
		if (getStr == null) {
			getStr = _jedis.hget("snooze", "+86" + _phoneNumber);
		}
		if (getStr == null) {
			if (_phoneNumber.startsWith("+86")) {
				getStr = _jedis.hget("snooze",
						_phoneNumber.replaceAll("\\+86", ""));
			}
		}
		System.out.println("z str:" + getStr);
		if (getStr != null && !getStr.trim().equals("")) {

			try {
				long toDate = Long.parseLong(getStr);
				if (toDate > System.currentTimeMillis()) {
					System.out.println("Is queued: " + _phoneNumber);
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public void run() {
		// System.out.println("In running:" + this.holdFlag);
		int tryCount = 0;
		while (tryCount <= tryTimes) {
			tryCount++;
			if (this.holdFlag == 0) {
				try {
					if (parent.getNetworkStatus() != ConnectivityManager.TYPE_WIFI) {

						parent.restartWIFI();
						Thread.sleep(50000);
						continue;
					}
					Jedis jedis = new Jedis("192.168.103.18");
					try {
						jedis.auth("123456redis");
					} catch (Exception e) {
						Thread.sleep(2000);
						continue;
					}

					// List<String> todo = jedis.blpop(60, "sendQueue");
					String task = jedis.lpop("sendQueue");
					System.out.println("read from to send");
					if (task == null) {
						try {
							Thread.sleep(2000);
							continue;
						} catch (InterruptedException e) {
						}
					}

					String value = task;
					if (value == null) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					} else {
						System.out.println(value.trim());
						MessageObject o;
						try {
							o = JSON.parseObject(value, MessageObject.class);
						} catch (Exception e) {
							continue;
						}

						if (o != null && o.getProperty() != null
								&& o.getPhoneNumber() != null
								&& o.getType() != null) {

							if (o.getType().equals("call")) {
								System.out.println("valid number: "
										+ this.ifValidNumber(jedis,
												o.getPhoneNumber()));
								System.out.println("tmp phone: "
										+ this.isTmpPhone(jedis,
												o.getPhoneNumber()));
								System.out.println("sleep: "
										+ this.inSleepTime(jedis, "call",
												o.getPhoneNumber()));
								if (this.ifValidNumber(jedis,
										o.getPhoneNumber())) {
									if (!this.inSleepTime(jedis, "call",
											o.getPhoneNumber())) {
										if (!this.isQueued(jedis,
												o.getPhoneNumber())) {
											jedis.lpush(
													"access_call:"
															+ o.getPhoneNumber(),
													System.currentTimeMillis()
															+ "");
											jedis.ltrim(
													"access_call:"
															+ o.getPhoneNumber(),
													0, 2);
											Intent intent = new Intent(
													Intent.ACTION_CALL,
													Uri.parse("tel:"
															+ o.getPhoneNumber()));
											System.out.println("make call: "
													+ o.getPhoneNumber());
											intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											this.parent.startActivity(intent);

											try {
												Thread.sleep(30000);
											} catch (InterruptedException e) {

												e.printStackTrace();
											}
											this.parent.endCall();
										}
									}
								}
								// endCall();
							} else if (o.getType().equals("sms-info")) {

								if (this.ifValidNumber(jedis,
										o.getPhoneNumber())) {
									if (this.isTmpPhone(jedis,
											o.getPhoneNumber())
											|| !this.inSleepTime(jedis, "msg",
													o.getPhoneNumber())) {
										if (!this.isQueued(jedis,
												o.getPhoneNumber())) {
											jedis.lpush(
													"access_msg:"
															+ o.getPhoneNumber(),
													System.currentTimeMillis()
															+ "");
											jedis.ltrim(
													"access_msg:"
															+ o.getPhoneNumber(),
													0, 2);
											String msg = o.getMessage();
											System.out.println("send messag: "
													+ o.getPhoneNumber()
													+ "  |  " + msg);
											if (msg == null) {
												msg = "[empty message]";
											}
											SmsManager sms = SmsManager
													.getDefault();
											List<String> texts = sms
													.divideMessage(msg);
											for (String text : texts) {
												sms.sendTextMessage(
														o.getPhoneNumber(),
														null, text, null, null);
											}
											if (this.isTmpPhone(jedis,
													o.getPhoneNumber())) {
												if (jedis.get("tmpphone:+86"
														+ o.getPhoneNumber()) != null) {
													jedis.decr("tmpphone:+86"
															+ o.getPhoneNumber());
												}
												if (jedis.get("tmpphone:"
														+ o.getPhoneNumber()) != null) {
													jedis.decr("tmpphone:"
															+ o.getPhoneNumber());
												}
											}
										}
									}
								}
							}
						}
					}
					// catch

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
