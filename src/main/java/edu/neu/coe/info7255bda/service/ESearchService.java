package edu.neu.coe.info7255bda.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface ESearchService {

    boolean createIndexByJson(String index, String strJson);

    boolean deleteIndex(String index);

    boolean addPlanDocument(String strJson);
}
