package com.ngatdo.elasticsearch.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngatdo.elasticsearch.entity.Index;
import com.ngatdo.elasticsearch.entity.Map;
import com.ngatdo.elasticsearch.entity.Search;
import com.ngatdo.elasticsearch.entity.User;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API chung cho elasticsearch
 */
@RestController
@RequestMapping("")
public class ElasticsearchController {

    @Autowired
    private RestHighLevelClient client;

    /**
     * API tạo một index
     * @param index object index gồm: name, number_of_shards, number_of_replicas
     * @throws IOException
     */
    @PostMapping("/create")
    public String index(@RequestBody Index index) throws IOException
    {
        CreateIndexRequest request = new CreateIndexRequest(index.getName());
        request.settings(Settings.builder()
                .put("index.number_of_shards", index.getNumber_of_shards())
                .put("index.number_of_replicas", index.getNumber_of_replicas())
        );

        GetIndexRequest getIndexRequest = new GetIndexRequest(index.getName());
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if(!exists){
            CreateIndexResponse indexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            return "Create index successfully!!";
        }
        return "Fail to create index, index existed!";
    }

    /**
     * API lấy tất cả dữ liệu của một index
     * @param index
     * @return
     * @throws IOException
     */
    @GetMapping("/{index}/getAll")
    public List<Object> getAll(@PathVariable String index) throws IOException {
        List<Object> objs = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.size(5);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        for(SearchHit searchHit : searchResponse.getHits().getHits()){
            Object obj = new ObjectMapper().readValue(searchHit.getSourceAsString(),Object.class);
            objs.add(obj);
        }
        return objs;
    }
    /**
     * API search
     * @param search object search gồm: fields, values
     * @return
     * @throws IOException
     */
    @PostMapping("/{index}/search")
    public Object search(@PathVariable("index") String index, @RequestBody Search search) throws IOException {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        for (Map entry : search.getMatch()){
            boolQuery.must(QueryBuilders.matchQuery(entry.getField(), entry.getValue()));
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        sourceBuilder.from(0);
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//        if(search.getSort() != null){
//            if(search.getSort().getAsc() == 1)
//                sourceBuilder.sort(new FieldSortBuilder(search.getSort().getSortBy()).order(SortOrder.ASC));
//             else   sourceBuilder.sort(new FieldSortBuilder(search.getSort().getSortBy()).order(SortOrder.DESC));
//
//        }
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
        return searchResponse.getHits().getHits();
    }
}
