package com.sec.device.config;

import com.sec.device.pojo.SysDeviceconf;
import com.sec.device.pojo.SysLayconfig;
import com.sec.device.pojo.SysLaytime;
import com.sec.device.pojo.Systimepos;
import com.sec.device.util.common.Analyse;
import com.sec.device.util.common.ApplicationContextProvider;
import com.sec.device.util.redis.RedisService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Server implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public static final Charset charset = Charset.forName("UTF-8");
    private Selector selector = null;
    //检测现有连接的通道列表，数据
    public static Map<Object, String> heatTimeMapData = new HashMap<Object, String>();
    //检测现有连接的通道，等待时间
    public static Map<Object, Long> heatTimeMap = new HashMap<Object, Long>();
    //检测现有连接的通道，第几次发送
    public static Map<Object, String> heatTimeflag = new HashMap<Object, String>();
    public static Map<String,Map<String,String>>  Commandmap = new HashMap<String,Map<String,String>>();
    //1：内存中（未接到答复，等待中），2：响应中   相应完成删除
    public static Map<String,Map<String,Integer>>  CommandStatusmap = new HashMap<String,Map<String,Integer>>();

    private ServerSocketChannel ssc = null;
    private Thread thread = new Thread(this);
    private Queue<String> queue = new ConcurrentLinkedDeque<String>();
    private volatile boolean live = true;

    private static SqlSessionFactory ssf = null;
    private int TimeOut = 1500;

    //获取的对象转为你需要的对象
    private static RedisService redisService = (RedisService) ApplicationContextProvider.getBean("redisService");

    static {
        String resource = "configuration.xml";
        Reader reader = null;
        try {
            reader = Resources.getResourceAsReader(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ssf = new SqlSessionFactoryBuilder().build(reader);
        InitConfig();
    }

    public void start(int port) throws IOException{
        selector = Selector.open();
        ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(port));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        thread.start();
    }


    @Override
    public void run() {
        try {
            int count = 0;
            while (live && !Thread.interrupted()){
                if(selector.select(TimeOut)==0){
                    continue;
                }
                //放在这里所有在线用户均可以收到
//                ByteBuffer outBuf = null;
//                String outMsg = queue.poll();
//                if(!StringUtils.isEmpty(outMsg)){
//                    switch (outMsg){
//                        case "01":
//                            outMsg = "this is 01 cmd";
//                            break;
//                        case "02":
//                            outMsg = "this is 02 cmd";
//                            break;
//                    }
//                    outBuf = ByteBuffer.wrap(outMsg.getBytes("UTF-8"));
//                    outBuf.limit(outMsg.length());
//                }

                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> it = set.iterator();
                while (it.hasNext()){
                    SelectionKey key = it.next();
                    it.remove();
                    if(key.isValid() && key.isAcceptable()){
                        this.onAcceptable(key);
                    }
                    if(key.isValid() && key.isReadable()){
                        this.onReadable(key);
                    }
                    if(key.isValid() && key.isWritable()){
                        //放在这里只有该用户可以收到
                        String answer = queue.poll();
                        if(answer != null && answer.indexOf("command10_")>=0){
                            //10命令
                            String answer01 = answer.split("_")[2];
                            SocketChannel sc = (SocketChannel)key.channel();
                            sc.write(charset.encode(answer01));
                            Thread.sleep(150);
                            String mid = answer.split("_")[1];
                            Map<String,String> templist = Commandmap.get(mid);
                            String values = "";
                            for(int i=0;i<templist.size();i++){
                                String k = templist.keySet().toArray()[i].toString();
                                String value = templist.values().toArray()[i].toString();
                                values = values+k+"_"+value+",";
                                sc.write(charset.encode(value));
                                long time = System.currentTimeMillis();
                                heatTimeMap.put(sc, time);
                                Thread.sleep(150);
                            }
                            if(values!=""){
                                heatTimeMapData.put(sc, values.substring(0,values.length()-1));
                                heatTimeflag.put(sc, 1+","+mid);
                            }
                        }else{

                        }
                    }
                }
                ScheduleCheck.Check();
            }
        } catch (Exception e) {
            log.error("Error on socket I/O",e);
        }
    }

    private void onAcceptable(SelectionKey key) throws IOException{
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = null;
        try{
            sc = ssc.accept();
            if(sc != null){
                log.info("client {} connected", sc.getRemoteAddress());
                sc.configureBlocking(false);
                sc.register(selector,SelectionKey.OP_READ | SelectionKey.OP_WRITE, ByteBuffer.allocate(1024));
            }
        }catch (Exception e){
            log.error("error on accept connection", e);
            sc.close();
        }
    }

    private void onReadable(SelectionKey key) throws IOException{
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();
        int r = 0;
        StringBuilder sb = new StringBuilder();
        String rs = null;
        String remote = null;
        buf.clear();
        try {
            remote = sc.getRemoteAddress().toString();
            while ((r = sc.read(buf)) > 0) {
                log.debug("Received {} bytes from {}", r, remote);
                buf.flip();
                sb.append(charset.decode(buf));
                buf.clear();
                rs = sb.toString();
                System.out.println(sc.toString() + ":" + rs);
                if (rs.length() > 0) {
                    if (rs.endsWith("\n")) {
                        break;
                    }
                    String answer = getAnswer(rs);
                    queue.add(answer);
                    //断开客户端
                    if("close".equalsIgnoreCase(answer)){
                        heatTimeMapData.remove(sc);
                        sc.close();
                        break;
                    }
                }
            }
            if (sc == null && sc.read(buf) == -1) {
                System.out.println("客户端主动断开连接！");
                sc.close();
                return;
            }
        }catch (Exception e){
            log.error("Error on read socket", e);
            sc.close();
            return;
        }
    }

    public static void write(SocketChannel sc, ByteBuffer buf) throws IOException{
        buf.position(0);
        int r = 0;
        try{
            while(buf.hasRemaining() && ((r=sc.write(buf))>0)){
                log.debug("write back {} bytes to {}", r, sc.getRemoteAddress());
            }
        }catch (Exception e){
            log.error("Error on write socket",e);
            sc.close();
            return;
        }
    }

    public void close(){
        live = false;
        try{
            thread.join();
            selector.close();
            ssc.close();
        }catch (InterruptedException e){
            log.error("Be interrupted on join",e);
        }catch (IOException e) {
            log.error("IOException on close", e);
        }
    }


    public String ConnectRedisCheckToken(String key){
        String value = "";
        int retry = 1;
        while (retry<=5){
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

    //初始化获取更新的记录
    public static void InitConfig(){
        SqlSession session = ssf.openSession();
        try{
            //查询时间设置表
            List<SysLayconfig>  listlayconfig = session.selectList("getAllLayConfigModifiedRecord");
            if(listlayconfig.size()>0){
                for (SysLayconfig syslayconfig:listlayconfig) {
                    redisService.setpersist("time_"+syslayconfig.getMid(), Analyse.Command_02_Send(syslayconfig));
                }
            }
            //查询基础设置表
            List<SysDeviceconf>  listdeviceconf = session.selectList("getAllDeviceConfModifiedRecord");
            if(listdeviceconf.size()>0){
                for (SysDeviceconf sysDeviceconf:listdeviceconf) {
                    if(sysDeviceconf.getIp()!=null){
                        redisService.remove("device_"+sysDeviceconf.getMid());
                        redisService.setpersist("device_"+sysDeviceconf.getMid(),Analyse.Command_03_Send(sysDeviceconf));
                    }
					//查询命令04
                    redisService.remove("04_"+sysDeviceconf.getMid());
					redisService.setpersist("04_"+sysDeviceconf.getMid(),Analyse.Command_04_Send(sysDeviceconf.getMid()));
                    //查询命令05
                    redisService.remove("05_"+sysDeviceconf.getMid());
					redisService.setpersist("05_"+sysDeviceconf.getMid(),Analyse.Command_05_Send(sysDeviceconf.getMid()));
					//查询命令06
                    redisService.remove("06_"+sysDeviceconf.getMid());
					redisService.setpersist("06_"+sysDeviceconf.getMid(),Analyse.Command_06_Send(sysDeviceconf.getMid()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private String getAnswer(String questiontemp){
        String answer = null;
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String mid = "";
        SqlSession session = null;
        Map<String,String> listtemp = null;
        Map<String,Integer> listststus = null;

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        //3A1A27000001F7970D0A
        //3A1A27000010140000752500AEC4E75B1F004E067500121200001500460E0D0A
        //3A1A2700000412007A70FC2D5FEA05001E00000000000000000061A50D0A
        //3A1A270000051C001A270000B4591401383938363034313131303138373131333932323593A10D0A
        //3A1A270000063000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000D2F40D0A
        if(questiontemp.length()<12 && questiontemp.indexOf("3A1A") == -1 && questiontemp.indexOf("0D0A") == -1){
            System.out.println("your sendding command is wrong...");
            System.out.println("waiting for next right command...");
            answer = "";
        }else{
            String[] questions = questiontemp.split("0D0A");
            for (String question:questions) {
                question = question+"0D0A";
                switch(question.substring(10,12)){
                    case "01":
//						answer = "close";
//						mid = Analyse.Command_01(question)[0];
//						String deviceconfig = ConnectRedisCheckToken("device_"+mid);
//						String timeconfig = ConnectRedisCheckToken("time_"+mid);
//						String get04config = ConnectRedisCheckToken("04_"+mid);
//						String get05config = ConnectRedisCheckToken("05_"+mid);
//						String get06config = ConnectRedisCheckToken("06_"+mid);
//						listtemp = new HashMap<String,String>();
//						listststus = new HashMap<String,Integer>();
//						if(deviceconfig!=null && deviceconfig!=""){
//							listtemp.put("com03",deviceconfig);
//							listststus.put("com03",1);
//						}
//						if(timeconfig!=null && timeconfig!=""){
//							listtemp.put("com02",timeconfig);
//							listststus.put("com02",1);
//						}
//						if(get04config!=null && get04config!=""){
//							listtemp.put("com04",get04config);
//							listststus.put("com04",1);
//						}
//						if(get05config!=null && get05config!=""){
//							listtemp.put("com05",get05config);
//							listststus.put("com05",1);
//						}
//						if(get06config!=null && get06config!=""){
//							listtemp.put("com06",get06config);
//							listststus.put("com06",1);
//						}
//						Commandmap.put(mid,listtemp);
//						CommandStatusmap.put(mid,listststus);
//						if(listtemp.size()>0){
//							answer = mid;
//						}
                        break;
                    case "10":
                        answer = "close";
                        String[] command10 = Analyse.Command_10(question);
                        mid = command10[0];

                        String deviceconfig = ConnectRedisCheckToken("device_"+mid);
                        String timeconfig = ConnectRedisCheckToken("time_"+mid);
                        String get04config = ConnectRedisCheckToken("04_"+mid);
                        String get05config = ConnectRedisCheckToken("05_"+mid);
                        String get06config = ConnectRedisCheckToken("06_"+mid);
                        listtemp = new HashMap<String,String>();
                        listststus = new HashMap<String,Integer>();
                        if(deviceconfig!=null && deviceconfig!=""){
                            listtemp.put("com03",deviceconfig);
                            listststus.put("com03",1);
                        }
                        if(timeconfig!=null && timeconfig!=""){
                            listtemp.put("com02",timeconfig);
                            listststus.put("com02",1);
                        }
                        if(get04config!=null && get04config!=""){
                            listtemp.put("com04",get04config);
                            listststus.put("com04",1);
                        }
                        if(get05config!=null && get05config!=""){
                            listtemp.put("com05",get05config);
                            listststus.put("com05",1);
                        }
                        if(get06config!=null && get06config!=""){
                            listtemp.put("com06",get06config);
                            listststus.put("com06",1);
                        }
                        Commandmap.put(mid,listtemp);
                        CommandStatusmap.put(mid,listststus);

                        String command = command10[1];
                        String type = command10[2];
                        String temperature = command10[3];
                        String voltage = command10[4];
                        String Error = command10[5];
                        String grantgmt = command10[6];
                        String latitude = command10[7];
                        String longitude = command10[8];
                        String cyclenum = command10[9];
                        String status = command10[10];
                        String gsm_signal_level = command10[11];
                        String pillcode = command10[12];

                        SysLaytime sysLaytime = new SysLaytime();
                        sysLaytime.setId(0);
                        sysLaytime.setMid(mid);
                        sysLaytime.setLatitude(latitude);
                        sysLaytime.setLongitude(longitude);

                        if(!grantgmt.equals("0")){
                            sysLaytime.setGrantgmt(new Date(Long.valueOf(grantgmt+"000")));
                        }else {
                            sysLaytime.setGrantgmt(new Date());
                        }
                        sysLaytime.setErr(Error);
                        sysLaytime.setVoltage(Double.parseDouble(voltage));
                        sysLaytime.setTemperature(Byte.parseByte(temperature));
                        sysLaytime.setType(Byte.parseByte(type));
                        sysLaytime.setTimegmt(new Date());
                        sysLaytime.setIslay(Byte.parseByte("0"));
                        sysLaytime.setSignallevel(Byte.parseByte(gsm_signal_level));
                        sysLaytime.setUpdatetime(new Date());
                        session = ssf.openSession();
                        try{
                            boolean res02 = session.insert("insertSysLaytime", sysLaytime) ==1?true:false;
                            HashMap <String,Object> map_10 = new HashMap<String,Object>();
                            map_10.put("mid",mid);
                            map_10.put("status",Integer.parseInt(status));
//							map_10.put("updatetime",new Date());
                            if(type.equals("1")){
                                boolean res03 = session.update("updatedeviceconfstatus",map_10) ==1?true:false;
                                session.commit();
                                if(res02 && res03){
                                    String command10_resp = Analyse.Command_10_Response(mid,true);
                                    answer = "command10_"+mid+"_"+command10_resp;
                                }
                            }else{
                                session.commit();
                                if(res02){
                                    String command10_resp = Analyse.Command_10_Response(mid,true);
                                    answer = "command10_"+mid+"_"+command10_resp;
                                }
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
                        answer = mid;
                        //收到响应
                        CommandStatusmap.get(mid).put("com02",2);
                        //更新数据库
                        session = ssf.openSession();
                        HashMap <String,Object> map_02 = new HashMap<String,Object>();
                        map_02.put("mid",mid);
                        map_02.put("updatetime",new Date());
                        try{
                            boolean res02 = session.update("updateLayconfigByMid", map_02) ==1?true:false;
                            session.commit();
                            if(res02){
                                //项圈已读取时间配置，并且数据库已更新，删除
                                redisService.remove("time_"+mid);
                                Commandmap.get(mid).remove("com02");
                            }
                            if(Commandmap.get(mid).size()==0){
                                answer = "close";
                            }else{
                                answer = "";
                            }
                            //响应完成，删除
                            CommandStatusmap.get(mid).remove("com02");
                        } catch (Exception e) {
                            e.printStackTrace();
                            session.close();
                            answer = "close";
                        } finally {

                        }
                        break;
                    case "03":
                        String[] command03_receive = Analyse.Command_03_Receive(question);
                        mid = command03_receive[0];
                        answer = mid;
                        //收到响应
                        CommandStatusmap.get(mid).put("com03",2);
                        //更新数据库
                        session = ssf.openSession();
                        HashMap <String,Object> map_03 = new HashMap<String,Object>();
                        map_03.put("mid",mid);
                        map_03.put("updatetime",new Date());
                        try{
                            boolean res03 = session.update("updateDeviceconfByMid", map_03) ==1?true:false;
                            session.commit();
                            if(res03){
                                //说明项圈已读取基础配置，并且数据库已更新，删除
                                redisService.remove("device_"+mid);
                                Commandmap.get(mid).remove("com03");
                            }
                            if(Commandmap.get(mid).size()==0){
                                answer = "close";
                            }else{
                                answer = "";
                            }
                            //响应完成，删除
                            CommandStatusmap.get(mid).remove("com03");
                        } catch (Exception e) {
                            e.printStackTrace();
                            session.close();
                            answer = "close";
                        } finally {

                        }
                        break;
                    case "04":
                        String[] command04 = Analyse.Command_04_Receive(question);
                        mid = command04[0];
                        answer = mid;
                        String command_04 = command04[1];
                        String ip = command04[2];
                        String port = command04[3];
                        String infoupdatecycle = command04[4];
                        String tickcycle = command04[5];
                        String ledenable = command04[6];
                        String tempflag = command04[7];
                        String tempgmt = command04[8];
                        String clearErr = command04[9];
                        String factory = command04[10];
                        //收到响应
                        //正常先收到10命令后再收到04命令
                        if(CommandStatusmap.get(mid)!=null){
                            CommandStatusmap.get(mid).put("com04",2);
                        }else{
                            //没有收到10命令，直接收到04命令,直接处理

                        }
                        //更新数据库
                        session = ssf.openSession();
                        //查询得到命令4后的逻辑
                        try{
                            SysDeviceconf sysDeviceconf = session.selectOne("selectSysDeviceconf",mid);
                            if(sysDeviceconf==null){
                                answer = "close";
                                break;
                            }
                            if(sysDeviceconf.getIp()!=null && sysDeviceconf.getIp()!=""){
                                if(Commandmap.get(mid) !=null){
                                    Commandmap.get(mid).remove("com04");
                                }
                                redisService.remove("04_"+mid);
                            }else{
                                //第一次从硬件拿到绑定的信息
                                sysDeviceconf.setMid(mid);
                                sysDeviceconf.setIp(ip);
                                sysDeviceconf.setPort(Integer.parseInt(port));
                                sysDeviceconf.setInfoupdatecycle(Integer.parseInt(infoupdatecycle));
                                sysDeviceconf.setTickcycle(Integer.parseInt(tickcycle));
                                sysDeviceconf.setLedenable(Byte.parseByte(ledenable));
                                sysDeviceconf.setTemporaryflag(Byte.parseByte(tempflag));
                                if(!tempgmt.equals("0")){
                                    sysDeviceconf.setTemporarygmt(new Date(Long.valueOf(tempgmt+"000")));
                                }else {
                                    sysDeviceconf.setTemporarygmt(new Date());
                                }
                                sysDeviceconf.setClearerr(Byte.parseByte(clearErr));
                                sysDeviceconf.setFactory(Byte.parseByte(factory));
                                sysDeviceconf.setUpdatetime(new Date());
                                sysDeviceconf.setUimodifyflag(Byte.parseByte("0"));
                                sysDeviceconf.setHardmodifyflag(Byte.parseByte("0"));
                                boolean flag = session.update("updateDeviceconf",sysDeviceconf)==1?true:false;
                                session.commit();
                                if(flag){
                                    if(Commandmap.get(mid)!=null){
                                        Commandmap.get(mid).remove("com04");
                                    }
                                    redisService.remove("04_"+mid);
                                }
                            }
                            if(Commandmap.get(mid)!=null && Commandmap.get(mid).size()==0){
                                answer = "close";
                            }else if(Commandmap.get(mid)==null){
                                answer = "close";
                            }
                            else{
                                answer = "";
                            }
                            //响应完成，删除
                            if(CommandStatusmap.get(mid)!=null){
                                CommandStatusmap.get(mid).remove("com04");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            answer = "close";
                        } finally {
                        }
                        break;
                    case "05":
                        String[] command05 = Analyse.Command_05_Receive(question);
                        mid = command05[0];
                        answer = mid;
                        String command_05 = command05[1];
                        String swver = command05[3];
                        String simccid = command05[4];
                        //收到响应
                        //正常先收到10命令后再收到05命令
                        if(CommandStatusmap.get(mid)!=null){
                            CommandStatusmap.get(mid).put("com05",2);
                        }else{
                            //没有收到10命令，直接收到05命令,直接处理

                        }
                        //更新数据库
                        session = ssf.openSession();
                        //查询得到命令5后的逻辑
                        try{
                            SysDeviceconf sysDeviceconf = session.selectOne("selectSysDeviceconf",mid);
                            if(sysDeviceconf==null){
                                answer = "close";
                                break;
                            }
                            if(sysDeviceconf.getSimccid()!=null && sysDeviceconf.getSimccid()!=""){
                                if(Commandmap.get(mid) != null){
                                    Commandmap.get(mid).remove("com05");
                                }
                                redisService.remove("05_"+mid);
                            }else{
                                //第一次从硬件拿到绑定的信息
                                sysDeviceconf.setMid(mid);
                                sysDeviceconf.setSwver(swver);
                                sysDeviceconf.setSimccid(simccid);
                                boolean flag = session.update("updateCCidDeviceconf",sysDeviceconf)==1?true:false;
                                session.commit();
                                if(flag){
                                    if(Commandmap.get(mid) != null){
                                        Commandmap.get(mid).remove("com05");
                                    }
                                    redisService.remove("05_"+mid);
                                }
                            }
                            if(Commandmap.get(mid)!=null && Commandmap.get(mid).size()==0){
                                answer = "close";
                            }else if(Commandmap.get(mid)==null){
                                answer = "close";
                            }
                            else{
                                answer = "";
                            }
                            //响应完成，删除
                            if(CommandStatusmap.get(mid)!=null){
                                CommandStatusmap.get(mid).remove("com05");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            answer = "close";
                        } finally {
                        }
                        break;
                    case "06":
                        String[] command06 = Analyse.Command_06_Receive(question);
                        mid = command06[0];
                        answer = mid;
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
                        //收到响应
                        //正常先收到10命令后再收到06命令
                        if(CommandStatusmap.get(mid)!=null){
                            CommandStatusmap.get(mid).put("com06",2);
                        }else{
                            //没有收到10命令，直接收到06命令,直接处理

                        }
                        //更新数据库
                        session = ssf.openSession();
                        //查询得到命令6后的逻辑
                        try{
                            Systimepos systimepos = session.selectOne("selectSystimepos",mid);
                            if(systimepos==null) {
                                //插入逻辑
                                systimepos = new Systimepos();
                            }
                            if(!time01.equals("0")){
                                systimepos.setOne(new Date(Long.valueOf(time01+"000")));
                            }else {
                                systimepos.setOne(null);
                            }
                            if(!time02.equals("0")){
                                systimepos.setTwo(new Date(Long.valueOf(time02+"000")));
                            }else {
                                systimepos.setTwo(null);
                            }
                            if(!time03.equals("0")){
                                systimepos.setThree(new Date(Long.valueOf(time03+"000")));
                            }else {
                                systimepos.setThree(null);
                            }
                            if(!time04.equals("0")){
                                systimepos.setFour(new Date(Long.valueOf(time04+"000")));
                            }else {
                                systimepos.setFour(null);
                            }
                            if(!time05.equals("0")){
                                systimepos.setFive(new Date(Long.valueOf(time05+"000")));
                            }else {
                                systimepos.setFive(null);
                            }
                            if(!time06.equals("0")){
                                systimepos.setSix(new Date(Long.valueOf(time06+"000")));
                            }else {
                                systimepos.setSix(null);
                            }
                            if(!time07.equals("0")){
                                systimepos.setSeven(new Date(Long.valueOf(time07+"000")));
                            }else {
                                systimepos.setSeven(null);
                            }
                            if(!time08.equals("0")){
                                systimepos.setEight(new Date(Long.valueOf(time08+"000")));
                            }else {
                                systimepos.setEight(null);
                            }
                            if(!time09.equals("0")){
                                systimepos.setNine(new Date(Long.valueOf(time09+"000")));
                            }else {
                                systimepos.setNine(null);
                            }
                            if(!time10.equals("0")){
                                systimepos.setTen(new Date(Long.valueOf(time10+"000")));
                            }else {
                                systimepos.setTen(null);
                            }
                            if(!time11.equals("0")){
                                systimepos.setEleven(new Date(Long.valueOf(time11+"000")));
                            }else {
                                systimepos.setEleven(null);
                            }
                            if(!time12.equals("0")){
                                systimepos.setTwelve(new Date(Long.valueOf(time12+"000")));
                            }else {
                                systimepos.setTwelve(null);
                            }
                            boolean flag = false;
                            if(systimepos.getMid()==null){
                                //插入
                                systimepos.setMid(mid);
                                flag  = session.insert("insertSystimepos", systimepos) ==1?true:false;
                                session.commit();
                            }else{
                                //更新
                                flag = session.update("updateSystimepos", systimepos) ==1?true:false;
                                session.commit();
                            }
                            if(flag && Commandmap.get(mid)!=null){
                                Commandmap.get(mid).remove("com06");
                                redisService.remove("06_"+mid);
                            }
                            if(Commandmap.get(mid)!=null && Commandmap.get(mid).size()==0){
                                answer = "close";
                            }else if(Commandmap.get(mid)==null){
                                answer = "close";
                            }else{
                                answer = "";
                            }
                            //响应完成，删除
                            if(CommandStatusmap.get(mid)!=null){
                                CommandStatusmap.get(mid).remove("com06");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            answer = "close";
                        } finally {
                        }
                        break;
                    default:
                        answer = "close";
                }
            }
        }
        return answer;
    }
}

