package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class LuceneIndexService {

    private final Analyzer analyzer;
    private final LuceneIndexManager luceneIndexManager;

    public LuceneIndexService(
            LuceneIndexManager luceneIndexManager
    ) {
        this.luceneIndexManager = luceneIndexManager;
        this.analyzer = new StandardAnalyzer();
    }

    /**
     * Index one ProjectIndex document inside the
     * Lucene index belonging to userId + conversationId.
     */
    public void index(ProjectIndex projectIndex) {

        if (projectIndex == null) {
            return;
        }

        if (projectIndex.getId() == null) {
            throw new IllegalArgumentException(
                    "ProjectIndex id cannot be null"
            );
        }

        if (projectIndex.getUserId() == null
                || projectIndex.getConversationId() == null) {

            throw new IllegalArgumentException(
                    "userId and conversationId are required"
            );
        }

        try (
                Directory directory =
                        luceneIndexManager.openIndex(
                                projectIndex.getUserId(),
                                projectIndex.getConversationId()
                        )
        ) {

            Document document =
                    CodeSearchDocumentFactory.from(projectIndex);

            IndexWriterConfig config =
                    new IndexWriterConfig(analyzer);

            try (IndexWriter writer =
                         new IndexWriter(directory, config)) {

                writer.updateDocument(
                        new Term(
                                LuceneField.ID,
                                projectIndex.getId()
                        ),
                        document
                );

                writer.commit();
            }

            log.debug(
                    "Indexed file into Lucene: userId={}, conversationId={}, file={}",
                    projectIndex.getUserId(),
                    projectIndex.getConversationId(),
                    projectIndex.getFilePath()
            );

        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to index file: "
                            + projectIndex.getFilePath(),
                    e
            );
        }
    }

    /**
     * Delete one document from the Lucene index
     * belonging to userId + conversationId.
     */
    public void delete(
            String projectIndexId,
            String userId,
            String conversationId
    ) {

        if (projectIndexId == null || projectIndexId.isBlank()) {
            return;
        }

        try (
                Directory directory =
                        luceneIndexManager.openIndex(
                                userId,
                                conversationId
                        )
        ) {

            IndexWriterConfig config =
                    new IndexWriterConfig(analyzer);

            try (IndexWriter writer =
                         new IndexWriter(directory, config)) {

                writer.deleteDocuments(
                        new Term(
                                LuceneField.ID,
                                projectIndexId
                        )
                );

                writer.commit();
            }

            log.debug(
                    "Removed Lucene document: userId={}, conversationId={}, id={}",
                    userId,
                    conversationId,
                    projectIndexId
            );

        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to delete Lucene document: "
                            + projectIndexId, e
            );
        }
    }

    /**
     * Count documents for one user + conversation.
     */
    public long count(String userId, String conversationId) {

        try(Directory directory = luceneIndexManager.openIndex(userId, conversationId)
        ){
            if (!DirectoryReader.indexExists(directory)) return 0;
            try (DirectoryReader reader = DirectoryReader.open(directory)) {
                return reader.numDocs();
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to read Lucene index", e);
        }
    }
}

//package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;
//
//import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectIndex;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.FSDirectory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//@Service
//@Slf4j
//public class LuceneIndexService {
//
//    private final Path indexPath;
//    private final Analyzer analyzer;
//    private Directory directory;
//    private final LuceneIndexManager luceneIndexManager;
//
//    public LuceneIndexService(
//            @Value("${code-search.lucene.index-path:./data/lucene}") String indexPath, LuceneIndexManager luceneIndexManager
//    ) {
//        this.indexPath = Path.of(indexPath);
//        this.luceneIndexManager = luceneIndexManager;
//        this.analyzer = new StandardAnalyzer();
//    }
//
//    @PostConstruct
//    public void initialize() {
//
//        try {
//
//            Files.createDirectories(indexPath);
//
//            directory = FSDirectory.open(indexPath);
//
//            log.info(
//                    "Lucene code index initialized at {}",
//                    indexPath.toAbsolutePath()
//            );
//
//        }
//        catch (IOException e) {
//
//            throw new IllegalStateException(
//                    "Failed to initialize Lucene index at "
//                            + indexPath.toAbsolutePath(),
//                    e
//            );
//        }
//    }
//
//    public void index(ProjectIndex projectIndex) {
//
//        if (projectIndex == null) {
//            return;
//        }
//
//        if (projectIndex.getId() == null) {
//            throw new IllegalArgumentException(
//                    "ProjectIndex id cannot be null"
//            );
//        }
//
//        Document document = CodeSearchDocumentFactory.from(projectIndex);
//
//        IndexWriterConfig config = new IndexWriterConfig(analyzer);
//
//        try (IndexWriter writer = new IndexWriter(directory, config)) {
//
//            writer.updateDocument(
//                    new Term(
//                            LuceneField.ID,
//                            projectIndex.getId()
//                    ),
//                    document
//            );
//
//            writer.commit();
//
//            log.debug(
//                    "Indexed file into Lucene: {}",
//                    projectIndex.getFilePath()
//            );
//
//        }
//        catch (IOException e) {
//
//            throw new IllegalStateException(
//                    "Failed to index file: "
//                            + projectIndex.getFilePath(),
//                    e
//            );
//        }
//    }
//
//    public void delete(String projectIndexId) {
//
//        if (projectIndexId == null || projectIndexId.isBlank()) {
//            return;
//        }
//
//        IndexWriterConfig config =
//                new IndexWriterConfig(analyzer);
//
//        try (IndexWriter writer =
//                     new IndexWriter(directory, config)) {
//
//            writer.deleteDocuments(
//                    new Term(
//                            LuceneField.ID,
//                            projectIndexId
//                    )
//            );
//
//            writer.commit();
//
//            log.debug(
//                    "Removed Lucene document: {}",
//                    projectIndexId
//            );
//
//        }
//        catch (IOException e) {
//
//            throw new IllegalStateException(
//                    "Failed to delete Lucene document: "
//                            + projectIndexId,
//                    e
//            );
//        }
//    }
//
//    public long count() {
//
//        try {
//
//            if (!DirectoryReader.indexExists(directory)) {
//                return 0;
//            }
//
//            try (DirectoryReader reader =
//                         DirectoryReader.open(directory)) {
//
//                return reader.numDocs();
//            }
//
//        }
//        catch (IOException e) {
//
//            throw new IllegalStateException(
//                    "Failed to read Lucene index",
//                    e
//            );
//        }
//    }
//}