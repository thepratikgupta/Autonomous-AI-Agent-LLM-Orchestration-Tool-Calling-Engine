package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class CodeSearchService {

    private final LuceneIndexManager luceneIndexManager;
    private final CodeSearchQueryParser queryParser;
    private final Analyzer analyzer = new StandardAnalyzer();

    public CodeSearchService(LuceneIndexManager luceneIndexManager, CodeSearchQueryParser queryParser) {
        this.luceneIndexManager = luceneIndexManager;
        this.queryParser = queryParser;
    }

    // existing methods...
    public List<CodeSearchResult> structuredSearch(
            String queryText, String userId, String conversationId, int maxResults
    ){
        validateSearchRequest(queryText, userId, conversationId, maxResults);
        CodeSearchQuery searchQuery = queryParser.parse(queryText);

        //DEBUGGING
        System.out.println("==============================================");
        System.out.println("CODE SEARCH REQUEST");
        System.out.println("queryText = " + queryText);
        System.out.println("userId = " + userId);
        System.out.println("conversationId = " + conversationId);
        System.out.println("parsed text = " + searchQuery.getText());
        System.out.println("parsed className = " + searchQuery.getClassName());
        System.out.println("==============================================");
        //DEBUGGING

        try(Directory directory = luceneIndexManager.openIndex(userId, conversationId)){

            if (!DirectoryReader.indexExists(directory)) return List.of();

            try(IndexReader reader = DirectoryReader.open(directory);
            ){

                System.out.println("LUCENE MAX DOCS = " + reader.maxDoc());
                System.out.println("LUCENE NUM DOCS = " + reader.numDocs());

                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = buildStructuredQuery(searchQuery);
                System.out.println("LUCENE QUERY = " + query);
                TopDocs topDocs = searcher.search(query, maxResults);

                //DEBUGGING
                System.out.println("LUCENE TOTAL HITS = " + topDocs.totalHits.value());
                System.out.println("LUCENE RETURNED HITS = " + topDocs.scoreDocs.length);
                //DEBUGGING

                List<CodeSearchResult> results = new ArrayList<>();

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

                    //Document document = searcher.doc(scoreDoc.doc);
                    Document document = reader.storedFields().document(scoreDoc.doc);
                    results.add(toSearchResult(document, scoreDoc.score));
                }
                return results;
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to execute structured code search", e);
        }
    }

    private Query buildStructuredQuery(CodeSearchQuery searchQuery){

        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        addFieldQuery(
                builder,
                LuceneField.CLASS_NAMES,
                searchQuery.getClassName()
        );

        addFieldQuery(
                builder,
                LuceneField.METHOD_NAMES,
                searchQuery.getMethodName()
        );

        addFieldQuery(
                builder,
                LuceneField.VARIABLE_NAMES,
                searchQuery.getVariableName()
        );

        addFieldQuery(
                builder,
                LuceneField.FILE_NAME,
                searchQuery.getFileName()
        );

        addFieldQuery(
                builder,
                LuceneField.FILE_PATH,
                searchQuery.getFilePath()
        );

        addFieldQuery(
                builder,
                LuceneField.LANGUAGE,
                searchQuery.getLanguage()
        );

        addFieldQuery(
                builder,
                LuceneField.PACKAGE_NAME,
                searchQuery.getPackageName()
        );

        addFieldQuery(
                builder,
                LuceneField.IMPORTS,
                searchQuery.getImportName()
        );

        addFieldQuery(
                builder,
                LuceneField.METHOD_CALLS,
                searchQuery.getMethodCall()
        );

        addFieldQuery(
                builder,
                LuceneField.OBJECTS,
                searchQuery.getObject()
        );

        addFieldQuery(
                builder,
                LuceneField.CSS_SELECTORS,
                searchQuery.getCssSelector()
        );

        addFieldQuery(
                builder,
                LuceneField.CSS_VARIABLES,
                searchQuery.getCssVariable()
        );

        if (searchQuery.getText() != null && !searchQuery.getText().isBlank()) {
            addTextSearch(builder, searchQuery.getText());
        }

        BooleanQuery query = builder.build();
        if (query.clauses().isEmpty()) {
            throw new IllegalArgumentException("Search query does not contain any searchable criteria");
        }
        return builder.build();
    }

    private void addFieldQuery(
            BooleanQuery.Builder builder,
            String field,
            String value
    ) {

        if (value == null || value.isBlank()) {
            return;
        }

        if (isExactField(field)) {

            builder.add(
                    new TermQuery(
                            new Term(
                                    field,
                                    value
                            )
                    ),
                    BooleanClause.Occur.MUST
            );

            return;
        }

        try {

            QueryParser parser =
                    new QueryParser(
                            field,
                            analyzer
                    );

            Query query =
                    parser.parse(
                            QueryParser.escape(value)
                    );

            builder.add(
                    query,
                    BooleanClause.Occur.MUST
            );

        }
        catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid search value for field "
                            + field,
                    e
            );
        }
    }
    private boolean isExactField(String field) {

        return field.equals(LuceneField.ID)
                || field.equals(LuceneField.PROJECT_ID)
                || field.equals(LuceneField.USER_ID)
                || field.equals(LuceneField.CONVERSATION_ID)
                || field.equals(LuceneField.FILE_PATH)
                || field.equals(LuceneField.FILE_NAME)
                || field.equals(LuceneField.LANGUAGE)
                || field.equals(LuceneField.PACKAGE_NAME)
                || field.equals(LuceneField.CHECKSUM)
                || field.equals(LuceneField.LAST_MODIFIED);
    }

    private void addTextSearch(
            BooleanQuery.Builder builder,
            String text
    ) {

        String[] fields = {

                LuceneField.FILE_PATH,
                LuceneField.FILE_NAME,
                LuceneField.PACKAGE_NAME,
                LuceneField.CLASS_NAMES,
                LuceneField.METHOD_NAMES,
                LuceneField.VARIABLE_NAMES,
                LuceneField.IMPORTS,
                LuceneField.METHOD_CALLS,
                LuceneField.OBJECTS,
                LuceneField.CSS_SELECTORS,
                LuceneField.CSS_VARIABLES,
                LuceneField.CONTENT
        };

        BooleanQuery.Builder textQuery =
                new BooleanQuery.Builder();

        for (String field : fields) {

            try {

                QueryParser parser =
                        new QueryParser(
                                field,
                                analyzer
                        );

                Query query =
                        parser.parse(
                                QueryParser.escape(text)
                        );

                textQuery.add(
                        query,
                        BooleanClause.Occur.SHOULD
                );

            }
            catch (Exception e) {

                throw new IllegalArgumentException(
                        "Invalid text search",
                        e
                );
            }
        }
        BooleanQuery builtTextQuery = textQuery.build();

        if (builtTextQuery.clauses().isEmpty()) {
            throw new IllegalArgumentException(
                    "Text search produced no searchable fields"
            );
        }

        builder.add(
                textQuery.build(),
                BooleanClause.Occur.MUST
        );
    }
    private void validateSearchRequest(
            String queryText,
            String userId,
            String conversationId,
            int maxResults)
    {
        if (queryText == null || queryText.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query cannot be blank"
            );
        }
        if (queryText.length() > 1000) {
            throw new IllegalArgumentException(
                    "Search query cannot exceed 1000 characters"
            );
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "userId cannot be blank"
            );
        }
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException(
                    "conversationId cannot be blank"
            );
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException(
                    "maxResults must be greater than 0"
            );
        }
        if (maxResults > 100) {
            throw new IllegalArgumentException(
                    "maxResults cannot exceed 100"
            );
        }
    }

        private CodeSearchResult toSearchResult(Document document, float score){

            return CodeSearchResult.builder()
                    .id(document.get(LuceneField.ID))
                    .projectId(document.get(LuceneField.PROJECT_ID))
                    .userId(document.get(LuceneField.USER_ID))
                    .conversationId(document.get(LuceneField.CONVERSATION_ID))
                    .filePath(document.get(LuceneField.FILE_PATH))
                    .fileName(document.get(LuceneField.FILE_NAME))
                    .language(document.get(LuceneField.LANGUAGE))
                    .packageName(document.get(LuceneField.PACKAGE_NAME))
                    .checksum(document.get(LuceneField.CHECKSUM))
                    .lastModified(parseLastModified(
                            document.get(LuceneField.LAST_MODIFIED)
                    ))
                    .score(score)
                    .build();
        }
    private Long parseLastModified(String value) {
        if (value == null || value.isBlank()) {return null;}
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            log.warn("Invalid lastModified value in Lucene document: {}", value);
            return null;
        }
    }


}
