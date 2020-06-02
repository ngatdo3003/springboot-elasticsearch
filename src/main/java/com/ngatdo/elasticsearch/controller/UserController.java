package com.ngatdo.elasticsearch.controller;

import com.ngatdo.elasticsearch.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controller cho users, index mặc định để đẩy dữ liệu là users
 */
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private RestHighLevelClient client;

    public static final IndexRequest request = new IndexRequest("users");
    public static final SearchRequest searchRequest = new SearchRequest("users");


    /**
     * Hàm save user
     * @param user
     * @return
     * @throws IOException
     */
    @PostMapping("/save")
    public String save(@RequestBody User user) throws IOException {
        // khởi tạo id cho document = id của user/ nếu id null thì sẽ gán cho nó một gía trị ngẫu nhiên
        request.id(user.getUserId());
        // gán source cho document
        request.source(new ObjectMapper().writeValueAsString(user), XContentType.JSON);
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        return indexResponse.toString();
    }

    /**
     * Hàm save user async(không đồng bộ, nên dùng)
     * @param user
     * @throws IOException
     */
    @PostMapping("/save/async")
    public String saveAsync(@RequestBody User user) throws IOException {
        request.id(user.getUserId());
        request.source(new ObjectMapper().writeValueAsString(user), XContentType.JSON);
        client.indexAsync(request, RequestOptions.DEFAULT,listener);
        return "Request submitted !!!";
    }

    /**
     * Hàm lấy thông tin user theo id
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("/{id}")
    public User read(@PathVariable final String id) throws IOException {
        GetRequest getRequest = new GetRequest("users",id);
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        User user = new ObjectMapper().readValue(getResponse.getSourceAsString(),User.class);
        System.out.println(getResponse.toString());
        return user;
    }

    @GetMapping("/")
    public List<User> readAll() throws IOException {
        List<User> users = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.size(5);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        for(SearchHit searchHit : searchResponse.getHits().getHits()){
            User user = new ObjectMapper().readValue(searchHit.getSourceAsString(),User.class);
            users.add(user);
        }
        return users;
    }

    @GetMapping("/name/{value}")
    public List<User> searchByName(@PathVariable final String value) throws IOException {
        List<User> users = new ArrayList<>();
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", value)
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(2)
                .maxExpansions(10);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));


        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
        for(SearchHit searchHit : searchResponse.getHits().getHits()){
            User user = new ObjectMapper().readValue(searchHit.getSourceAsString(),User.class);
            users.add(user);
        }
        return users;
    }

    @RequestMapping(value = "/",method =RequestMethod.PUT)
    public String update(@RequestBody User user) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("users",user.getUserId());
        updateRequest.doc(new ObjectMapper().writeValueAsString(user), XContentType.JSON);
        UpdateResponse updateResponse = client.update(updateRequest,RequestOptions.DEFAULT);
        System.out.println(updateResponse.getGetResult());

        return updateResponse.status().name();
    }

    /**
     * Hàm xoá user theo id
     * @param id
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    public String delete(@PathVariable final String id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("users",id);
        DeleteResponse deleteResponse = client.delete(deleteRequest,RequestOptions.DEFAULT);
        return deleteResponse.getResult().name();
    }

    /**
     * Hàm lắng nghe sự kiện
     * Phục vụ cho các hàm async khi có response
     */
    ActionListener listener = new ActionListener<IndexResponse>() {
        @Override
        public void onResponse(IndexResponse indexResponse) {
            System.out.println(indexResponse.toString());
            System.out.println(" Document updated successfully !!!");
        }

        @Override
        public void onFailure(Exception e) {
            System.out.print(" Document creation failed !!!"+ e.getMessage());
        }
    };



}
