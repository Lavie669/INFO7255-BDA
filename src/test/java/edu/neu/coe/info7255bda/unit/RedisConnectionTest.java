package edu.neu.coe.info7255bda.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired( required = false)
    private RedisTemplate redisTemplate;

    @Test
    void getName(){
        redisTemplate.opsForValue().set("name","Gan Li");
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
}
