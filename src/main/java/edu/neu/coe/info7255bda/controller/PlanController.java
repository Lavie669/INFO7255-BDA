package edu.neu.coe.info7255bda.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.PlanService;
import edu.neu.coe.info7255bda.utils.exception.Customer304Exception;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Plan!";
    }

    @PostMapping("/add")
    public Map<String, String> addPlan(@RequestBody String strJson, HttpServletResponse response){
        JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
        if (!jsonData.isEmpty()){
            String token = '"' + DigestUtils.md5DigestAsHex(jsonData.get("creationDate").asText().getBytes()) + '"';
            response.addHeader("ETag", token);
        }
        return planService.validateAndAdd(strJson);
    }

    @GetMapping("/get/{id}")
    public Object getPlanByKey(@PathVariable("id") String key, HttpServletRequest request){
        Object obj = planService.getPlanByKey(key);
        String token = JsonValidateUtil.str2JsonNode(JSON.toJSONString(obj)).get("creationDate").asText();
        String previousToken = request.getHeader("If-None-Match");
        if (previousToken != null && previousToken.equals(DigestUtils.md5DigestAsHex(token.getBytes()))){
            throw new Customer304Exception(StatusCode.NOT_MODIFIED.getCode(), StatusCode.NOT_MODIFIED.getMessage());
        }
        return obj;
    }

    @DeleteMapping("/del/{id}")
    public ResultData<String> delByKey(@PathVariable("id") String key){
        return ResultData.success(planService.delPlanByKey(key));
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