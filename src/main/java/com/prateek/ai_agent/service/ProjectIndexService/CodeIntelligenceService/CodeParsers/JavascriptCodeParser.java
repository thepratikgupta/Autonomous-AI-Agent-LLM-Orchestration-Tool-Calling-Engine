package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers;

import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.ast.AstRoot;
import org.springframework.stereotype.Service;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class JavascriptCodeParser implements CodeParser {

    private final Parser parser = createParser();

    @Override
    public boolean supports(LanguageType type) {
        return type == LanguageType.JAVASCRIPT;
    }

    @Override
    public FileMetadata parse(String filePath, String content) {
        try {
            log.info(
                    "Parsing JS file: {} | content length: {}",
                    filePath,
                    content == null ? 0 : content.length()
            );

            Parser parser = createParser();
            AstRoot root = parser.parse(content, filePath, 1);

            //JavaScriptAstVisitor visitor = new JavaScriptAstVisitor();
            JavaScriptAstVisitor visitor = new JavaScriptAstVisitor(content);
            root.visit(visitor);


            log.info(
                    "JS parsed successfully: {} | methods={} variables={} imports={} objects={} methodCalls={}",
                    filePath,
                    visitor.getMethods().size(),
                    visitor.getVariables().size(),
                    visitor.getImports().size(),
                    visitor.getObjects().size(),
                    visitor.getMethodCalls().size()
            );

            return FileMetadata.builder()
                    .methods(visitor.getMethods())
                    .variables(visitor.getVariables())
                    .imports(visitor.getImports())
                    .objects(visitor.getObjects())
                    .methodCalls(visitor.getMethodCalls())
                    .build();
        }
        catch(Exception ex){
            log.warn(
                    "Failed to parse JS file {}",
                    filePath,
                    ex
            );
            return FileMetadata.builder().build();
        }
    }

    //HELPER METHODS:
    private Parser createParser() {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setLanguageVersion(org.mozilla.javascript.Context.VERSION_ES6);
        env.setRecoverFromErrors(true);
        env.setRecordingComments(true);
        env.setRecordingLocalJsDocComments(true);

        ErrorReporter reporter = new ErrorReporter() {

            @Override
            public void warning(
                    String message,
                    String sourceName,
                    int line,
                    String lineSource,
                    int lineOffset
            ) {
            }
            @Override
            public void error(
                    String message,
                    String sourceName,
                    int line,
                    String lineSource,
                    int lineOffset
            ) {
            }
            @Override
            public EvaluatorException runtimeError(
                    String message,
                    String sourceName,
                    int line,
                    String lineSource,
                    int lineOffset
            ) {
                return new EvaluatorException(message);
            }
        };
        return new Parser(env, reporter);
    }
}