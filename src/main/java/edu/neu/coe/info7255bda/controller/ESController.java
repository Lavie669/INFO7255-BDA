package edu.neu.coe.info7255bda.controller;

import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.ESearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/es")
public class ESController {

    @Autowired
    private ESearchService esService;

    @PostMapping("/create/{index}")
    public Object createUserIndex(@PathVariable("index") String index, @RequestBody String strJson){
        if (esService.createIndexByJson(index, strJson)){
            return ResultData.success("Successfully create index: "+ index);
        }
        else {
            return ResultData.fail(400, "Failed to create index: " + index);
        }
    }

    @DeleteMapping("/delete")
    public Object deleteUserIndex(@RequestBody String index){
        if (esService.deleteIndex(index)){
            return ResultData.success("Successfully delete index: "+ index);
        }
        else {
            return ResultData.fail(400, "Failed to delete index: " + index);
        }
    }

    @PutMapping("/put")
    public Object putPlanDocument(@RequestBody String strJson){
        if (esService.addPlanDocument(strJson)){
            return ResultData.success("Successfully add plan to ES");
        }else {
            return ResultData.fail(400, "Failed to add plan to ES");
        }
    }
}
