package edu.neu.coe.info7255bda.utils.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.neu.coe.info7255bda.constant.Constant;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class ElasticsearchUtil {

    @Resource
    private RestHighLevelClient client;

    public boolean createIndexByJson(String index, String strJson){
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        createIndexRequest.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
        );
        createIndexRequest.mapping(strJson, XContentType.JSON);
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        }catch (Exception e){
            log.error("Failed to create index: " + index + " ,error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteIndex(String index){
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
        try {
            AcknowledgedResponse deleteIndexResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            return deleteIndexResponse.isAcknowledged();
        }catch (Exception e){
            log.error("Failed to delete index: " + index + " ,error: " + e.getMessage());
            return false;
        }
    }

    public boolean addDocument(JsonNode jsonNode) throws Exception {
        IndexRequest indexRequest = new IndexRequest(Constant.INDEX)
                .id(jsonNode.get(Constant.OBJECT_ID).asText())
                .source(jsonNode.asText(), XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.status().equals(RestStatus.OK);
    }

    private boolean addDocument(String strJson, String id) throws Exception {
        IndexRequest indexRequest = new IndexRequest(Constant.INDEX)
                .id(id).source(strJson, XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.status().equals(RestStatus.OK);
    }

    private boolean addDocument(String strJson, String childID, String routingID) throws Exception {
        IndexRequest indexRequest = new IndexRequest(Constant.INDEX)
                .id(childID).routing(routingID).source(strJson, XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.status().equals(RestStatus.OK);
    }

    public boolean addPlanDocument(String strJson){
        Map<String, String> map = convert2JoinRelation(JsonValidateUtil.str2JsonNode(strJson), "");
        AtomicBoolean flag = new AtomicBoolean(true);
        map.forEach((k, v) -> {
            try {
                addJoinFieldAndIndex(k, v);
            }catch (Exception e){
                log.warn("Failed to add plan as Join relation: " + e.getMessage());
                flag.set(false);
            }
        });
        return flag.get();
    }

    private void addJoinFieldAndIndex(String k, String v) throws Exception {
        JSONObject json = JSON.parseObject(v);

        String type = json.get(Constant.OBJECT_TYPE).toString();
        String[] keys = k.split(Constant.SPLIT);
        if (keys.length == 1){
            json.put(Constant.JOIN_FIELD, type);
            addDocument(json.toJSONString(), k);
        }
        else if (keys.length == 2){
            JSONObject jsonObject = new JSONObject();
            String parentID = keys[0];
            String childID = keys[1];

            jsonObject.put(Constant.PARENT_PROP, parentID);
            jsonObject.put(Constant.CHILD_PROP, type);
            json.put(Constant.JOIN_FIELD, jsonObject);
            addDocument(json.toJSONString(), childID, parentID);
        }
        else {
            JSONObject jsonObject = new JSONObject();
            String grandParentID = keys[0];
            String parentID = keys[1];
            String childID = keys[2];

            jsonObject.put(Constant.PARENT_PROP, parentID);
            if (type.equals(Constant.LINKED_PROP)){
                jsonObject.put(Constant.CHILD_PROP, Constant.LINKED_PREFIX + type);
            }else {
                jsonObject.put(Constant.CHILD_PROP, type);
            }
            json.put(Constant.JOIN_FIELD, jsonObject);
            addDocument(json.toJSONString(), childID, grandParentID);
        }
    }

    private static Map<String, String> convert2JoinRelation(JsonNode jsonData, String parentID){
        Map<String, String> map = new HashMap<>();
        String id = jsonData.get(Constant.OBJECT_ID).asText();
        String ownKey;
        if (parentID.isEmpty()){
            ownKey = id;
        }
        else {
            ownKey = parentID + '_' + id;
        }
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        Iterator<String> iterator = jsonData.fieldNames();
        while (iterator.hasNext()){
            String field = iterator.next();
            JsonNode node = jsonData.get(field);
            if (node.isContainerNode()){
                if (node.isArray()){
                    for (JsonNode n : node){
                        map.putAll(convert2JoinRelation(n, ownKey));
                    }
                }else {
                    map.putAll(convert2JoinRelation(node, ownKey));
                }
            }else {
                objectNode.set(field, node);
            }
        }
        map.put(ownKey, objectNode.toString());
        return map;
    }

    public static void main(String[] args) throws Exception {
        String strJson = "{\n" +
                "  \"planCostShares\": {\n" +
                "    \"deductible\": 2000,\n" +
                "    \"_org\": \"example.com\",\n" +
                "    \"copay\": 23,\n" +
                "    \"objectId\": \"1234vxc2324sdf-501\",\n" +
                "    \"objectType\": \"membercostshare\"\n" +
                "  },\n" +
                "  \"linkedPlanServices\": [\n" +
                "    {\n" +
                "      \"linkedService\": {\n" +
                "        \"_org\": \"example.com\",\n" +
                "        \"objectId\": \"1234520xvc30asdf-502\",\n" +
                "        \"objectType\": \"service\",\n" +
                "        \"name\": \"Yearly physical\"\n" +
                "      },\n" +
                "      \"planserviceCostShares\": {\n" +
                "        \"deductible\": 10,\n" +
                "        \"_org\": \"example.com\",\n" +
                "        \"copay\": 0,\n" +
                "        \"objectId\": \"1234512xvc1314asdfs-503\",\n" +
                "        \"objectType\": \"membercostshare\"\n" +
                "      },\n" +
                "      \"_org\": \"example.com\",\n" +
                "      \"objectId\": \"27283xvx9asdff-504\",\n" +
                "      \"objectType\": \"planservice\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"_org\": \"example.com\",\n" +
                "  \"objectId\": \"12xvxc345ssdsds-508\",\n" +
                "  \"objectType\": \"plan\",\n" +
                "  \"planType\": \"inNetwork\",\n" +
                "  \"creationDate\": \"12-12-2017\"\n" +
                "}";
        JsonNode jsonNode = JsonValidateUtil.str2JsonNode(strJson);
        Map<String, String> map = convert2JoinRelation(jsonNode, "");
//        map.forEach((k, v) -> {
//            System.out.println(addJoinField(k, v));
//        });
        System.out.println(map);
    }
}
