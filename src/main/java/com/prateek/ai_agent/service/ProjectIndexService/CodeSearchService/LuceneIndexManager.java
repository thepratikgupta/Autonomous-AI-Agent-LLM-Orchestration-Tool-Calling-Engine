package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LuceneIndexManager {

    private final Path baseIndexPath;

    public LuceneIndexManager(@Value("${code-search.lucene.base-path:./data/lucene}") String basePath) {
        this.baseIndexPath = Path.of(basePath);
    }

    public Directory openIndex(String userId, String conversationId) throws IOException {

        Path indexPath = getIndexPath(userId, conversationId);
        Files.createDirectories(indexPath);
        
        System.out.println("==============================================");
        System.out.println("LUCENE INDEX PATH = " + indexPath.toAbsolutePath());
        System.out.println("USER ID = " + userId);
        System.out.println("CONVERSATION ID = " + conversationId);
        System.out.println("INDEX EXISTS = " + Files.exists(indexPath));
        System.out.println("==============================================");
        
        return FSDirectory.open(indexPath);
    }

    public Path getIndexPath(String userId, String conversationId) {
        validateId(userId, "userId");
        validateId(conversationId, "conversationId");
        return baseIndexPath.resolve(userId).resolve(conversationId);
    }

    private void validateId(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
        if (value.contains("/") || value.contains("\\") || value.contains("..")) {
            throw new IllegalArgumentException("Invalid " + field);
        }
    }
}
