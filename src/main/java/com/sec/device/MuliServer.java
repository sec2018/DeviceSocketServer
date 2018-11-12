package com.sec.device;

import com.sec.device.redis.RedisService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.Transaction;

import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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

//	@Autowired
//	protected RedisService redisService;
	//获取的ben对象转为你需要的对象
	RedisService redisService = (RedisService) ApplicationContextProvider.getBean("redisService");

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
						if(answer=="ok"){
							//无更新，不回复, 或者是来自客户端的回复，去除redis的key，更新数据库，然后不回复
							//不需要客户端再次握手，发数据
							heatTimeMap.remove(client);
							heatTimeMapData.remove(client);
							heatTimeflag.remove(client);
							if(client != null){
								client.close();
							}
						}else{
							if(answer.indexOf(",")>-1){
								String[] answerlist =  answer.split(",");
								heatTimeMapData.put(client, answerlist[0]);
								heatTimeflag.put(client, 1);
								client.write(cs.encode(answerlist[0]));

								heatTimeMapData.put(client, answerlist[1]);
								heatTimeflag.put(client, 1);
								client.write(cs.encode(answerlist[1]));
							}else{
								heatTimeMapData.put(client, answer);
								heatTimeflag.put(client, 1);
								client.write(cs.encode(answer));
							}
						}
//						if(1==1) {
//							heatTimeMapData.put(client, answer);
//							heatTimeflag.put(client, 1);
//						//不需要客户端再次握手，发数据
//						}else {
//							heatTimeMap.remove(client);
//							heatTimeMapData.remove(client);
//							heatTimeflag.remove(client);
//						}
//						//服务端给出回复
////						client.write(cs.encode(time+" "+answer));
//						client.write(cs.encode(answer));
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
		String mid = "";
		SqlSession session = null;

		//3A1A27000001F7970D0A
		//3A1A27000010140000752500AEC4E75B1F004E067500121200001500460E0D0A
		//3A1A2700000412007A70FC2D5FEA05001E00000000000000000061A50D0A
		//3A1A270000051C001A270000B4591401383938363034313131303138373131333932323593A10D0A
		//3A1A270000063000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D2F40D0A
		switch(question.substring(10,12)){
			case "01":
				answer = "ok";
				mid = Analyse.Command_01(question)[0];
				String deviceconfig = ConnectRedisCheckToken("device_"+mid);
				String timeconfig = ConnectRedisCheckToken("time_"+mid);
				if(deviceconfig!=null && deviceconfig!=""){
					answer = deviceconfig;
				}
				if(timeconfig!=null && timeconfig!=""){
					answer = answer+","+timeconfig;
				}
				if(answer.indexOf(",")==0){
					answer = answer.substring(1,answer.length()-1);
				}
				break;
			case "10":
				String[] command10 = Analyse.Command_10(question);
				mid = command10[0];
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

				SysLaytime sysLaytime = new SysLaytime();
				sysLaytime.setId(0);
				sysLaytime.setMid(mid);
				sysLaytime.setLatitude(latitude);
				sysLaytime.setLongitude(longitude);
				sysLaytime.setGrantgmt(new Date(Long.valueOf(grantgmt+"000")));
				sysLaytime.setErr(Error);
				sysLaytime.setVoltage(Double.parseDouble(voltage));
				sysLaytime.setTemperature(Byte.parseByte(temperature));
				sysLaytime.setType(Byte.parseByte(type));
				sysLaytime.setTimegmt(new Date());
				sysLaytime.setIslay(Byte.parseByte(status));
				sysLaytime.setSignallevel(Byte.parseByte(gsm_signal_level));
				sysLaytime.setUpdatetime(new Date());

				session = ssf.openSession();
				try{
					boolean res02 = session.insert("insertSysLaytime", sysLaytime) ==1?true:false;
					session.commit();
					if(res02){
						String command10_resp = Analyse.Command_10_Response(mid,true);
						answer = command10_resp;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					session.close();
				}
				break;
			case "02":
				String[] command02_receive = Analyse.Command_02_Receive(question);
				mid = command02_receive[0];
				//说明项圈已读取时间配置，删除
				redisService.remove("time_"+mid);
				//更新数据库
				session = ssf.openSession();
				HashMap <String,Object> map_02 = new HashMap<String,Object>();
				map_02.put("mid",mid);
				map_02.put("updatetime",new Date());
				try{
					boolean res02 = session.update("updateLayconfigByMid", map_02) ==1?true:false;
					session.commit();
					if(res02){
						answer = "ok";
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					session.close();
				}
				answer = "ok";
				break;
			case "03":
				String[] command03_receive = Analyse.Command_03_Receive(question);
				mid = command03_receive[0];
				//说明项圈已读取基础配置，删除
				redisService.remove("device_"+mid);
				//更新数据库
				session = ssf.openSession();
				HashMap <String,Object> map_03 = new HashMap<String,Object>();
				map_03.put("mid",mid);
				map_03.put("updatetime",new Date());
				try{
					boolean res03 = session.update("updateDeviceconfByMid", map_03) ==1?true:false;
//					SysDeviceconf res03 = session.selectOne("selectSysDeviceconf", mid);
					session.commit();
					if(res03){
						answer = "ok";
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					session.close();
				}
				answer = "ok";
				break;
			case "04":
				String[] command04 = Analyse.Command_04_Receive(question);
				mid = command04[0];
				String command_04 = command04[1];
				String ip = command04[2];
				String port = command04[3];
				String infoupdatecycle = command04[4];
				String tickcycle = command04[5];
				String ledenable = command04[6];
				String tempflag = command04[7];
				String tempgmt = command04[8];

				answer = "ok";
				break;
			case "05":
				String[] command05 = Analyse.Command_05_Receive(question);
				mid = command05[0];
				String command_05 = command05[1];
				String swver = command05[3];
				String simccid = command05[4];

				answer = "ok";
				break;
			case "06":
				String[] command06 = Analyse.Command_06_Receive(question);
				mid = command06[0];
				String command_06 = command06[1];
				String time01 = command06[2];
				String time02 = command06[3];
				String time03 = command06[4];
				String time04 = command06[5];
				String time05 = command06[6];
				String time06 = command06[7];
				String time07 = command06[8];
				String time08 = command06[9];
				String time09 = command06[10];
				String time10 = command06[11];
				String time11 = command06[12];
				String time12 = command06[13];

				answer = "ok";
				break;
			default:
                answer = "ok";
        }  
        return answer;  
    }
	
	
	public static void Start() throws InterruptedException{
		MuliServer server1 = new MuliServer(59999);
		new Thread(server1).start();
	}

	public String ConnectRedisCheckToken(String key){
		String value = "";
		int retry = 1;
		while (retry<=3){
			try
			{
				//业务代码
				value = redisService.get(key);
				break;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				//重试
				retry++;
				if(retry == 4){
					return value;
				}
			}
		}
		return value;
	}
}
