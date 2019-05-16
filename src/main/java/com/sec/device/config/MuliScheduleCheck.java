package com.sec.device.config;

import com.sec.device.config.MuliServer;

import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;

public class MuliScheduleCheck {

	public static void Check(){
		try{
        	//所有客户端连接数据,判断客户端答复时间，10秒没响应，重发一次，再10秒无反应,即为超时，关闭client
        	//heatTimeMapData.LinkMap存储所有客户端连接数据
            if(!MuliServer.heatTimeMapData.isEmpty())
            {
                Iterator iter = MuliServer.heatTimeMapData.entrySet().iterator();
                
                //遍历所有客户端连接
                while (iter.hasNext()) 
                {
					Map.Entry entry = (Map.Entry) iter.next();
                    SocketChannel key = (SocketChannel)entry.getKey();
                    long time = System.currentTimeMillis();
                    //等待客户端答复，2秒没响应，重发一次，再2秒无反应,即为超时，关闭client
                    if(MuliServer.heatTimeMap.get(key) !=null
                        && time - MuliServer.heatTimeMap.get(key) > 5000  && time - MuliServer.heatTimeMap.get(key) < 10000)
                    {
                    	if(MuliServer.heatTimeflag.get(key).split(",")[0].equals("1")) {
                    		//服务端重发数据
                            String[] values = MuliServer.heatTimeMapData.get(key).split(",");
                            String mid = MuliServer.heatTimeflag.get(key).split(",")[1];
                            String commandkey = "";
                            for(int i=0;i<values.length;i++){
                                commandkey = values[i].split("_")[0];
                                if(MuliServer.Commandmap.get(mid).containsKey(commandkey) && MuliServer.CommandStatusmap.get(mid).get(commandkey) == 1){
//                                    key.write(MuliServer.cs.encode(MuliServer.heatTimeMapData.get(key)));
                                    key.write(MuliServer.cs.encode(values[i].split("_")[1]));
                                    Thread.sleep(150);
                                }
                            }
                        	MuliServer.heatTimeflag.put(key, 2 +","+MuliServer.heatTimeflag.get(key).split(",")[1]);
                    	}
                    }else if(MuliServer.heatTimeMap.get(key) !=null
                            && time - MuliServer.heatTimeMap.get(key) > 10000) {
                    	//服务端重发数据，发第3次，或者不发
//                        key.write(MuliServer.cs.encode(MuliServer.heatTimeMapData.get(key)));
//                        System.out.println(time+" closed");

//                        String mid = MuliServer.heatTimeflag.get(key).split(",")[1];
//                        if(MuliServer.Commandmap.get(mid).size()==0){
//                            ShutDownClient(key);
//                            iter.remove();
//                        }
                        ShutDownClient(key);
                        iter.remove();
                    }
                }
            }
            if(!MuliServer.heatTimeMap.isEmpty()){
                Iterator iter1 = MuliServer.heatTimeMap.entrySet().iterator();

                //遍历所有客户端连接
                while (iter1.hasNext()){
                    Map.Entry entry = (Map.Entry) iter1.next();
                    SocketChannel key1 = (SocketChannel)entry.getKey();
                    long time1 = System.currentTimeMillis();

                    if(MuliServer.heatTimeMap.get(key1) !=null
                            && time1 - MuliServer.heatTimeMap.get(key1) > 10000){
                        System.out.println(time1+" closed");
                        ShutDownClient(key1);
                    }
                }
            }
        }  
        catch(Throwable t){  
            t.printStackTrace();  
        }   
	}
	
	public static void ShutDownClient(SocketChannel client)
    {
		if(client.isOpen())
        {
            try 
            {
                //将连接的输入输出都关闭，而不是直接Close连接
            	client.shutdownInput();
            	client.shutdownOutput();


            	MuliServer.heatTimeflag.remove(client);
            	MuliServer.heatTimeMap.remove(client);

                MuliServer.heatTimeMapData.remove(client);
            	
            	if(client != null){  
            		System.out.println(client+" 的客户端回复时间超时，连接关闭！");
					client.close(); 
	            }
            }
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }



    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /* Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
            * @param src byte[] data
 * @return hex string
 */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

}
