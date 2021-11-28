package edu.neu.coe.info7255bda.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import edu.neu.coe.info7255bda.service.ESearchService;
import edu.neu.coe.info7255bda.utils.es.ElasticsearchUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("esService")
public class ESearchServiceImpl implements ESearchService {

    @Autowired
    private ElasticsearchUtil esUtil;

    @SneakyThrows
    @Override
    public boolean createIndexByJson(String index, String strJson) {
        return esUtil.createIndexByJson(index, strJson);
    }

    @SneakyThrows
    @Override
    public boolean deleteIndex(String index) {
        return esUtil.deleteIndex(index);
    }

    @Override
    public boolean addPlanDocument(String strJson) {
        return esUtil.addPlanDocument(strJson);
    }
}
