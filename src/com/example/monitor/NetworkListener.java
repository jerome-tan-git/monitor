package com.example.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import redis.clients.jedis.Jedis;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

public class NetworkListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		State wifiState = null;
		State mobileState = null;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		if (wifiState != null && mobileState != null
				&& State.CONNECTED != wifiState
				&& State.CONNECTED == mobileState) {
			Network.network = 1;
			System.out.println("GPRS");
//			Jedis jedis = new Jedis("192.168.103.18");
//			jedis.auth("123456redis");
//			jedis.rpush("network_change", "gprs |" + new Date().toLocaleString());

		} else if (wifiState != null && mobileState != null
				&& State.CONNECTED != wifiState
				&& State.CONNECTED != mobileState) {
			Network.network= 0;
			System.out.println("offline");
//			Jedis jedis = new Jedis("192.168.103.18");
//			jedis.auth("123456redis");
//			jedis.rpush("network_change", "offline | " + new Date().toLocaleString());

		} else if (wifiState != null && State.CONNECTED == wifiState) {
			Network.network = 2; 
			System.out.println("wifi");
			Jedis jedis = new Jedis("192.168.103.18");
			jedis.auth("123456redis");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
			
			jedis.rpush("network_change", "wifi | " + sdf.format(new Date()));

		}

	}

}
