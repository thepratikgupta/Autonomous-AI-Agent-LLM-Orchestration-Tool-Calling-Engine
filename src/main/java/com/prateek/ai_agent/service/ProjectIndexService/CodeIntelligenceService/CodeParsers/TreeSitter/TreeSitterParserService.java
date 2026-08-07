//package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers.TreeSitter;
//
//import com.prateek.ai_agent.entity.Enums.LanguageType;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.treesitter.TSParser;
//import org.treesitter.TSTree;
//
//
//@Service
//@RequiredArgsConstructor
//public class TreeSitterParserService {
//
//    private final GrammarLoader loader;
//
//    public TSTree parse(LanguageType language, String code) {
//
//        TSParser parser = new TSParser();
//        parser.setLanguage(loader.get(language));
//        return parser.parse(code);
//    }
//}
