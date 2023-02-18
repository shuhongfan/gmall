package com.atguigu.gmall.task.scheduled;

import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling //开启定时任务的支持
@Slf4j
public class ScheduledTask {


    @Autowired
    private RabbitService rabbitService;
    /**
     * 参数值：
     *  秒 分 时  日 月 星期 年（不写）
     *   *：任何时间
     *   日和星期
     *
     */
    @Scheduled(cron = "0/15 * * * * ? ")
    public void tesk1(){

        System.out.println("执行了");

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,
                MqConst.ROUTING_TASK_1,"");

    }

    /**
     * 每天下午18点执行
     */
//@Scheduled(cron = "0/35 * * * * ?")
    @Scheduled(cron = "0 0 18 * * ?")
    public void task18() {

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_18, "");
    }

}
