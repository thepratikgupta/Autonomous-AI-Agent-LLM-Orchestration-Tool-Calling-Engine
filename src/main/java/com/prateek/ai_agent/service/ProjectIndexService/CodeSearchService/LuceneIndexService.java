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
