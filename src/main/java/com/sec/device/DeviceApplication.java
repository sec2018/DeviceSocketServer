package com.sec.device;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeviceApplication {

	public static void main(String[] args) {
		try {
			MuliServer.Start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
