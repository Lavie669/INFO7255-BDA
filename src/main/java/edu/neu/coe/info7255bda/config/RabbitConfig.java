package edu.neu.coe.info7255bda.config;

import edu.neu.coe.info7255bda.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfig {

    @Bean
    public Queue TestDirectQueue() {
        return new Queue("TestDirectQueue", true);
    }

    @Bean
    public Queue ESIndexingQueue() {
        return new Queue(Constant.ES_INDEX_QUEUE, true);
    }

    @Bean
    public Queue ESUpdateQueue() {
        return new Queue(Constant.ES_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue ESDeleteQueue() {
        return new Queue(Constant.ES_DELETE_QUEUE, true);
    }

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((data, ack, cause) -> {
            if (!ack) {
                log.warn("Failed to send message!" + cause + data);
            } else {
                log.info("Successfully send message to RabbitMQ server");
            }
        });
        return rabbitTemplate;
    }


//    @Bean
//    DirectExchange TestDirectExchange() {
//        return new DirectExchange("TestDirectExchange", true, false);
//    }
//
//    @Bean
//    Binding bindingDirect() {
//        return BindingBuilder.bind(TestDirectQueue()).to(TestDirectExchange()).with("TestDirectRouting");
//    }
//
//    @Bean
//    DirectExchange lonelyDirectExchange() {
//        return new DirectExchange("lonelyDirectExchange");
//    }
}
