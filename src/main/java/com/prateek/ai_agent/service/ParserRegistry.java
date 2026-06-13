package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.LanguageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParserRegistry {

    private final List<CodeParser> parsers;

    public CodeParser getParser(
            LanguageType type
    ) {

        return parsers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow();
    }
}
