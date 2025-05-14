package org.mango.mangobot.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.AcknowledgedResponse;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.mango.mangobot.model.document.TextDocument;
import org.mango.mangobot.service.EsDocumentService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


@Service
public class EsDocumentServiceImpl implements EsDocumentService {

    private final ElasticsearchClient client;

    public EsDocumentServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 创建索引
     * @param indexName
     * @return
     */
    @Override
    public boolean createIndexWithMapping(String indexName){
        if (indexExists(indexName)) {
            System.out.println("Index already exists.");
            return false;
        }

        String mappingJson = null;
        try {
            mappingJson = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("mapping/knowledge_library_mapping.json").toURI())));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        InputStream is = new ByteArrayInputStream(mappingJson.getBytes(StandardCharsets.UTF_8));

        CreateIndexRequest request = CreateIndexRequest.of(b -> b
                .index(indexName)
                .withJson(is)
        );

        CreateIndexResponse response = null;
        try {
            response = client.indices().create(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response.acknowledged();
    }

    @Override
    public void addDocument(String indexName, TextDocument document) throws IOException {
        String json = objectMapper.writeValueAsString(Map.of(
                "content", document.getContent(),
                "vector_embedding", document.getVectorEmbedding()
        ));

        IndexRequest<InputStream> request = IndexRequest.of(b -> b
                .index(indexName)
                .id(document.getId())
                .withJson(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
        );

        client.index(request);
    }

    @Override
    public TextDocument getDocumentById(String indexName, String docId) throws IOException {
        GetRequest request = GetRequest.of(b -> b
                .index(indexName)
                .id(docId)
        );

        GetResponse<Object> response = client.get(request, Object.class);

        if (!response.found()) {
            return null;
        }

        Map<String, Object> source = (Map<String, Object>) response.source();
        TextDocument document = new TextDocument();
        document.setId(docId);
        document.setContent((String) source.get("content"));

        @SuppressWarnings("unchecked")
        List<Double> vectorList = (List<Double>) source.get("vector_embedding");
        float[] vectorArray = new float[vectorList.size()];
        for (int i = 0; i < vectorArray.length; i++) {
            vectorArray[i] = vectorList.get(i).floatValue();
        }
        document.setVectorEmbedding(vectorArray);

        return document;
    }

    @Override
    public List<Map<String, Object>> searchDocuments(
            String indexName,
            String queryText,
            float[] vector,
            int size
    ){
        List<Float> vectorList = IntStream.range(0, vector.length).mapToObj(i -> vector[i]).toList();
        // 构建 knn 查询
        KnnQuery knnQuery = KnnQuery.of(b -> b
                .field("vector_embedding")
                .queryVector(vectorList)
                .numCandidates(100L)
        );

        // 构建 match 查询
        MatchQuery matchQuery = MatchQuery.of(b -> b
                .field("content")
                .query(queryText)
        );

        // 构建 bool 查询：结合全文和向量
        BoolQuery boolQuery = BoolQuery.of(b -> b
                .should(Query.of(q -> q.match(matchQuery)))
                .should(Query.of(q -> q.knn(knnQuery)))
        );

        // 构建搜索请求
        SearchRequest request = SearchRequest.of(b -> b
                .index(indexName)
                .query(Query.of(q -> q.bool(boolQuery)))
                .size(size)
        );

        SearchResponse<Map> response = null;
        try {
            response = client.search(request, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Map<String, Object>> results = new ArrayList<>();

        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source != null) {
                source.put("id", hit.id());
                source.put("score", hit.score());
                results.add(source);
            }
        }

        return results;
    }

    @Override
    public List<Map<String, Object>> fullTextSearch(String indexName, String queryText, int size) {

        MatchQuery matchQuery = MatchQuery.of(b -> b
                .field("content")
                .query(queryText)
        );

        SearchRequest request = SearchRequest.of(b -> b
                .index(indexName)
                .query(Query.of(q -> q.match(matchQuery)))
                .size(size)
        );

        SearchResponse<Map> response = null;
        try {
            response = client.search(request, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Map<String, Object>> results = new ArrayList<>();

        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source != null) {
                source.put("id", hit.id());
                source.put("score", hit.score());
                results.add(source);
            }
        }

        return results;
    }

    @Override
    public List<Map<String, Object>> vectorSearch(String indexName, float[] vector, int size){

        List<Float> vectorList = IntStream.range(0, vector.length).mapToObj(i -> vector[i]).toList();
        KnnQuery knnQuery = KnnQuery.of(b -> b
                .field("vector_embedding")
                .queryVector(vectorList)
                .numCandidates(100L)
        );

        SearchRequest request = SearchRequest.of(b -> b
                .index(indexName)
                .query(Query.of(q -> q.knn(knnQuery)))
                .size(size)
        );

        SearchResponse<Map> response = null;
        try {
            response = client.search(request, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Map<String, Object>> results = new ArrayList<>();

        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source != null) {
                source.put("id", hit.id());
                source.put("score", hit.score());
                results.add(source);
            }
        }

        return results;
    }

    @Override
    public boolean deleteDocumentById(String indexName, String docId) throws IOException {
        DeleteRequest request = DeleteRequest.of(b -> b
                .index(indexName)
                .id(docId)
        );

        DeleteResponse response = client.delete(request);

        return response.result() != null && !response.result().toString().contains("NotFound");
    }

    @Override
    public boolean deleteIndex(String indexName) throws IOException {
        // 检查索引是否存在
        boolean exists = client.indices().exists(e -> e.index(indexName)).value();

        if (!exists) {
            System.out.println("索引 [" + indexName + "] 不存在");
            return false;
        }

        // 删除索引
        DeleteIndexRequest deleteRequest = DeleteIndexRequest.of(b -> b
                .index(indexName)
        );

        AcknowledgedResponse response = client.indices().delete(deleteRequest);

        return response.acknowledged();
    }



    private boolean indexExists(String indexName){
        ExistsRequest request = ExistsRequest.of(b -> b.index(indexName));
        try {
            return client.indices().exists(request).value();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}