package com.sec.device;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class MuliServer implements Runnable{
	
	private int port = 8888;
	private int TimeOut = 2000;
	static Map<Object, Integer> heatTimeflag = new HashMap<Object, Integer>(); 
	static Map<Object, Long> heatTimeMap = new HashMap<Object, Long>();  
	static Map<Object, String> heatTimeMapData = new HashMap<Object, String>(); 
	
	static Charset cs = Charset.forName("utf-8");
	// 定义准备执行读取数据的ByteBuffer
	private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer sBuffer = ByteBuffer.allocate(1024);
	private Selector selector;
	private Map<String,SocketChannel> clientsMap = new HashMap<String,SocketChannel>();
	private static SqlSessionFactory ssf = null;

	static {
		String resource = "configuration.xml";
		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ssf = new SqlSessionFactoryBuilder().build(reader);
	}


	public MuliServer(int port) {
		// TODO Auto-generated constructor stub
		this.port = port;

		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void init() throws IOException{
		// 创建通道,并设置非阻塞
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		// 通道绑定端口号,可以启动多个serverSocket，监听多端口
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		// 创建选择器，并为通道绑定感兴趣的事件
		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("server start on port: "+port);
	}
	
	@Override
	public void run() { 
		// 开始轮询通道事件
        while(true){  
            try{
                selector.select(TimeOut); 
    			Iterator ite = selector.selectedKeys().iterator();
                while(ite.hasNext()){  
                    SelectionKey key = (SelectionKey) ite.next();                    
                    ite.remove();//确保不重复处理 
                    handle(key);  
                }  
                ScheduleCheck.Check();
            }  
            catch(Throwable t){  
                t.printStackTrace();  
            }                            
        }    
    }
	
	private void handle(SelectionKey selectionKey) throws IOException, ClosedChannelException {
		ServerSocketChannel server = null;
		SocketChannel client = null;
		String receiveText = null;
		int count = 0;
		// 如果sk对应的通道包含客户端的连接请求
		if(selectionKey.isValid() && selectionKey.isAcceptable()) {
			server = (ServerSocketChannel) selectionKey.channel();
			// 调用accept方法接受连接，产生服务器端对应的SocketChannel
			client = server.accept();
			if(client!=null) {
				// 设置采用非阻塞模式
				client.configureBlocking(false);
				// 将该SocketChannel也注册到selector
				client.register(selector, SelectionKey.OP_READ);
			}
		}
		// 如果sk对应的通道有数据需要读取
		if(selectionKey.isValid() && selectionKey.isReadable()) {
			try {
				// 获取该SelectionKey对应的Channel，该Channel中有可读的数据
				client = (SocketChannel) selectionKey.channel();
//				System.out.println(rBuffer.toString());
				rBuffer.clear();
//				System.out.println(rBuffer.toString());
				// 开始读取数据
				count = client.read(rBuffer);
//				System.out.println(count);
				// count>0 count=0 count<0
				//count>0代表连接未断开
				if(count>0) {
					rBuffer.flip();
					receiveText = String.valueOf(cs.decode(rBuffer).array());
					System.out.println(client.toString()+":"+receiveText);
					
					String id = client.toString().split("/")[2];  
					id = id.substring(0,id.length() - 1);
					long time = System.currentTimeMillis();
                    heatTimeMap.put(client, time);
                    if(receiveText.length() > 0) {
						String answer = getAnswer(receiveText);  
						//根据answer判断需要客户端再次握手，发数据
						if(1==1) {
							heatTimeMapData.put(client, answer);
							heatTimeflag.put(client, 1);
						//不需要客户端再次握手，发数据
						}else {
							heatTimeMap.remove(client);
							heatTimeMapData.remove(client);
							heatTimeflag.remove(client);
						}
						
						//服务端给出回复
						client.write(cs.encode(time+" "+answer));
					}
				}else {
					//这里关闭channel，因为客户端已经关闭channel或者异常了  
					System.out.println(client.toString()+"的客户端关闭了！");
					client.close();  
				}
			}catch (Exception e) {
				e.printStackTrace();
				selectionKey.cancel();         //其他教程
				
				System.out.println("一个客户端关闭了！");
				if(client != null){  
					client.close();  
	            }
			}
		}
	}
	
	private String getAnswer(String question){  
        String answer = null;  
           
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

		//3A1A27000001F7970D0A
		//3A1A27000010140000752500AEC4E75B1F004E067500121200001500460E0D0A
		//3A1A2700000412007A70FC2D5FEA05001E00000000000000000061A50D0A
		//3A1A270000051C001A270000B4591401383938363034313131303138373131333932323593A10D0A
		//3A1A270000063000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D2F40D0A
		switch(question.substring(10,12)){
			case "01":
				answer = "01 command";
				break;
			case "10":
				String[] command10 = Analyse.Command_10(question);
				String mid = command10[0];
				String command = command10[1];
				String type = command10[2];
				String temperature = command10[3];
				String voltage = command10[4];
				String Error = command10[5];
				String grantgmt = command10[6];
				String latitude = command10[7];
				String longitude = command10[8];
				String status = command10[9];
				String gsm_signal_level = command10[10];
//				boolean tempflag = ;
//				String command10_resp = Analyse.Command_10_Response(mid,tempflag);
				String command10_resp = Analyse.Command_10_Response("10010",true);
				answer = command10_resp;
				break;
			case "02":
				String[] command02_receive = Analyse.Command_02_Receive(question);

				String command02_send = Analyse.Command_02_Send();
				answer = command02_send;
				break;
			case "03":
				String[] command03_receive = Analyse.Command_03_Receive(question);

				String command03_send = Analyse.Command_03_Send();
				answer = command03_send;
				break;
			case "04":
				String[] command04 = Analyse.Command_04_Receive(question);

				String command04_send = Analyse.Command_04_Send();
				answer = command04_send;
				break;
			case "05":
				String[] command05 = Analyse.Command_05_Receive(question);

				String command05_send = Analyse.Command_05_Send();
				answer = command05_send;
				break;
			case "06":
				String[] command06 = Analyse.Command_06_Receive(question);

				String command06_send = Analyse.Command_06_Send();
				answer = command06_send;
				break;
			case "needconn":
				mid = "new_collar0001";
				SqlSession session = ssf.openSession();
				try{
					SysDeviceconf sysDeviceconf = session.selectOne("selectSysDeviceconf", mid);
					System.out.println(sysDeviceconf.toString());
					answer = "ok";
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					session.close();
				}
				break;
			default:
                answer = "input who, or what, or where";
        }  
        return answer;  
    }
	
	
	public static void Start() throws InterruptedException{
		MuliServer server1 = new MuliServer(59999);
		new Thread(server1).start();
	}
}
