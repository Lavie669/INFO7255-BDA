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
@RequestMapping("/plan")
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

    @PostMapping("/add/schema")
    public Object addPlanSchema(@RequestBody String strSchema){
        return ResultData.success(planService.addSchema(strSchema));
    }

    @GetMapping("/get/schema")
    public Object getPlanSchema(){
        return planService.getSchema();
    }

    @PostMapping("/add/other")
    public Map<String, String> addPlanAsGraph(@RequestBody String strJson){
        return planService.addOtherPlan(strJson);
    }

    @GetMapping("/get/{type}/{id}")
    public Object getByTypeAndID(@PathVariable("type") String objType, @PathVariable("id") String objID, HttpServletRequest request){
        String previousToken = request.getHeader("If-None-Match");
        return planService.getGraphWithEtag(objType + '_' + objID, previousToken);
    }

    @GetMapping("/get/{id}")
    public Object getPlanByKey(@PathVariable("id") String key, HttpServletRequest request){
        String previousToken = request.getHeader("If-None-Match");
        return planService.getValueWithEtag(key, previousToken);
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

    @PatchMapping("/update/{type}/{id}")
    public ResultData<String> updatePlan(@PathVariable("type") String objType, @PathVariable("id") String objID,
                                         @RequestBody String strJson, HttpServletRequest request, HttpServletResponse response){
        String previousToken = request.getHeader("If-None-Match");
        String res = planService.updatePlanWithEtag(objType + '_' + objID, strJson, previousToken);
        if (res == null){
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            return ResultData.fail(HttpServletResponse.SC_PRECONDITION_FAILED, "Precondition Failed");
        }
        String plan = planService.getPlanByKey(objType + '_' + objID).toString();
        String token = '"' + DigestUtils.md5DigestAsHex(JsonValidateUtil.str2JsonNode(plan).get("creationDate").asText().getBytes()) + '"';
        response.addHeader("ETag", token);
        return ResultData.success(res);
    }

    @PutMapping("/update/{type}/{id}")
    public ResultData<String> updateAllPlan(@PathVariable("type") String objType, @PathVariable("id") String objID,
                                         @RequestBody String strJson, HttpServletRequest request, HttpServletResponse response){
        String previousToken = request.getHeader("If-None-Match");
        String token = '"' + DigestUtils.md5DigestAsHex(JsonValidateUtil.str2JsonNode(strJson).get("creationDate").asText().getBytes()) + '"';
        response.addHeader("ETag", token);
        String res = planService.updateAllPlanWithEtag(objType + '_' + objID, strJson, previousToken);
        if (res == null){
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            return ResultData.fail(HttpServletResponse.SC_PRECONDITION_FAILED, "Precondition Failed");
        }
        return ResultData.success(res);
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