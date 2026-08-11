package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers;

import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ImportMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ObjectMetadata;
import org.jsoup.parser.Parser;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HtmlCodeParser implements CodeParser {

    @Override
    public boolean supports(LanguageType type) {
        return type == LanguageType.HTML;
    }

    @Override
    public FileMetadata parse(String filePath, String content) {

        Parser parser = Parser.htmlParser();
        parser.setTrackPosition(true);

        Document document = Jsoup.parse(content, filePath, parser);

        List<ObjectMetadata> objects =
                document.getAllElements()
                        .stream()
                        .filter(element -> !"#root".equals(element.tagName()))
                        .map(this::toObjectMetadata)
                        .toList();

        List<ImportMetadata> imports = new ArrayList<>();

        document.select("script[src]")
                .forEach(script ->
                        imports.add(
                                ImportMetadata.builder()
                                        .name(script.attr("src"))
                                        .type("SCRIPT")
                                        .external(isExternal(script.attr("src")))
                                        .build()
                        )
                );
        document.select("link[href]")
                .forEach(link ->
                        imports.add(
                                ImportMetadata.builder()
                                        .name(link.attr("href"))
                                        .type("STYLESHEET")
                                        .external(isExternal(link.attr("href")))
                                        .build()
                        )
                );
        document.select("img[src]")
                .forEach(img ->
                        imports.add(
                                ImportMetadata.builder()
                                        .name(img.attr("src"))
                                        .type("IMAGE")
                                        .external(isExternal(img.attr("src")))
                                        .build()
                        )
                );
        document.select("video[src]")
                .forEach(video ->
                        imports.add(
                                ImportMetadata.builder()
                                        .name(video.attr("src"))
                                        .type("VIDEO")
                                        .external(isExternal(video.attr("src")))
                                        .build()
                        )
                );
        document.select("audio[src]")
                .forEach(audio ->
                        imports.add(
                                ImportMetadata.builder()
                                        .name(audio.attr("src"))
                                        .type("AUDIO")
                                        .external(isExternal(audio.attr("src")))
                                        .build()
                        )
                );
        document.select("source[src]")
                .forEach(source ->
                        imports.add(
                                ImportMetadata.builder()
                                        .name(source.attr("src"))
                                        .type("SOURCE")
                                        .external(isExternal(source.attr("src")))
                                        .build()
                        )
                );
        document.select("iframe[src]")
                .forEach(frame ->
                        imports.add(
                                ImportMetadata.builder()
                                        .name(frame.attr("src"))
                                        .type("IFRAME")
                                        .external(isExternal(frame.attr("src")))
                                        .build()
                        )
                );

        return FileMetadata.builder()
                .objects(objects)
                .imports(imports)
                .methods(List.of())
                .classes(List.of())
                .variables(List.of())
                .constructors(List.of())
                .records(List.of())
                .enums(List.of())
                .methodCalls(List.of())
                .lambdaExpressions(List.of())
                .build();
    }
    private boolean isExternal(String url) {

        return url.startsWith("http://")
                || url.startsWith("https://")
                || url.startsWith("//");

    }
    private ObjectMetadata toObjectMetadata(Element element) {

        int lineNumber = -1;

        if (element.sourceRange().isTracked()) {
            lineNumber = element.sourceRange()
                    .start()
                    .lineNumber();
        }

        return ObjectMetadata.builder()
                .name(
                        element.id().isBlank()
                                ? element.tagName()
                                : element.id()
                )
                .type(element.tagName())
                .text(element.ownText())
                .parent(
                        element.parent() != null
                                ? element.parent().tagName()
                                : null
                )
                .children(
                        element.children()
                                .stream()
                                .map(Element::tagName)
                                .toList()
                )
                .attributes(
                        element.attributes()
                                .asList()
                                .stream()
                                .map(attr -> attr.getKey() + "=" + attr.getValue())
                                .toList()
                )
                .lineNumber(lineNumber)
                .build();
    }

}
