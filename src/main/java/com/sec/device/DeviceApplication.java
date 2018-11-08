package com.sec.device;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeviceApplication {

	public static void main(String[] args) {
//		try {
//			MuliServer.Start();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		testChange();
	}


	public static void testChange(){

//		String hexstr = "3A1A2700001014000019270075F9E35B1F00191979002E4B00001800E3860D0A";
//		String resstr = ScheduleCheck.hexStr2Str(hexstr);
//		System.out.println(resstr);

//		String str = "1A2B";
		String str = "3A1A2700001014000019270075F9E35B1F00191979002E4B00001800E3860D0A3A1A2700001014000019270075F9E35B1F00191979002E4B00001800E3860D0A";
//		String reshex = ScheduleCheck.str2HexStr(str);
//		System.out.println(reshex);
		byte[] res = ScheduleCheck.hexStringToBytes(str);
		byte[] v=new byte[4];
		for(int i=4,k=0;i>0;i--,k++){
			v[k]=res[i];
		}
		int ans=ScheduleCheck.byteArrayToInt(v);
		System.out.println(ans);
	}
}
