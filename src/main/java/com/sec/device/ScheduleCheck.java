package com.sec.device;

import java.nio.channels.SocketChannel;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

public class ScheduleCheck {

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
                   //等待客户端答复，10秒没响应，重发一次，再10秒无反应,即为超时，关闭client
                    if(MuliServer.heatTimeMap.get(key) != null   
                        && time - MuliServer.heatTimeMap.get(key) > 5000  && time - MuliServer.heatTimeMap.get(key) < 10000)  
                    {  
                    	if(MuliServer.heatTimeflag.get(key) == 1) {
                    		//服务端重发数据
                        	key.write(MuliServer.cs.encode(time+" "+MuliServer.heatTimeMapData.get(key)));
                        	MuliServer.heatTimeflag.put(key, 2);
                    	}
                    }else if(MuliServer.heatTimeMap.get(key) != null   
                            && time - MuliServer.heatTimeMap.get(key) > 10000) {
                    	//服务端重发数据
                    	key.write(MuliServer.cs.encode(time+" closed"));
                    	ShutDownClient(key);
                    	iter.remove();
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
}
