package edu.neu.coe.info7255bda.utils.redis;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class RedisUtil {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public Set<String> getKeys(String keys){
        try {
            return redisTemplate.keys(keys);
        }catch (Exception e){
            //throw new CustomerException(StatusCode.);
            e.printStackTrace();
            return null;
        }
    }

    public Object getByKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean setKV(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean delByKey(String key){
        if (key != null){
            return redisTemplate.delete(key);
        }
        else {
            return false;
        }
    }
}
