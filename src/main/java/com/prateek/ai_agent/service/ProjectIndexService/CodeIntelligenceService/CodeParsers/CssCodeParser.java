package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers;

import com.helger.css.decl.*;
import com.helger.css.reader.CSSReader;
import com.helger.css.reader.CSSReaderSettings;
import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.CssSelectorMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ImportMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CssCodeParser implements CodeParser {

    @Override
    public boolean supports(LanguageType type) {
        return type == LanguageType.CSS;
    }

    @Override
    public FileMetadata parse(String filePath, String content) {

        try {

            CSSReaderSettings settings = new CSSReaderSettings()
                    .setUseSourceLocation(true);

            CascadingStyleSheet css =
                    CSSReader.readFromStringReader(
                            content,
                            settings
                    );

            if(css == null) return FileMetadata.builder().build();

            List<CssSelectorMetadata> selectors = new ArrayList<>();
            List<ImportMetadata> imports = new ArrayList<>();
            List<String> mediaQueries = new ArrayList<>();
            List<String> keyFrames = new ArrayList<>();
            List<String> cssVariables = new ArrayList<>();
            List<String> fontFaces = new ArrayList<>();

            for (ICSSTopLevelRule rule : css.getAllRules()) {
                processRule(
                        rule,
                        selectors,
                        imports,
                        mediaQueries,
                        keyFrames,
                        cssVariables,
                        fontFaces
                );
            }

            return FileMetadata.builder()
                    .cssSelectors(selectors)
                    .imports(imports)
                    .mediaQueries(mediaQueries)
                    .keyFrames(keyFrames)
                    .cssVariables(cssVariables)
                    .fontFaces(fontFaces)
                    .build();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return FileMetadata.builder().build();
        }
    }



    private void processStyleRule(
            CSSStyleRule styleRule, List<CssSelectorMetadata> selectors, List<String> cssVariables) {

        int lineNumber = getLineNumber(styleRule);
        for (CSSSelector selector : styleRule.getAllSelectors()) {
            String selectorText = selector.getAsCSSString();
            selectors.add(
                    CssSelectorMetadata.builder()
                            .selector(selectorText)
                            .type(detectSelectorType(selectorText))
                            .declarations(
                                    styleRule.getAllDeclarations()
                                            .stream()
                                            .map(d ->
                                                    d.getProperty()
                                                            + ": "
                                                            + d.getExpression().getAsCSSString())
                                            .toList()
                            )
                            .lineNumber(lineNumber)
                            .build()
            );
        }

        for (CSSDeclaration declaration : styleRule.getAllDeclarations()) {
            if (declaration.getProperty().startsWith("--")) {
                cssVariables.add(declaration.getProperty());
            }
        }
    }

    private int getLineNumber(CSSStyleRule styleRule) {

        if (styleRule.getSourceLocation() == null) {
            return -1;
        }
        return styleRule.getSourceLocation().getFirstTokenBeginLineNumber();
    }

    private boolean isExternal(String path) {
        if (path == null) return false;
        return path.startsWith("http://")
                || path.startsWith("https://")
                || path.startsWith("//");
    }

    private String detectSelectorType(String selector) {

        selector = selector.trim();

        if (selector.startsWith(".")) {
            return "CLASS";
        }

        if (selector.startsWith("#")) {
            return "ID";
        }

        if (selector.startsWith("*")) {
            return "UNIVERSAL";
        }

        if (selector.contains(":")) {
            return "PSEUDO";
        }

        if (selector.contains("[")) {
            return "ATTRIBUTE";
        }

        if (selector.contains(">") ||
                selector.contains("+") ||
                selector.contains("~")) {
            return "COMBINATOR";
        }

        return "ELEMENT";
    }
    private void processRule(
            ICSSTopLevelRule rule,
            List<CssSelectorMetadata> selectors,
            List<ImportMetadata> imports,
            List<String> mediaQueries,
            List<String> keyFrames,
            List<String> cssVariables,
            List<String> fontFaces
    ) {

        if (rule instanceof CSSImportRule importRule) {

            String importPath = importRule.getLocationString();

            imports.add(
                    ImportMetadata.builder()
                            .name(importPath)
                            .type("CSS_IMPORT")
                            .external(isExternal(importPath))
                            .build()
            );

            return;
        }

        if (rule instanceof CSSMediaRule mediaRule) {

            mediaQueries.add(
                    mediaRule.getAllMediaQueries()
                            .stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "))
            );

            // recurse into nested rules
            for (ICSSTopLevelRule nested : mediaRule.getAllRules()) {
                processRule(
                        nested,
                        selectors,
                        imports,
                        mediaQueries,
                        keyFrames,
                        cssVariables,
                        fontFaces
                );
            }
            return;
        }

        if (rule instanceof CSSKeyframesRule keyframesRule) {
            keyFrames.add(keyframesRule.getAnimationName());
            return;
        }

        if (rule instanceof CSSFontFaceRule fontFaceRule) {
            fontFaces.add("font-face");
            return;
        }

        if (rule instanceof CSSStyleRule styleRule) {
            processStyleRule(
                    styleRule,
                    selectors,
                    cssVariables
            );
        }
    }
}
