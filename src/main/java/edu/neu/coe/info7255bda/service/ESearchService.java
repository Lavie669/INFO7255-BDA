package edu.neu.coe.info7255bda.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.elasticsearch.search.SearchHit;

public interface ESearchService {

    boolean createIndexByJson(String index, String strJson);

    boolean deleteIndex(String index);

    boolean addPlanDocument(String strJson);

    SearchHit[] searchByChildAndParentType(String parentType, String childType);
}
