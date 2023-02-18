package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import jodd.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import sun.applet.Main;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 切点： 你要对哪块功能进行增强，要增强的那个点，可以是某个包、某个类、某个方法
 * 切面： 要增强的内容是什么？
 *
 */
@Component
@Aspect
public class GmallCacheAspect {


    @Autowired
    private RedissonClient redissonClient;

    /**
     *
     * 使用aop实现分布式锁和缓存
     *  Around:环绕通知
     *    value:表示声明切入的位置
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     *
     * 1.定义获取数据的key
     *  例如：获取skuInfo    key ===sku:skuId
     *   (1)获取添加了@Gmallcache注解的方法
     *      可以获取注解 可以获取注解的属性  可以获取方法的参数
     *   （2）可以尝试获取数据
     *
     *
     */
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheGmallAspect(ProceedingJoinPoint joinPoint) throws Throwable {
       //创建返回对象
        Object object=new Object();
        //获取添加了注解的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取注解
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        //获取属性前缀
        String prefix = gmallCache.prefix();
        //获取属性后缀
        String suffix = gmallCache.suffix();
        //获取方法传入的参数
        Object[] args = joinPoint.getArgs();
        //组合获取数据的key
        String key =prefix+ Arrays.asList(args).toString()+suffix;
        //从缓存中获取数据
        object=cacheHit(key,signature);
        try {
            //判断
            if(object==null){
                //缓存中没有数据，需要从数据库查询
                //定义锁的key
                String lockKey=prefix+":lock";
                //准备上锁 redis redisson
                RLock lock = redissonClient.getLock(lockKey);
                //上锁
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1,
                        RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断是否成功
                if(flag){
                    try {
                        //获取到了锁
                        //查询数据库，-执行切入的方法的方法体实际上就是查询数据库
                        object= joinPoint.proceed(args);

                        //判断是否从mysql查询到了数据
                        if(object==null){


                            //创建空值
//                            object=new Object();
                            //反射-获取返回值类型的字节码对象
                            Class aClass = signature.getReturnType();
                            //创建对象
                            object = aClass.newInstance();
                           //存储
                            redisTemplate.opsForValue().set(key,JSON.toJSONString(object),
                                    RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);

                            return object;

                        }else{

                            //存储
                            redisTemplate.opsForValue().set(key,JSON.toJSONString(object),
                                    RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);

                            return object;


                        }
                    } finally {
                        //释放锁
                        lock.unlock();

                    }


                }else{
                    //睡眠
                    Thread.sleep(100);
                    //自旋
                    return cacheGmallAspect(joinPoint);
                }


            }else{


                //从缓存中获取了数据
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


        //兜底的方法--查询数据库，实际上执行方法体就是查询数据库
        return joinPoint.proceed(args);


    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 从缓存中获取数据
     * @param key
     * @param signature
     * @return
     */
    private Object cacheHit(String key, MethodSignature signature) {
        //获取数据--存储的时候，转换成Json字符串
        String strJson = (String) redisTemplate.opsForValue().get(key);

        //判断
        if(!StringUtil.isEmpty(strJson)){


            //获取当前方法的返回值类型
            Class returnType = signature.getReturnType();
            //将字符串转换成指定的类型
            return JSON.parseObject(strJson,returnType);

        }

        return null;
    }


}
