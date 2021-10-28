package edu.neu.coe.info7255bda.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface PlanService {

    Map<String, String> addByJson(JsonNode jsonData);

    String addByMap(Map<String, Object> map);

    Map<String, String> validateAndAdd(String strJson);

    Object getPlanByKey(String key);

    String delPlanByKey(String key);

    String validatePlan(String strJson);

    List<String> findMissAtPlan(String strJson);

    JsonNode getSchema();

    Map<String, String> validateAndAddAsGraph(String strJson);

    String addSchema(String strSchema);

    Object getGraphByKey(String key);

    Object getGraphWithEtag(String key, String eTag);

    Object getValueWithEtag(String key, String eTag);

    String delGraphByKey(String key);

    String delEdgeByKey(String key);

    String delEdgeByKeyAndN(String key, int n);

    Map<String, String> addOtherPlan(String strJson);

    String updatePlan(String key, String strJson);
}
