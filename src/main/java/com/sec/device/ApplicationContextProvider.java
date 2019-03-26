package com.sec.device;

import com.sec.device.redis.RedisService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
//import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
/**
 *  2018.8.21
 *  spring boot 项目中  普通类中无法注入service问题解决方案
 *
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;

    @SuppressWarnings("static-access")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}

//============================================================上面的不动============================================================================
class MyThread implements Runnable {

    public RedisService redisService;//换成自己的业务层

//    public MongoTemplate mongoTemplate;

    public MyThread (){
        this.redisService = (RedisService) ApplicationContextProvider.getBean("redisService");//替换成自己的service层
        //或者
        //this.userService = ApplicationContextProvider.getBean(UserService .class);
    }


    @Override
    public void run() {

    }

    //=============================下面是get set方法=====================================================
    public RedisService getRedisService() {
        return redisService;
    }

    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }
}
