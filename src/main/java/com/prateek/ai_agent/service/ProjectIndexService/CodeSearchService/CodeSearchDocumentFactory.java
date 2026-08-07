package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ClassMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.CssSelectorMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.MethodMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.MethodCallMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectIndex;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.VariableMetadata;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CodeSearchDocumentFactory {

    private CodeSearchDocumentFactory() {
    }

    public static Document from(ProjectIndex index) {

        Document document = new Document();

        addStringField(
                document,
                LuceneField.ID,
                index.getId()
        );

        addStringField(
                document,
                LuceneField.PROJECT_ID,
                index.getProjectId()
        );

        addStringField(
                document,
                LuceneField.USER_ID,
                index.getUserId()
        );

        addStringField(
                document,
                LuceneField.CONVERSATION_ID,
                index.getConversationId()
        );

        addStringField(
                document,
                LuceneField.FILE_PATH,
                index.getFilePath()
        );

        addStringField(
                document,
                LuceneField.FILE_NAME,
                index.getFileName()
        );

        addStringField(
                document,
                LuceneField.LANGUAGE,
                index.getLanguage() == null
                        ? null
                        : index.getLanguage().name()
        );
        addStringField(
                document,
                LuceneField.PACKAGE_NAME,
                index.getPackageName()
        );

        addTextField(
                document,
                LuceneField.CLASS_NAMES,
                extractClassNames(index.getClasses())
        );

        addTextField(
                document,
                LuceneField.METHOD_NAMES,
                extractMethodNames(index.getMethods())
        );

        addTextField(
                document,
                LuceneField.VARIABLE_NAMES,
                extractVariableNames(index.getVariables())
        );

        addTextField(
                document,
                LuceneField.IMPORTS,
                extractImports(index)
        );

        addTextField(
                document,
                LuceneField.METHOD_CALLS,
                extractMethodCalls(index.getMethodCalls())
        );

        addTextField(
                document,
                LuceneField.OBJECTS,
                extractObjects(index)
        );

        addTextField(
                document,
                LuceneField.CSS_SELECTORS,
                extractCssSelectors(index.getCssSelectors())
        );

        addTextField(
                document,
                LuceneField.CSS_VARIABLES,
                join(index.getCssVariables())
        );

        addTextField(
                document,
                LuceneField.CONTENT,
                buildSearchableContent(index)
        );

        addStringField(
                document,
                LuceneField.CHECKSUM,
                index.getChecksum()
        );

        if (index.getLastModified() > 0) {
            document.add(
                    new StringField(
                            LuceneField.LAST_MODIFIED,
                            String.valueOf(index.getLastModified()),
                            Field.Store.YES
                    )
            );
        }
        return document;
    }

    private static void addStringField(
            Document document,
            String field,
            String value
    ) {
        if (value == null || value.isBlank()) {
            return;
        }

        document.add(
                new StringField(
                        field,
                        value,
                        Field.Store.YES
                )
        );
    }

    private static void addTextField(
            Document document,
            String field,
            String value
    ) {
        if (value == null || value.isBlank()) {
            return;
        }

        document.add(
                new TextField(
                        field,
                        value,
                        Field.Store.YES
                )
        );
    }

    private static String extractClassNames(
            List<ClassMetadata> classes
    ) {
        if (classes == null) {
            return "";
        }

        return classes.stream()
                .filter(Objects::nonNull)
                .map(ClassMetadata::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String extractMethodNames(
            List<MethodMetadata> methods
    ) {
        if (methods == null) {
            return "";
        }

        return methods.stream()
                .filter(Objects::nonNull)
                .map(MethodMetadata::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String extractVariableNames(
            List<VariableMetadata> variables
    ) {
        if (variables == null) {
            return "";
        }

        return variables.stream()
                .filter(Objects::nonNull)
                .map(VariableMetadata::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String extractImports(
            ProjectIndex index
    ) {
        if (index.getImports() == null) {
            return "";
        }

        return index.getImports()
                .stream()
                .filter(Objects::nonNull)
                .map(importMetadata ->
                        importMetadata.getName()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String extractMethodCalls(
            List<MethodCallMetadata> methodCalls
    ) {
        if (methodCalls == null) {
            return "";
        }

        return methodCalls.stream()
                .filter(Objects::nonNull)
                .map(call -> {

                    String owner = call.getOwner();
                    String method = call.getMethodName();

                    if (owner == null || owner.isBlank()) {
                        return method;
                    }

                    return owner + "." + method;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String extractObjects(
            ProjectIndex index
    ) {
        if (index.getObjects() == null) {
            return "";
        }

        return index.getObjects()
                .stream()
                .filter(Objects::nonNull)
                .map(object -> {

                    String name = object.getName();
                    String type = object.getType();

                    if (name == null) {
                        return type;
                    }

                    if (type == null) {
                        return name;
                    }

                    return name + " " + type;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String extractCssSelectors(
            List<CssSelectorMetadata> selectors
    ) {
        if (selectors == null) {
            return "";
        }

        return selectors.stream()
                .filter(Objects::nonNull)
                .map(CssSelectorMetadata::getSelector)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String join(List<String> values) {

        if (values == null) {
            return "";
        }

        return values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    private static String buildSearchableContent(
            ProjectIndex index
    ) {

        StringBuilder content = new StringBuilder();

        append(content, index.getFilePath());
        append(content, index.getFileName());
        append(content, index.getPackageName());
        append(content, index.getSummary());

        append(
                content,
                extractClassNames(index.getClasses())
        );

        append(
                content,
                extractMethodNames(index.getMethods())
        );

        append(
                content,
                extractVariableNames(index.getVariables())
        );

        append(
                content,
                extractImports(index)
        );

        append(
                content,
                extractMethodCalls(index.getMethodCalls())
        );

        append(
                content,
                extractObjects(index)
        );

        append(
                content,
                extractCssSelectors(index.getCssSelectors())
        );

        append(
                content,
                join(index.getCssVariables())
        );

        append(
                content,
                join(index.getMediaQueries())
        );

        append(
                content,
                join(index.getKeyFrames())
        );

        append(
                content,
                join(index.getFontFaces())
        );

        return content.toString();
    }

    private static void append(
            StringBuilder builder,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            builder.append(value).append(' ');
        }
    }
}
