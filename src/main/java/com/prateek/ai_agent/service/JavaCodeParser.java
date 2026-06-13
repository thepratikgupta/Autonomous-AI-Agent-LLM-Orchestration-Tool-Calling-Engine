package com.prateek.ai_agent.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.prateek.ai_agent.dto.FileMetadata;
import com.prateek.ai_agent.entity.ClassMetadata;
import com.prateek.ai_agent.entity.LanguageType;
import com.prateek.ai_agent.entity.MethodMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JavaCodeParser implements CodeParser {

    @Override
    public boolean supports(LanguageType type) {
        return type == LanguageType.JAVA;
    }

    @Override
    public FileMetadata parse(
            String filePath,
            String content
    ) {

        CompilationUnit cu =
                StaticJavaParser.parse(content);

//        List<String> classes =
//                cu.findAll(ClassOrInterfaceDeclaration.class)
//                        .stream()
//                        .map(ClassOrInterfaceDeclaration::getNameAsString)
//                        .toList();
        List<ClassMetadata> classes =
                cu.findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .map(c -> ClassMetadata.builder()
                                .name(c.getNameAsString())
                                .packageName(
                                        cu.getPackageDeclaration()
                                                .map(p -> p.getNameAsString())
                                                .orElse("")
                                )
                                .superClass(
                                        c.getExtendedTypes().isEmpty()
                                                ? null
                                                : c.getExtendedTypes()
                                                .get(0)
                                                .getNameAsString()
                                )
                                .interfaces(
                                        c.getImplementedTypes()
                                                .stream()
                                                .map(i -> i.getNameAsString())
                                                .toList()
                                )
                                .lineNumber(
                                        c.getBegin()
                                                .map(p -> p.line)
                                                .orElse(-1)
                                )
                                .build())
                        .toList();

//        List<String> methods =
//                cu.findAll(MethodDeclaration.class)
//                        .stream()
//                        .map(MethodDeclaration::getNameAsString)
//                        .toList();
        List<MethodMetadata> methods =
                cu.findAll(MethodDeclaration.class)
                        .stream()
                        .map(method -> MethodMetadata.builder()
                                .name(method.getNameAsString())
                                .returnType(
                                        method.getType().asString()
                                )
                                .parameters(
                                        method.getParameters()
                                                .stream()
                                                .map(p ->
                                                        p.getTypeAsString()
                                                                + " "
                                                                + p.getNameAsString()
                                                )
                                                .toList()
                                )
                                .lineNumber(
                                        method.getBegin()
                                                .map(p -> p.line)
                                                .orElse(-1)
                                )
                                .isPublic(method.isPublic())
                                .isPrivate(method.isPrivate())
                                .isProtected(method.isProtected())
                                .isStatic(method.isStatic())
                                .build())
                        .toList();

        List<String> imports =
                cu.getImports()
                        .stream()
                        .map(i -> i.getNameAsString())
                        .toList();

        String packageName =
                cu.getPackageDeclaration()
                        .map(p -> p.getNameAsString())
                        .orElse(null);

        return FileMetadata.builder()
                .packageName(packageName)
                .classes(classes)
                .methods(methods)
                .imports(imports)
                .build();
    }
}
