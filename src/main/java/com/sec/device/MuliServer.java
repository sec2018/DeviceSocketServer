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
        
        switch(question){  
        case "who":  
            answer = "i am wang\n";  
            break;  
        case "what":  
            answer = "i am your friend\n";  
            break;  
        case "where":  
            answer = "i come from shanghai\n";  
            break;  
        case "hi":  
            answer = "hello\n";  
            break;  
        case "bye":  
            answer = "88\n";  
        case "needconn":
        	String mid = "new_collar0001";
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
                answer = "input who， or what， or where";  
        }  
        return answer;  
    }
	
	
	public static void Start() throws InterruptedException{
		MuliServer server1 = new MuliServer(59999);
		new Thread(server1).start();
	}
}
