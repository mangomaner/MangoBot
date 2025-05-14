package org.mango.mangobot.service;

import org.mango.mangobot.model.document.TextDocument;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EsDocumentService {
    /**
     * 创建索引
     * @param indexName
     * @return
     */
    boolean createIndexWithMapping(String indexName);

    /**
     * 添加文档
     * @param indexName
     * @param document
     */
    void addDocument(String indexName, TextDocument document) throws IOException;

    /**
     * 根据索引id获取文档
     * @param indexName
     * @param docId
     * @return
     * @throws IOException
     */
    TextDocument getDocumentById(String indexName, String docId) throws IOException;

    /**
     * 搜索文档（关键词与向量混合搜索）
     * @param indexName
     * @param queryText
     * @param vector
     * @param size
     * @return
     */
    List<Map<String, Object>> searchDocuments(
            String indexName,
            String queryText,
            float[] vector,
            int size
    );

    /**
     * 全文检索：根据关键词搜索文档
     */
    List<Map<String, Object>> fullTextSearch(String indexName, String queryText, int size);

    /**
     * 向量检索：根据向量进行相似度匹配
     */
    List<Map<String, Object>> vectorSearch(String indexName, float[] vector, int size);

    /**
     * 删除指定索引下的某个文档（根据 docId）
     */
    boolean deleteDocumentById(String indexName, String docId) throws IOException;

    /**
     * 删除整个索引（谨慎操作）
     */
    boolean deleteIndex(String indexName) throws IOException;
}