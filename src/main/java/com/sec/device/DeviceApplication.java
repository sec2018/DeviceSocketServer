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

		String str_01 = "3A1A27000001F7970D0A";
		String[] command01 = Command_01(str_01);
		System.out.println(command01.length);


		String str_10 = "3A1a270000101400001927002C1DE55B1F004E067500121200001A000EDD0D0A";
		String[] command10 = Command_10(str_10);
		System.out.println(command10.length);

		String command10_resp = Command_10_Response("10010",true);
		System.out.println(command10_resp);

		String command02_send = Command_02_Send();
		System.out.println(command02_send);


		String str_02 = "3A1a2700000200000D0A";
		String[] command02_receive = Command_02_Receive(str_02);
		System.out.println(command02_receive.length);

		Command_03_Send();

		String str_04 = "3A1A270000041200001927002C1DE55B1F004E0675001212000025160D0A";
		String[] command04 = Command_04(str_04);
		System.out.println(command04.length);
	}

	//心跳反应   01命令接收解析
	public static String[] Command_01(String hexstr){
		if(hexstr.length()!=20){
			return null;
		}
		String[] rescommand_01 = new String[2];
		if(hexstr.substring(0,2).equals("3A") && hexstr.substring(16,20).equals("0D0A")) {
			byte[] res = ScheduleCheck.hexStringToBytes(hexstr);
			//04命令
			//起始码，1个字节    (第1个字节)
			String res1 = "0x" + hexstr.substring(0, 2);
			System.out.println("开始标志： " + res1);
			//设备地址码，4字节，mid   (第2,3,4,5个字节)
			byte[] v = new byte[4];
			for(int i=4,k=0;i>0;i--,k++){
				v[k]=res[i];
			}
			int res2 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("设备地址码： "+res2);
			rescommand_01[0] = res2+"";
			//命令码，1个字节   (第6个字节)
			String res3 = "0x"+hexstr.substring(10,12);
			System.out.println("命令码： "+res3);
			rescommand_01[1] = res3;
		}
		return rescommand_01;
	}

	//发药命令解析  10命令接收解析
	public static String[] Command_10(String hexstr){
		if(hexstr.length()!=64){
			return null;
		}
		String[] rescommand_10 = new String[10];
		if(hexstr.substring(0,2).equals("3A") && hexstr.substring(60,64).equals("0D0A")){
			byte[] res = ScheduleCheck.hexStringToBytes(hexstr);
			//10命令
			//起始码，1个字节    (第1个字节)
			String res1 = "0x"+hexstr.substring(0,2);
			System.out.println("开始标志： "+res1);
			//设备地址码，4字节，mid   (第2,3,4,5个字节)
			byte[] v = new byte[4];
			for(int i=4,k=0;i>0;i--,k++){
				v[k]=res[i];
			}
			int res2 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("设备地址码： "+res2);
			rescommand_10[0] = res2+"";
			//命令码，1个字节   (第6个字节)
			String res3 = "0x"+hexstr.substring(10,12);
			System.out.println("命令码： "+res3);
			rescommand_10[1] = res3;
			//数据长度, 2个字节  小端模式  (第7,8个字节)
			v = new byte[4];
			v[0] = 0;
			v[1] = 0;
			for(int i=7,k=2;i>5;i--,k++){
				v[k]=res[i];
			}
			int res4 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("数据长度： "+res4);
			//region 要写的数据  20个字节
			//投药类型  1个字节    (第9个字节)
			v = new byte[4];
			v[0] = 0;
			v[1] = 0;
			v[2] = 0;
			v[3] = res[8];
			int res51 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("投药类型： "+res51);
			rescommand_10[2] = res51+"";
			//温度  1个字节         (第10个字节)
			v[3] = res[9];
			int res52 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("温度： "+res52);
			rescommand_10[3] = res52+"";
			//电池电压  1个字节      (第11个字节)
			v[3] = res[10];
			int res53 = ScheduleCheck.byteArrayToInt(v);
			double res53_voltage = res53/10.0;
			System.out.println("电池电压： "+res53_voltage);
			rescommand_10[4] = res53_voltage+"";
			//故障信息  1个字节      (第12个字节)
			v[3] = res[11];
			int res54 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("故障信息： "+res54);
			rescommand_10[5] = res54+"";
			//当前投药时间GMT  4个字节  小端模式   (第13,14,15,16个字节)
			for(int i=15,k=0;i>11;i--,k++){
				v[k]=res[i];
			}
			int res55 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("当前投药时间GMT： "+res55);
			rescommand_10[6] = res55+"";
			//维度  2个字节（小端模式）+1个字节+1个字节     (第17,18,19,20个字节)
			v[0] = 0;
			v[1] = 0;
			for(int i=17,k=2;i>15;i--,k++){
				v[k]=res[i];
			}
			int res56_latdegree = ScheduleCheck.byteArrayToInt(v);
			v[2] = 0;
			v[3] = res[18];
			int res56_latcent = ScheduleCheck.byteArrayToInt(v);
			v[3] = res[19];
			int res56_latsecond = ScheduleCheck.byteArrayToInt(v);
			String res56 = res56_latdegree+"."+res56_latcent+res56_latsecond;
			System.out.println("维度： "+res56);
			rescommand_10[7] = res56;
			//经度  2个字节（小端模式）+1个字节+1个字节    (第21,22,23,24个字节)
			v[0] = 0;
			v[1] = 0;
			for(int i=21,k=2;i>19;i--,k++){
				v[k]=res[i];
			}
			int res57_lngdegree = ScheduleCheck.byteArrayToInt(v);
			v[2] = 0;
			v[3] = res[22];
			int res57_latcent = ScheduleCheck.byteArrayToInt(v);
			v[3] = res[23];
			int res57_latsecond = ScheduleCheck.byteArrayToInt(v);
			String res57 = res57_lngdegree+"."+res57_latcent+res57_latsecond;
			System.out.println("经度： "+res57);
			rescommand_10[8] = res57;
			//投药寄存器状态  2个字节  小端模式   (第25，26个字节)
			v[0] = 0;
			v[1] = 0;
			for(int i=25,k=2;i>23;i--,k++){
				v[k]=res[i];
			}
			int res58 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("投药寄存器状态： "+res58);
			rescommand_10[9] = res58+"";
			//endregion
			//CRC16 2个字节  小端模式         (第29，30个字节)
			v[0] = 0;
			v[1] = 0;
			for(int i=29,k=2;i>27;i--,k++){
				v[k]=res[i];
			}
			int res6 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("CRC16： "+res6);
			//结束码 1                       (第31个字节)
			String res7= "0x"+hexstr.substring(60,62);
			System.out.println(res7);
			//结束码 2                       (第32个字节)
			String res8= "0x"+hexstr.substring(62,64);
			System.out.println(res8);
		}
		return rescommand_10;
	}

	//10命令响应
	public static String Command_10_Response(String mid,boolean flag){
		String resp = "";
		int Mid = Integer.parseInt(mid);
		if(flag){
			byte[] v = new byte[4];
			//设置高位在前
			Mid = Integer.reverseBytes(Mid);
			v = ScheduleCheck.intToByteArray(Mid);
			return "3A"+ScheduleCheck.bytesToHexString(v).toUpperCase()+"04"+"0000"+"0D0A";
		}
		return "";
	}

	//02命令发送
	public static String Command_02_Send(){
		String mid = "10010";
		//传入12个投药时间对象
		int[] time_all = new int[12];
		int time_01 = 1541741868;
		int time_02 = 1541751868;
		int time_03 = 1541761868;
		int time_04 = 1541771868;
		int time_05 = 1541781868;
		int time_06 = 1541791868;
		int time_07 = 1541801868;
		int time_08 = 1541811868;
		int time_09 = 1541821868;
		int time_10 = 1541831868;
		int time_11 = 1541841868;
		int time_12 = 1541851868;
		time_all[0] = time_01;time_all[1] = time_02;time_all[2] = time_03;time_all[3] = time_04;time_all[4] = time_05;time_all[5] = time_06;
		time_all[6] = time_07;time_all[7] = time_08;time_all[8] = time_09;time_all[9] = time_10;time_all[10] = time_11;time_all[11] = time_12;
		String resp = "";
		int Mid = Integer.parseInt(mid);

		byte[] v = new byte[4];
		//设置高位在前
		Mid = Integer.reverseBytes(Mid);
		v = ScheduleCheck.intToByteArray(Mid);
		String midstr = ScheduleCheck.bytesToHexString(v).toUpperCase();
		for (int i=0;i<=11;i++){
			//设置高位在前
			time_all[i] = Integer.reverseBytes(time_all[i]);
			v = ScheduleCheck.intToByteArray(time_all[i]);
			resp += ScheduleCheck.bytesToHexString(v).toUpperCase();
		}
		resp = "3A"+midstr+"02"+"0000"+resp+"0000"+"0D0A";
		return resp;
	}

	//02命令  项圈响应解析
	public static String[] Command_02_Receive(String hexstr){
		if(hexstr.length()!=20){
			return null;
		}
		String[] rescommand_02 = new String[2];
		if(hexstr.substring(0,2).equals("3A") && hexstr.substring(16,20).equals("0D0A")) {
			byte[] res = ScheduleCheck.hexStringToBytes(hexstr);
			//02命令
			//起始码，1个字节    (第1个字节)
			String res1 = "0x" + hexstr.substring(0, 2);
			System.out.println("开始标志： " + res1);
			//设备地址码，4字节，mid   (第2,3,4,5个字节)
			byte[] v = new byte[4];
			for(int i=4,k=0;i>0;i--,k++){
				v[k]=res[i];
			}
			int res2 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("设备地址码： "+res2);
			rescommand_02[0] = res2+"";
			//命令码，1个字节   (第6个字节)
			String res3 = "0x"+hexstr.substring(10,12);
			System.out.println("命令码： "+res3);
			rescommand_02[1] = res3;
		}
		return rescommand_02;
	}

	//03命令  发送配置信息
	public static String Command_03_Send(){
		String mid = "10010";

		String resp = "";
		int Mid = Integer.parseInt(mid);

		byte[] v = new byte[4];
		//设置高位在前
		Mid = Integer.reverseBytes(Mid);
		v = ScheduleCheck.intToByteArray(Mid);
		String midstr = ScheduleCheck.bytesToHexString(v).toUpperCase();

		//服务器IP地址
		String ip = "122.112.252.45";
		String[] iparr = ip.split("\\.");
		String ipres = "";
		for(int i=0;i<=3;i++){
			int a = Integer.parseInt(iparr[i]);
			v = ScheduleCheck.intToByteArray(a);
			ipres += ScheduleCheck.bytesToHexString(v).toUpperCase().substring(6,8);
		}
		//端口号
		String port = "59999";
		int Port = Integer.parseInt(port);
		Port = Integer.reverseBytes(Port);
		v = ScheduleCheck.intToByteArray(Port);
		String portres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,4);
		//

		resp = "3A"+midstr+"02"+"0000"+resp+"0000"+"0D0A";
		return resp;
	}

	//04命令   项圈响应解析
	public static String[] Command_04(String hexstr){
		if(hexstr.length()!=60){
			return null;
		}
		String[] rescommand_04 = new String[2];
		if(hexstr.substring(0,2).equals("3A") && hexstr.substring(16,20).equals("0D0A")) {
			byte[] res = ScheduleCheck.hexStringToBytes(hexstr);
			//04命令
			//起始码，1个字节    (第1个字节)
			String res1 = "0x" + hexstr.substring(0, 2);
			System.out.println("开始标志： " + res1);
			//设备地址码，4字节，mid   (第2,3,4,5个字节)
			byte[] v = new byte[4];
			for(int i=4,k=0;i>0;i--,k++){
				v[k]=res[i];
			}
			int res2 = ScheduleCheck.byteArrayToInt(v);
			System.out.println("设备地址码： "+res2);
			rescommand_04[0] = res2+"";
			//命令码，1个字节   (第6个字节)
			String res3 = "0x"+hexstr.substring(10,12);
			System.out.println("命令码： "+res3);
			rescommand_04[1] = res3;
			//其他信息

		}
		return rescommand_04;
	}
}
