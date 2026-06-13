//package com.prateek.ai_agent.service;
//
//import com.prateek.ai_agent.dto.FileMetadata;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//@RequiredArgsConstructor
//public class ProjectIndexerService {
//
//    public FileMetadata extractMetadata(String content) {
//
//        List<String> classes = new ArrayList<>();
//        List<String> methods = new ArrayList<>();
//        List<String> imports = new ArrayList<>();
//
//        Pattern classPattern =
//                Pattern.compile("class\\s+(\\w+)");
//
//        Pattern importPattern =
//                Pattern.compile("import\\s+([\\w\\.]+)");
//
//        Pattern methodPattern =
//                Pattern.compile(
//                        "(public|private|protected).*\\s+(\\w+)\\("
//                );
//
//        Matcher classMatcher =
//                classPattern.matcher(content);
//
//        while (classMatcher.find()) {
//            classes.add(classMatcher.group(1));
//        }
//
//        Matcher importMatcher =
//                importPattern.matcher(content);
//
//        while (importMatcher.find()) {
//            imports.add(importMatcher.group(1));
//        }
//
//        Matcher methodMatcher =
//                methodPattern.matcher(content);
//
//        while (methodMatcher.find()) {
//            methods.add(methodMatcher.group(2));
//        }
//
//        return FileMetadata.builder()
//                .classes(classes)
//                .methods(methods)
//                .imports(imports)
//                .build();
//    }
//}
