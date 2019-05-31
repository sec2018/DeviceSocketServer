package com.sec.device.util.common;

import com.sec.device.config.MuliScheduleCheck;
import com.sec.device.config.ScheduleCheck;
import com.sec.device.pojo.SysDeviceconf;
import com.sec.device.pojo.SysLayconfig;

public class Analyse {
    //心跳反应   01命令接收解析
    public static String[] Command_01(String hexstr){
        if(hexstr.length()!=20){
            return null;
        }
        String[] rescommand_01 = new String[2];
        if(hexstr.substring(0,2).equals("3A") && hexstr.substring(16,20).equals("0D0A")) {
            byte[] res = MuliScheduleCheck.hexStringToBytes(hexstr);
            //04命令
            //起始码，1个字节    (第1个字节)
            String res1 = "0x" + hexstr.substring(0, 2);
            System.out.println("开始标志： " + res1);
            //设备地址码，4字节，mid   (第2,3,4,5个字节)
            byte[] v = new byte[4];
            for(int i=4,k=0;i>0;i--,k++){
                v[k]=res[i];
            }
            int res2 = MuliScheduleCheck.byteArrayToInt(v);
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
        String[] rescommand_10 = new String[13];
        if(hexstr.substring(0,2).equals("3A") && hexstr.substring(60,64).equals("0D0A")){
            byte[] res = MuliScheduleCheck.hexStringToBytes(hexstr);
            //10命令
            //起始码，1个字节    (第1个字节)
            String res1 = "0x"+hexstr.substring(0,2);
            System.out.println("开始标志： "+res1);
            //设备地址码，4字节，mid   (第2,3,4,5个字节)
            byte[] v = new byte[4];
            for(int i=4,k=0;i>0;i--,k++){
                v[k]=res[i];
            }
//            int sta = res[1] + res[2]*256;
//            int sta1 = res[1] + res[2]*16;
//            int sta2 = res[1] + (res[2]&0x0F)*256;   //0x0F 屏蔽4位   0x00屏蔽8位
//            int sta3 = res[2]>>8;                    //右移8位
//            int sta4 = res[2]&0x0F;
            int res2 = MuliScheduleCheck.byteArrayToInt(v);
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
            int res4 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("数据长度： "+res4);
            //region 要写的数据  20个字节
            //投药类型  1个字节    (第9个字节)
            v = new byte[4];
            v[0] = 0;
            v[1] = 0;
            v[2] = 0;
            v[3] = res[8];
            int res51 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药类型： "+res51);
            rescommand_10[2] = res51+"";
            //温度  1个字节         (第10个字节)
            v[3] = res[9];
            int res52 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("温度： "+res52);
            rescommand_10[3] = res52+"";
            //电池电压  1个字节      (第11个字节)
            v[3] = res[10];
            int res53 = MuliScheduleCheck.byteArrayToInt(v);
            double res53_voltage = res53/10.0;
            System.out.println("电池电压： "+res53_voltage);
            rescommand_10[4] = res53_voltage+"";
            //故障信息  1个字节      (第12个字节)
            v[3] = res[11];
            int res54 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("故障信息： "+res54);
            rescommand_10[5] = res54+"";
            //当前投药时间GMT  4个字节  小端模式   (第13,14,15,16个字节)
            for(int i=15,k=0;i>11;i--,k++){
                v[k]=res[i];
            }
            int res55 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("当前投药时间GMT： "+res55);
            rescommand_10[6] = res55+"";
            //维度  2个字节（小端模式）+1个字节+1个字节     (第17,18,19,20个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=17,k=2;i>15;i--,k++){
                v[k]=res[i];
            }
            int res56_latdegree = MuliScheduleCheck.byteArrayToInt(v);
            v[2] = 0;
            v[3] = res[18];
            int res56_latcent = MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[19];
            int res56_latsecond = MuliScheduleCheck.byteArrayToInt(v);
            String res56 = res56_latdegree+"."+res56_latcent+res56_latsecond;
            System.out.println("维度： "+res56);
            rescommand_10[7] = res56;
            //经度  2个字节（小端模式）+1个字节+1个字节    (第21,22,23,24个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=21,k=2;i>19;i--,k++){
                v[k]=res[i];
            }
            int res57_lngdegree = MuliScheduleCheck.byteArrayToInt(v);
            v[2] = 0;
            v[3] = res[22];
            int res57_latcent = MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[23];
            int res57_latsecond = MuliScheduleCheck.byteArrayToInt(v);
            String res57 = res57_lngdegree+"."+res57_latcent+res57_latsecond;
            System.out.println("经度： "+res57);
            rescommand_10[8] = res57;
 //           //投药寄存器状态  2个字节  小端模式   (第25，26个字节)
//            v[0] = 0;
//            v[1] = 0;
//            for(int i=25,k=2;i>23;i--,k++){
//                v[k]=res[i];
//            }
//            int res58 = MuliScheduleCheck.byteArrayToInt(v);
//            System.out.println("投药寄存器状态： "+res58);
//            rescommand_10[9] = res58+"";

            //轮询次数  投药寄存器状态  2个字节  小端模式   (第25，26个字节)
            //            int sta2 = res[1] + (res[2]&0x0F)*256;   //0x0F 屏蔽4位   0x00屏蔽8位
            //轮询次数  右移4位
            int res581 = res[25] >> 4;
            //投药寄存器状态
            int res582 = res[24] + (res[25]&0x0F)*256;
            System.out.println("轮询次数： "+res581);
            rescommand_10[9] = res581+"";
            System.out.println("投药寄存器状态： "+res582);
            rescommand_10[10] = res582+"";
            //gsm信号强度 2个字节  小端模式   (第27，28个字节)
//            v[0] = 0;
//            v[1] = 0;
//            for(int i=27,k=2;i>25;i--,k++){
//                v[k]=res[i];
//            }
//            int res59 = ScheduleCheck.byteArrayToInt(v);
//            System.out.println("gsm信号强度： "+res59);
//            rescommand_10[10] = res59+"";
            //gsm信号强度 1个字节    (第26个字节)
            v[3] = res[26];
            int res591 = ScheduleCheck.byteArrayToInt(v);
            System.out.println("gsm信号强度： "+res591);
            rescommand_10[11] = res591+"";

            //投药编号  1个字节      (第27个字节)
            v[3] = res[27];
            int res592 = ScheduleCheck.byteArrayToInt(v);
            System.out.println("投药编号： "+res592);
            rescommand_10[12] = res592+"";


            //endregion
            //CRC16 2个字节  小端模式         (第29，30个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=29,k=2;i>27;i--,k++){
                v[k]=res[i];
            }
            int res6 = MuliScheduleCheck.byteArrayToInt(v);
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
            v = MuliScheduleCheck.intToByteArray(Mid);
            return "3A"+ MuliScheduleCheck.bytesToHexString(v).toUpperCase()+"10"+"0000"+"0D0A";
        }
        return "";
    }

    //02命令发送
    public static String Command_02_Send(SysLayconfig sysLayconfig){
//        String mid = "10010";
        String mid = sysLayconfig.getMid();
        //传入12个投药时间对象
        int[] time_all = new int[12];
//        int time_01 = 1541741868;
//        int time_02 = 1541751868;
//        int time_03 = 1541761868;
//        int time_04 = 1541771868;
//        int time_05 = 1541781868;
//        int time_06 = 1541791868;
//        int time_07 = 1541801868;
//        int time_08 = 1541811868;
//        int time_09 = 1541821868;
//        int time_10 = 1541831868;
//        int time_11 = 1541841868;
//        int time_12 = 1541851868;
        int time_01 = (int)(sysLayconfig.getOne().getTime()/1000);
        int time_02 = (int)(sysLayconfig.getTwo().getTime()/1000);
        int time_03 = (int)(sysLayconfig.getThree().getTime()/1000);
        int time_04 = (int)(sysLayconfig.getFour().getTime()/1000);
        int time_05 = (int)(sysLayconfig.getFive().getTime()/1000);
        int time_06 = (int)(sysLayconfig.getSix().getTime()/1000);
        int time_07 = (int)(sysLayconfig.getSeven().getTime()/1000);
        int time_08 = (int)(sysLayconfig.getEight().getTime()/1000);
        int time_09 = (int)(sysLayconfig.getNine().getTime()/1000);
        int time_10 = (int)(sysLayconfig.getTen().getTime()/1000);
        int time_11 = (int)(sysLayconfig.getEleven().getTime()/1000);
        int time_12 = (int)(sysLayconfig.getTwelve().getTime()/1000);
        time_all[0] = time_01;time_all[1] = time_02;time_all[2] = time_03;time_all[3] = time_04;time_all[4] = time_05;time_all[5] = time_06;
        time_all[6] = time_07;time_all[7] = time_08;time_all[8] = time_09;time_all[9] = time_10;time_all[10] = time_11;time_all[11] = time_12;
        String resp = "";
        int Mid = Integer.parseInt(mid);

        byte[] v = new byte[4];
        //设置高位在前
        Mid = Integer.reverseBytes(Mid);
        v = MuliScheduleCheck.intToByteArray(Mid);
        String midstr = MuliScheduleCheck.bytesToHexString(v).toUpperCase();
        for (int i=0;i<=11;i++){
            //设置高位在前
            time_all[i] = Integer.reverseBytes(time_all[i]);
            v = MuliScheduleCheck.intToByteArray(time_all[i]);
            resp += MuliScheduleCheck.bytesToHexString(v).toUpperCase();
        }
        resp = "3A"+midstr+"02"+"3000"+resp+"0000"+"0D0A";
        return resp;
    }

    //02命令  项圈响应解析
    public static String[] Command_02_Receive(String hexstr){
        if(hexstr.length()!=20){
            return null;
        }
        String[] rescommand_02 = new String[2];
        if(hexstr.substring(0,2).equals("3A") && hexstr.substring(16,20).equals("0D0A")) {
            byte[] res = MuliScheduleCheck.hexStringToBytes(hexstr);
            //02命令
            //起始码，1个字节    (第1个字节)
            String res1 = "0x" + hexstr.substring(0, 2);
            System.out.println("开始标志： " + res1);
            //设备地址码，4字节，mid   (第2,3,4,5个字节)
            byte[] v = new byte[4];
            for(int i=4,k=0;i>0;i--,k++){
                v[k]=res[i];
            }
            int res2 = MuliScheduleCheck.byteArrayToInt(v);
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
    public static String Command_03_Send(SysDeviceconf sysDeviceconf){

//        String mid = "10010";
        String mid = sysDeviceconf.getMid();

        String resp = "";
        int Mid = Integer.parseInt(mid);

        byte[] v = new byte[4];
        //设置高位在前
        Mid = Integer.reverseBytes(Mid);
        v = ScheduleCheck.intToByteArray(Mid);
        String midstr = ScheduleCheck.bytesToHexString(v).toUpperCase();

        //服务器IP地址
//        String ip = "122.112.252.45";
        String ip = sysDeviceconf.getIp();
        if(ip==null){
            ip = "0.0.0.0";
        }
        String[] iparr = ip.split("\\.");
        String ipres = "";
        for(int i=0;i<=3;i++){
            int a = Integer.parseInt(iparr[i]);
            v = ScheduleCheck.intToByteArray(a);
            ipres += ScheduleCheck.bytesToHexString(v).toUpperCase().substring(6,8);
        }
        //端口号
//        String port = "59999";
//        int Port = Integer.parseInt(port);
        int Port = 0;
        if(sysDeviceconf.getPort() != null){
            Port = sysDeviceconf.getPort();
        }
        Port = Integer.reverseBytes(Port);
        v = ScheduleCheck.intToByteArray(Port);
        String portres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,4);
        //投药信息上传周期
//        int infoupdatecycle = 45;
        int infoupdatecycle = 0;
        if(sysDeviceconf.getInfoupdatecycle() != null){
            infoupdatecycle = sysDeviceconf.getInfoupdatecycle();
        }
        infoupdatecycle = Integer.reverseBytes(infoupdatecycle);
        v = ScheduleCheck.intToByteArray(infoupdatecycle);
        String infoupdatecycleres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,4);
        //心跳周期
//        int tickcycle = 10000;
        int tickcycle = 0;
        if(sysDeviceconf.getTickcycle() != null){
            tickcycle = sysDeviceconf.getTickcycle();
        }
        tickcycle = Integer.reverseBytes(tickcycle);
        v = ScheduleCheck.intToByteArray(tickcycle);
        String tickcycleres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,4);
        //LED使能
