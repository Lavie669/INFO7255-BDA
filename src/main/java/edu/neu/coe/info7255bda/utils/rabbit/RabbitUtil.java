package edu.neu.coe.info7255bda.utils.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class RabbitUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean sendDirectTestMessage(){
        String context = "hello----"+ LocalDateTime.now();
        rabbitTemplate.convertAndSend("TestDirectQueue",context);
        return true;
    }

    public void sendDirectMessage(String routingKey, Object object){
        log.info("Sending message to queue: " + routingKey);
        rabbitTemplate.convertAndSend(routingKey,object);
    }
}
