package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CodeSearchQueryParser {

    private static final Pattern FIELD_PATTERN =
            Pattern.compile("(\\w+)\\s*:\\s*(\"[^\"]*\"|'[^']*'|\\S+)");

    public CodeSearchQuery parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query cannot be blank"
            );
        }

        CodeSearchQuery.CodeSearchQueryBuilder builder = CodeSearchQuery.builder();

        Matcher matcher = FIELD_PATTERN.matcher(input);

        StringBuilder remainingText = new StringBuilder();

        int lastEnd = 0;

        while (matcher.find()) {

            remainingText.append(input,lastEnd, matcher.start());

            String field = matcher.group(1);

            String value = cleanValue(matcher.group(2));

            applyField(builder, field, value);

            lastEnd = matcher.end();
        }

        remainingText.append(input, lastEnd, input.length());

        String text = remainingText.toString().trim();

        if (!text.isBlank()) {
            builder.text(text);
        }
        return builder.build();
    }

    private void applyField(
            CodeSearchQuery.CodeSearchQueryBuilder builder,
            String field,
            String value
    ) {

        switch (field.toLowerCase()) {

            case "class":
            case "classname":
                builder.className(value);
                break;

            case "method":
            case "methodname":
                builder.methodName(value);
                break;

            case "variable":
            case "variablename":
                builder.variableName(value);
                break;

            case "file":
            case "filename":
                builder.fileName(value);
                break;

            case "path":
            case "filepath":
                builder.filePath(value);
                break;

            case "language":
                builder.language(value);
                break;

            case "package":
                builder.packageName(value);
                break;

            case "import":
                builder.importName(value);
                break;

            case "call":
            case "methodcall":
                builder.methodCall(value);
                break;

            case "object":
                builder.object(value);
                break;

            case "css":
            case "selector":
                builder.cssSelector(value);
                break;

            case "cssvariable":
                builder.cssVariable(value);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown search field: " + field
                );
        }
    }

    private String cleanValue(String value) {

        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.length() >= 2) {

            char first = value.charAt(0);
            char last = value.charAt(
                    value.length() - 1
            );

            if ((first == '"' && last == '"')
                    || (first == '\'' && last == '\'')) {

                return value.substring(
                        1,
                        value.length() - 1
                );
            }
        }

        return value;
    }
}
