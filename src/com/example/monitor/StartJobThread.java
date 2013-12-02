package com.example.monitor;

public class StartJobThread implements Runnable {
	private MyService service;

	public StartJobThread(MyService _service) {
		this.service = _service;
	}

	@Override
	public void run() {
		while (true) {
			MonitorProcess mp = new MonitorProcess(this.service, 20);
			System.out.println("start new thread....");
			mp.setHoldFlag(0);
			Thread t = new Thread(mp);

			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