//        int ledenable = 1;
        int ledenable = 0;
        if(sysDeviceconf.getLedenable() != null){
            ledenable = sysDeviceconf.getLedenable();
        }
        ledenable = Integer.reverseBytes(ledenable);
        v = ScheduleCheck.intToByteArray(ledenable);
        String ledenableres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,2);
        //临时投药标志
//        int tempflag = 1;
        int tempflag = 0;
        if(sysDeviceconf.getTemporaryflag() != null){
            tempflag = sysDeviceconf.getTemporaryflag();
        }
        tempflag = Integer.reverseBytes(tempflag);
        v = ScheduleCheck.intToByteArray(tempflag);
        String tempflagres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,2);
        //临时投药时间
//        int tempgmt = 1561123123;
        int tempgmt = 0;
        if(sysDeviceconf.getTemporarygmt() != null){
            tempgmt = (int)(sysDeviceconf.getTemporarygmt().getTime()/1000);
        }
        tempgmt = Integer.reverseBytes(tempgmt);
        v = ScheduleCheck.intToByteArray(tempgmt);
        String tempgmtres = ScheduleCheck.bytesToHexString(v).toUpperCase();
        //clearErr  清除故障标志位
        int clearErr = 0;
        if(sysDeviceconf.getClearerr() != null){
            clearErr = sysDeviceconf.getClearerr();
        }
        clearErr = Integer.reverseBytes(clearErr);
        v = ScheduleCheck.intToByteArray(clearErr);
        String clearErrres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,2);
        //恢复出厂设置
        int factory = 0;
        if(sysDeviceconf.getFactory() != null){
            factory = sysDeviceconf.getFactory();
        }
        factory = Integer.reverseBytes(factory);
        v = ScheduleCheck.intToByteArray(factory);
        String factoryres = ScheduleCheck.bytesToHexString(v).toUpperCase().substring(0,2);

        resp = "3A"+midstr+"03"+"1200"+ipres+portres+infoupdatecycleres+tickcycleres+ledenableres+tempflagres+tempgmtres+clearErrres+factoryres+"0000"+"0D0A";
        return resp;
    }

    //03命令  项圈响应解析
    public static String[] Command_03_Receive(String hexstr){
        if(hexstr.length()!=20){
            return null;
        }
        String[] rescommand_03 = new String[2];
        if(hexstr.substring(0,2).equals("3A") && hexstr.substring(16,20).equals("0D0A")) {
            byte[] res = MuliScheduleCheck.hexStringToBytes(hexstr);
            //03命令
            //起始码，1个字节    (第1个字节)
            String res1 = "0x" + hexstr.substring(0, 2);
            System.out.println("开始标志： " + res1);
            //设备地址码，4字节，mid   (第2,3,4,5个字节)
            byte[] v = new byte[4];
            for(int i=4,k=0;i>0;i--,k++){
                v[k]=res[i];
            }
            int res2 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("设备地址码： "+res2);
            rescommand_03[0] = res2+"";
            //命令码，1个字节   (第6个字节)
            String res3 = "0x"+hexstr.substring(10,12);
            System.out.println("命令码： "+res3);
            rescommand_03[1] = res3;
        }
        return rescommand_03;
    }

    //04命令  发送配置信息
    public static String Command_04_Send(String mid){
        String resp = "";
        int Mid = Integer.parseInt(mid);

        byte[] v = new byte[4];
        //设置高位在前
        Mid = Integer.reverseBytes(Mid);
        v = MuliScheduleCheck.intToByteArray(Mid);
        String midstr = MuliScheduleCheck.bytesToHexString(v).toUpperCase();
        resp = "3A"+midstr+"04"+"0000"+"0D0A";
        return resp;
    }

    //04命令   项圈响应解析
    public static String[] Command_04_Receive(String hexstr){
        if(hexstr.length()!=60){
            return null;
        }
        String[] rescommand_04 = new String[11];
        if(hexstr.substring(0,2).equals("3A") && hexstr.substring(56,60).equals("0D0A")) {
            byte[] res = MuliScheduleCheck.hexStringToBytes(hexstr);
            //04命令
            //起始码，1个字节    (第1个字节)
            String res1 = "0x" + hexstr.substring(0, 2);
            System.out.println("开始标志： " + res1);
            //设备地址码，4字节，mid   (第2,3,4,5个字节)
            byte[] v = new byte[4];
            for(int i=4,k=0;i>0;i--,k++){
                v[k]=res[i];
            }
            int res2 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("设备地址码： "+res2);
            rescommand_04[0] = res2+"";
            //命令码，1个字节   (第6个字节)
            String res3 = "0x"+hexstr.substring(10,12);
            System.out.println("命令码： "+res3);
            rescommand_04[1] = res3;
            //ip
            //数据长度, 2个字节  小端模式  (第7,8个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=7,k=2;i>5;i--,k++){
                v[k]=res[i];
            }
            int res4 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("数据长度： "+res4);
            //region 要读的数据  18个字节
            //ip  4个字节    (第9,10,11,12个字节)
            v[0] = 0;
            v[1] = 0;
            v[2] = 0;
            v[3] = res[8];
            int res51_1 = MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[9];
            int res51_2 = MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[10];
            int res51_3 = MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[11];
            int res51_4 = MuliScheduleCheck.byteArrayToInt(v);
            String res51 = res51_1+"."+res51_2+"."+res51_3+"."+res51_4;
            System.out.println("ip地址： "+res51);
            rescommand_04[2] = res51;
            //端口号  2个字节         (第13,14个字节)
            v[0] = 0;
            v[1] = 0;
            v[2] = res[13];
            v[3] = res[12];
            int res52 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("端口号： "+res52);
            rescommand_04[3] = res52+"";
            //投药信息上传周期  2个字节      (第15,16个字节)
            v[0] = 0;
            v[1] = 0;
            v[2] = res[15];
            v[3] = res[14];
            int res53 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药信息上传周期： "+res53);
            rescommand_04[4] = res53+"";
            //心跳周期  2个字节      (第17,18个字节)
            v[0] = 0;
            v[1] = 0;
            v[2] = res[17];
            v[3] = res[16];
            int res54 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("心跳周期： "+res54);
            rescommand_04[5] = res54+"";
            //LED使能  1个字节      (第19个字节)
            v[0] = 0;
            v[1] = 0;
            v[2] = 0;
            v[3] = res[18];
            int res55 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("LED使能： "+res55);
            rescommand_04[6] = res55+"";
            //临时投药标志  1个字节      (第20个字节)
            v[3] = res[19];
            int res56 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("临时投药标志： "+res56);
            rescommand_04[7] = res56+"";
            //临时投药时间  4个字节      (第21,22,23,24个字节)
            v[0] = res[23];
            v[1] = res[22];
            v[2] = res[21];
            v[3] = res[20];
            int res57 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("临时投药时间： "+res57);
            rescommand_04[8] = res57+"";
            //ClearErr  清除故障标志位 1个字节  （第25个字节）
            v[0] = 0;
            v[1] = 0;
            v[2] = 0;
            v[3] = res[24];
            int res58 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("清除故障标志位： "+res58);
            rescommand_04[9] = res58+"";
            v[3] = res[25];
            int res59 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("恢复出厂设置： "+res59);
            rescommand_04[10] = res59+"";

            //CRC16 2个字节  小端模式         (第27，28个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=27,k=2;i>26;i--,k++){
                v[k]=res[i];
            }
            int res6 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("CRC16： "+res6);
        }
        return rescommand_04;
    }

    //05命令   发送读取信息寄存器信息
    public static String Command_05_Send(String mid){
        String resp = "";
        int Mid = Integer.parseInt(mid);

        byte[] v = new byte[4];
        //设置高位在前
        Mid = Integer.reverseBytes(Mid);
        v = MuliScheduleCheck.intToByteArray(Mid);
        String midstr = MuliScheduleCheck.bytesToHexString(v).toUpperCase();
        resp = "3A"+midstr+"05"+"0000"+"0D0A";
        return resp;
    }

    //05命令   项圈响应解析
    public static String[] Command_05_Receive(String hexstr){
        if(hexstr.length()!=80){
            return null;
        }
        String[] rescommand_05 = new String[5];
        if(hexstr.substring(0,2).equals("3A") && hexstr.substring(76,80).equals("0D0A")) {
            byte[] res = MuliScheduleCheck.hexStringToBytes(hexstr);
            //04命令
            //起始码，1个字节    (第1个字节)
            String res1 = "0x" + hexstr.substring(0, 2);
            System.out.println("开始标志： " + res1);
            //设备地址码，4字节，mid   (第2,3,4,5个字节)
            byte[] v = new byte[4];
            for(int i=4,k=0;i>0;i--,k++){
                v[k]=res[i];
            }
            int res2 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("设备地址码： "+res2);
            rescommand_05[0] = res2+"";
            //命令码，1个字节   (第6个字节)
            String res3 = "0x"+hexstr.substring(10,12);
            System.out.println("命令码： "+res3);
            rescommand_05[1] = res3;
            //ip
            //数据长度, 2个字节  小端模式  (第7,8个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=7,k=2;i>5;i--,k++){
                v[k]=res[i];
            }
            int res4 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("数据长度： "+res4);
            //region 要读的数据  18个字节
            //mid  4个字节    (第9,10,11,12个字节)
            for(int i=11,k=0;i>7;i--,k++){
                v[k]=res[i];
            }
            int res51 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("mid： "+res51);
            rescommand_05[2] = ""+res51;
            //swver 4个字节   (第13,14,15,16)
            v[0] = res[15];
            v[1] = res[14];
            v[2] = res[13];
            v[3] = res[12];
            int res52 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("版本信息： "+res52);
            rescommand_05[3] = ""+res52;
            //SIM_CCID
            v[0] = 0;
            v[1] = 0;
            v[2] = 0;
            v[3] = res[16];
            char res53_1 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[17];
            char res53_2 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[18];
            char res53_3 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[19];
            char res53_4 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[20];
            char res53_5 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[21];
            char res53_6 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[22];
            char res53_7 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[23];
            char res53_8 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[24];
            char res53_9 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[25];
            char res53_10 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[26];
            char res53_11 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[27];
            char res53_12 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[28];
            char res53_13 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[29];
            char res53_14 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[30];
            char res53_15 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[31];
            char res53_16 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[32];
            char res53_17 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[33];
            char res53_18 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[34];
            char res53_19 = (char) MuliScheduleCheck.byteArrayToInt(v);
            v[3] = res[35];
            char res53_20 = (char) MuliScheduleCheck.byteArrayToInt(v);
            String res53 = ""+res53_1+res53_2+res53_3+res53_4+res53_5+res53_6+res53_7+res53_8+res53_9+res53_10+res53_11+res53_12+res53_13+res53_14+res53_15+res53_16+res53_17+res53_18+res53_19+res53_20;
            System.out.println("SIM_CCID: "+res53);
            rescommand_05[4] = res53;
            //CRC16 2个字节  小端模式         (第37，38个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=37,k=2;i>35;i--,k++){
                v[k]=res[i];
            }
            int res6 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("CRC16： "+res6);
        }
        return rescommand_05;
    }

    //06命令   发送读取时间寄存器信息
    public static String Command_06_Send(String mid){

        String resp = "";
        int Mid = Integer.parseInt(mid);

        byte[] v = new byte[4];
        //设置高位在前
        Mid = Integer.reverseBytes(Mid);
        v = MuliScheduleCheck.intToByteArray(Mid);
        String midstr = MuliScheduleCheck.bytesToHexString(v).toUpperCase();
        resp = "3A"+midstr+"06"+"0000"+"0D0A";
        return resp;
    }

    //06命令   项圈响应解析
    public static String[] Command_06_Receive(String hexstr){
        if(hexstr.length()!=120){
            return null;
        }
        String[] rescommand_06 = new String[14];
        if(hexstr.substring(0,2).equals("3A") && hexstr.substring(116,120).equals("0D0A")) {
            byte[] res = MuliScheduleCheck.hexStringToBytes(hexstr);
            //06命令
            //起始码，1个字节    (第1个字节)
            String res1 = "0x" + hexstr.substring(0, 2);
            System.out.println("开始标志： " + res1);
            //设备地址码，4字节，mid   (第2,3,4,5个字节)
            byte[] v = new byte[4];
            for(int i=4,k=0;i>0;i--,k++){
                v[k]=res[i];
            }
            int res2 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("设备地址码： "+res2);
            rescommand_06[0] = res2+"";
            //命令码，1个字节   (第6个字节)
            String res3 = "0x"+hexstr.substring(10,12);
            System.out.println("命令码： "+res3);
            rescommand_06[1] = res3;
            //ip
            //数据长度, 2个字节  小端模式  (第7,8个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=7,k=2;i>5;i--,k++){
                v[k]=res[i];
            }
            int res4 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("数据长度： "+res4);
            //region 要读的数据  18个字节
            //投药周期1  4个字节    (第9,10,11,12个字节)
            for(int i=11,k=0;i>7;i--,k++){
                v[k]=res[i];
            }
            int res51_1 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间1： "+res51_1);
            rescommand_06[2] = res51_1+"";
            //投药周期2  4个字节    (第13,14,15,16个字节)
            for(int i=15,k=0;i>11;i--,k++){
                v[k]=res[i];
            }
            int res51_2 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间2： "+res51_2);
            rescommand_06[3] = res51_2+"";
            //投药周期3  4个字节
            for(int i=19,k=0;i>15;i--,k++){
                v[k]=res[i];
            }
            int res51_3 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间3： "+res51_3);
            rescommand_06[4] = res51_3+"";
            //投药周期4  4个字节
            for(int i=23,k=0;i>19;i--,k++){
                v[k]=res[i];
            }
            int res51_4 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间4： "+res51_4);
            rescommand_06[5] = res51_4+"";
            //投药周期5  4个字节
            for(int i=27,k=0;i>23;i--,k++){
                v[k]=res[i];
            }
            int res51_5 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间5： "+res51_5);
            rescommand_06[6] = res51_5+"";
            //投药周期6  4个字节
            for(int i=31,k=0;i>27;i--,k++){
                v[k]=res[i];
            }
            int res51_6 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间6： "+res51_6);
            rescommand_06[7] = res51_6+"";
            //投药周期7  4个字节
            for(int i=35,k=0;i>31;i--,k++){
                v[k]=res[i];
            }
            int res51_7 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间7： "+res51_7);
            rescommand_06[8] = res51_7+"";
            //投药周期8  4个字节
            for(int i=39,k=0;i>35;i--,k++){
                v[k]=res[i];
            }
            int res51_8 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间8： "+res51_8);
            rescommand_06[9] = res51_8+"";
            //投药周期9  4个字节
            for(int i=43,k=0;i>39;i--,k++){
                v[k]=res[i];
            }
            int res51_9 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间9： "+res51_9);
            rescommand_06[10] = res51_9+"";
            //投药周期10  4个字节
            for(int i=47,k=0;i>43;i--,k++){
                v[k]=res[i];
            }
            int res51_10 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间10： "+res51_10);
            rescommand_06[11] = res51_10+"";
            //投药周期11  4个字节    (
            for(int i=51,k=0;i>47;i--,k++){
                v[k]=res[i];
            }
            int res51_11 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间11： "+res51_11);
            rescommand_06[12] = res51_11+"";
            //投药周期12  4个字节
            for(int i=55,k=0;i>51;i--,k++){
                v[k]=res[i];
            }
            int res51_12 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("投药时间12： "+res51_12);
            rescommand_06[13] = res51_12+"";

            //endregion

            //CRC16 2个字节  小端模式         (第57，58个字节)
            v[0] = 0;
            v[1] = 0;
            for(int i=57,k=2;i>56;i--,k++){
                v[k]=res[i];
            }
            int res6 = MuliScheduleCheck.byteArrayToInt(v);
            System.out.println("CRC16： "+res6);
        }
        return rescommand_06;
    }
}
