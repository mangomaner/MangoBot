package org.mango.mangobot.controller;

import org.mango.mangobot.model.document.TextDocument;
import org.mango.mangobot.service.EsDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/es")
public class EsDocumentController {

    private final EsDocumentService esDocumentService;

    public EsDocumentController(EsDocumentService esDocumentService) {
        this.esDocumentService = esDocumentService;
    }

    @PostMapping("/create-index")
    public String createIndex(@RequestParam String indexName) {
        try {
            if (esDocumentService.createIndexWithMapping(indexName)) {
                return "索引 [" + indexName + "] 创建成功";
            } else {
                return "索引 [" + indexName + "] 创建失败或已存在";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "创建索引时发生错误：" + e.getMessage();
        }
    }

    @PostMapping("/add-document")
    public String addDocument(@RequestBody TextDocument document,
                              @RequestParam String indexName) {
        try {
            esDocumentService.addDocument(indexName, document);
            return "文档 ID [" + document.getId() + "] 已添加到索引 [" + indexName + "]";
        } catch (Exception e) {
            e.printStackTrace();
            return "添加文档失败：" + e.getMessage();
        }
    }
    @GetMapping("/get-document")
    public ResponseEntity<?> getDocument(@RequestParam String indexName, @RequestParam String docId) {
        try {
            TextDocument document = esDocumentService.getDocumentById(indexName, docId);
            if (document == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文档不存在");
            }
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchDocuments(
            @RequestParam String indexName,
            @RequestParam String queryText,
            @RequestParam(defaultValue = "5") int size
    ) {
        try {
            List<Map<String, Object>> result = esDocumentService.searchDocuments(indexName, queryText, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/search-fulltext")
    public ResponseEntity<List<Map<String, Object>>> searchFullText(
            @RequestParam String indexName,
            @RequestParam String queryText,
            @RequestParam(defaultValue = "5") int size
    ) throws IOException, InterruptedException {
        List<Map<String, Object>> result = esDocumentService.fullTextSearch(indexName, queryText, "", size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-alldoc")
    public ResponseEntity<List<Map<String, Object>>> getAllDoc(
            @RequestParam String indexName,
            @RequestParam(defaultValue = "5") int size
    ) {
        List<Map<String, Object>> result = esDocumentService.getAllDocuments(indexName);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search-vector")
    public ResponseEntity<List<Map<String, Object>>> searchVector(
            @RequestParam String indexName,
            @RequestBody float[] vector,
            @RequestParam(defaultValue = "5") int size
    ) throws IOException, InterruptedException {
        List<Map<String, Object>> result = esDocumentService.vectorSearch(indexName, vector, size);
        return ResponseEntity.ok(result);
    }
    @DeleteMapping("/delete-document")
    public ResponseEntity<?> deleteDocument(
            @RequestParam String indexName,
            @RequestParam String docId
    ) {
        try {
            boolean deleted = esDocumentService.deleteDocumentById(indexName, docId);
            if (deleted) {
                return ResponseEntity.ok("文档删除成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文档未找到或删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-index")
    public ResponseEntity<?> deleteIndex(@RequestParam String indexName) {
        try {
            boolean deleted = esDocumentService.deleteIndex(indexName);
            if (deleted) {
                return ResponseEntity.ok("索引 [" + indexName + "] 删除成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("索引未找到或删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}