package edu.neu.coe.info7255bda.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.PlanService;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PlanService planService;

    @GetMapping("/get/{type}/{id}")
    public Object getByTypeAndID(@PathVariable("type") String objType, @PathVariable("id") String objID){
        return planService.getGraphByKey(objType + '_' + objID);
    }

    @PostMapping("redis/add")
    public ResultData<String> addPlanWithoutValidation(@RequestBody Map<String, Object> paramsMap){
        return ResultData.success(planService.addByMap(paramsMap));
    }

    @GetMapping("redis/getKeys")
    public Set<String> getKeysByPrefix(@RequestBody String prefix){
        return redisUtil.getKeys(prefix);
    }

    @GetMapping("redis/getValue")
    public Object getValueByKey(@RequestBody String key){
        return ResultData.success(redisUtil.getByKey(key));
    }

    @DeleteMapping("redis/del")
    public ResultData<String> delByKey(@RequestBody String key){
        if (redisUtil.delByKey(key)){
            return ResultData.success("Deletion success");
        }
        else {
            throw new Customer400Exception(StatusCode.REDIS_DEL_ERROR.getCode(), StatusCode.REDIS_DEL_ERROR.getMessage());
        }
    }

    @PostMapping("/json/validate")
    public ResultData<String> validate(@RequestBody String strJson){
        return ResultData.success(planService.validatePlan(strJson));
    }

    @PostMapping("/json/findmissing")
    public List<String> findMissing(@RequestBody String strJson){
        return planService.findMissAtPlan(strJson);
    }

}
