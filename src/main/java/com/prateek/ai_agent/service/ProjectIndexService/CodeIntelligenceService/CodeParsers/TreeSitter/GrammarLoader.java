//package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers.TreeSitter;
//
//import com.prateek.ai_agent.entity.Enums.LanguageType;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.treesitter.TSLanguage;
//
//import java.util.EnumMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class GrammarLoader {
//
//    private final Map<LanguageType, TSLanguage> grammars = new EnumMap<>(LanguageType.class);
//
//    @PostConstruct
//    public void init() {
//
//        grammars.put(LanguageType.JAVA, load("java"));
//
//        grammars.put(LanguageType.JAVASCRIPT, load("javascript"));
//
//        grammars.put(LanguageType.PYTHON, load("python"));
//
//        grammars.put(LanguageType.HTML, load("html"));
//
//        grammars.put(LanguageType.CSS, load("css"));
//    }
//
//    public TSLanguage get(LanguageType language) {
//        return grammars.get(language);
//    }
//
//    private TSLanguage load(String grammarName) {
//        ...
//
//    }
//}
