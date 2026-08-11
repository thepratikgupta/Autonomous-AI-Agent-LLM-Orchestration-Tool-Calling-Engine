package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

public final class LuceneField {

    private LuceneField() {}

    public static final String ID = "id";
    public static final String PROJECT_ID = "projectId";
    public static final String USER_ID = "userId";
    public static final String CONVERSATION_ID = "conversationId";
    public static final String FILE_PATH = "filePath";
    public static final String FILE_NAME = "fileName";
    public static final String LANGUAGE = "language";
    public static final String PACKAGE_NAME = "packageName";

    public static final String CLASS_NAMES = "classNames";
    public static final String METHOD_NAMES = "methodNames";
    public static final String VARIABLE_NAMES = "variableNames";
    public static final String IMPORTS = "imports";
    public static final String METHOD_CALLS = "methodCalls";
    public static final String OBJECTS = "objects";
    public static final String CSS_SELECTORS = "cssSelectors";
    public static final String CSS_VARIABLES = "cssVariables";

    public static final String CONTENT = "content";

    public static final String CHECKSUM = "checksum";
    public static final String LAST_MODIFIED = "lastModified";
}
