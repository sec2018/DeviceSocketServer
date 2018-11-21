package com.sec.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DeviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeviceApplication.class, args);
		try {
			MuliServer.Start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}



//		String str_01 = "3A1A27000001F7970D0A";
//		String[] command01 = Command_01(str_01);
//		System.out.println(command01.length);
//
//
//		String str_10 = "3A1A27000010140000752500AEC4E75B1F004E067500121200001500460E0D0A";
//		String[] command10 = Analyse.Command_10(str_10);
//		System.out.println(command10.length);
//
//		String command10_resp = Analyse.Command_10_Response("10010",true);
//		System.out.println(command10_resp);
//
//		String command02_send = Analyse.Command_02_Send();
//		System.out.println(command02_send);
//
//
//		String str_02 = "3A1a2700000200000D0A";
//		String[] command02_receive = Analyse.Command_02_Receive(str_02);
//		System.out.println(command02_receive.length);
//
//		String command03_send = Analyse.Command_03_Send();
//		System.out.println(command03_send);
//
//		String str_03 = "3A1a2700000300000D0A";
//		String[] command03_receive = Analyse.Command_03_Receive(str_03);
//		System.out.println(command03_receive.length);
//
//		String command04_send = Analyse.Command_04_Send();
//		System.out.println(command04_send);
//
//		String str_04 = "3A1A2700000412007A70FC2D5FEA05001E00000000000000000061A50D0A";
//		String[] command04 = Analyse.Command_04_Receive(str_04);
//		System.out.println(command04.length);

//		Command_05_Send();
//
//		String str_05 = "3A1A270000051C001A270000B4591401383938363034313131303138373131333932323593A10D0A";
//		String[] command05 = Analyse.Command_05_Receive(str_05);
//		System.out.println(command05.length);


//		Command_06_Send();
//
//		String str_06 = "3A1A270000063000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D2F40D0A";
//		String[] command06 = Analyse.Command_06_Receive(str_06);
//		System.out.println(command06.length);
	}
}
