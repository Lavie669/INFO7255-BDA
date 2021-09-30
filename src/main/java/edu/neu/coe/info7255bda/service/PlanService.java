package edu.neu.coe.info7255bda.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface PlanService {

    Map<String, String> addByJson(JsonNode jsonData);

    String addByMap(Map<String, Object> map);

    Map<String, String> validateAndAdd(String strJson);

    Object getPlanByKey(String key);
}
