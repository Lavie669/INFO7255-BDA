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
        Map<String, String> res = planService.validateAndAdd(strJson);
        String token = '"' + DigestUtils.md5DigestAsHex(JsonValidateUtil.str2JsonNode(strJson).get("creationDate").asText().getBytes()) + '"';
        response.addHeader("ETag", token);
        return res;
    }

    @PostMapping("/add/graph")
    public Map<String, String> addPlanAsGraph(@RequestBody String strJson, HttpServletResponse response){
        Map<String, String> res = planService.validateAndAddAsGraph(strJson);
        String token = '"' + DigestUtils.md5DigestAsHex(JsonValidateUtil.str2JsonNode(strJson).get("creationDate").asText().getBytes()) + '"';
        response.addHeader("ETag", token);
        return res;
    }

    @PostMapping("add/schema")
    public ResultData<String> addPlanSchema(@RequestBody String strSchema){
        return ResultData.success(planService.addSchema(strSchema));
    }

    @PostMapping("/add/other")
    public Map<String, String> addPlanAsGraph(@RequestBody String strJson){
        return planService.addOtherPlan(strJson);
    }

    @GetMapping("/get/{type}/{id}")
    public Object getByTypeAndID(@PathVariable("type") String objType, @PathVariable("id") String objID){
        return planService.getGraphByKey(objType + '_' + objID);
    }

    @GetMapping("/get/{id}")
    public Object getPlanByKey(@PathVariable("id") String key, HttpServletRequest request){
        Object obj = planService.getPlanByKey(key);
        JsonNode jsonNode = JsonValidateUtil.str2JsonNode(JSON.toJSONString(obj));
        if (jsonNode.hasNonNull("creationDate")){
            String token = jsonNode.get("creationDate").asText();
            String previousToken = request.getHeader("If-None-Match");
            if (previousToken != null && previousToken.equals(DigestUtils.md5DigestAsHex(token.getBytes()))){
                throw new Customer304Exception(StatusCode.NOT_MODIFIED.getCode(), StatusCode.NOT_MODIFIED.getMessage());
            }
        }
        return obj;
    }

    @DeleteMapping("/del/{id}")
    public ResultData<String> delByKey(@PathVariable("id") String key){
        return ResultData.success(planService.delPlanByKey(key));
    }

    @DeleteMapping("/del/{type}/{id}")
    public ResultData<String> delByTypeAndID(@PathVariable("type") String objType, @PathVariable("id") String objID){
        return ResultData.success(planService.delGraphByKey(objType + '_' + objID));
    }

    @DeleteMapping("/del/{type}/{id}/{type2}")
    public ResultData<String> delPlanEdge(@PathVariable("type") String objType, @PathVariable("id") String objID,
                                            @PathVariable("type2") String objType2){
        return ResultData.success(planService.delEdgeByKey(objType + '_' + objID + '_' + objType2));
    }

    @DeleteMapping("/del/{type}/{id}/{type2}/{n}")
    public ResultData<String> delPlanNthEdge(@PathVariable("type") String objType, @PathVariable("id") String objID,
                                          @PathVariable("type2") String objType2, @PathVariable("n") String n){
        return ResultData.success(planService.delEdgeByKeyAndN(objType + '_' + objID + '_' + objType2, Integer.parseInt(n)));
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