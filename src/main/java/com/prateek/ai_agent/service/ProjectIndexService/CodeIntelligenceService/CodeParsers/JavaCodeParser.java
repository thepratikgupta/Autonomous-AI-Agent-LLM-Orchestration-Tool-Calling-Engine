package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.*;
import com.prateek.ai_agent.entity.Enums.LanguageType;
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
    public FileMetadata parse(String filePath, String content) {
        System.out.println("JAVA CODE PARSER STARTED");

        CompilationUnit cu = StaticJavaParser.parse(content);

        List<ClassMetadata> classes = cu.findAll(ClassOrInterfaceDeclaration.class)
                        .stream()
                        .map(c -> {
                            String packageName =
                                    cu.getPackageDeclaration()
                                            .map(p -> p.getNameAsString())
                                            .orElse("");

                            return ClassMetadata.builder()
                                    .name(c.getNameAsString())
                                    .qualifiedName(
                                            packageName.isEmpty()
                                                    ? c.getNameAsString()
                                                    : packageName + "." + c.getNameAsString()
                                    )
                                    .packageName(packageName)
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
                                    .modifiers(
                                            c.getModifiers()
                                                    .stream()
                                                    .map(m -> m.getKeyword().asString())
                                                    .toList()
                                    )
                                    .annotations(
                                            c.getAnnotations()
                                                    .stream()
                                                    .map(a -> a.getNameAsString())
                                                    .toList()
                                    )
                                    .documentation(
                                            c.getJavadoc()
                                                    .map(j -> j.getDescription().toText())
                                                    .orElse(null)
                                    )
                                    .startLine(
                                            c.getBegin()
                                                    .map(p -> p.line)
                                                    .orElse(-1)
                                    )
                                    .endLine(
                                            c.getEnd()
                                                    .map(p -> p.line)
                                                    .orElse(-1)
                                    )
                                    .build();
                        })
                        .toList();

        List<VariableMetadata> variables =
                cu.findAll(VariableDeclarator.class)
                        .stream()
                        .map(v -> {

                            boolean field =
                                    v.findAncestor(FieldDeclaration.class).isPresent();

                            String ownerClass =
                                    v.findAncestor(ClassOrInterfaceDeclaration.class)
                                            .map(ClassOrInterfaceDeclaration::getNameAsString)
                                            .orElse(null);

                            String ownerMethod =
                                    v.findAncestor(MethodDeclaration.class)
                                            .map(MethodDeclaration::getNameAsString)
                                            .orElse(null);

                            List<String> modifiers =
                                    v.findAncestor(FieldDeclaration.class)
                                            .map(f ->
                                                    f.getModifiers()
                                                            .stream()
                                                            .map(m -> m.getKeyword().asString())
                                                            .toList()
                                            )
                                            .orElse(List.of());

                            List<String> annotations =
                                    v.findAncestor(FieldDeclaration.class)
                                            .map(f ->
                                                    f.getAnnotations()
                                                            .stream()
                                                            .map(a -> a.getNameAsString())
                                                            .toList()
                                            )
                                            .orElse(List.of());

                            return VariableMetadata.builder()
                                    .name(v.getNameAsString())
                                    .type(v.getTypeAsString())
                                    .initializer(
                                            v.getInitializer()
                                                    .map(Object::toString)
                                                    .orElse(null)
                                    )
                                    .ownerClass(ownerClass)
                                    .ownerMethod(ownerMethod)
                                    .field(field)
                                    .lineNumber(
                                            v.getBegin()
                                                    .map(p -> p.line)
                                                    .orElse(-1)
                                    )
                                    .modifiers(modifiers)
                                    .annotations(annotations)
                                    .build();
                        })
                        .toList();

        List<MethodMetadata> methods =
                cu.findAll(MethodDeclaration.class)
                        .stream()
                        .map(method -> {

                            String ownerClass =
                                    method.findAncestor(ClassOrInterfaceDeclaration.class)
                                            .map(ClassOrInterfaceDeclaration::getNameAsString)
                                            .orElse(null);

                            String packageName =
                                    cu.getPackageDeclaration()
                                            .map(p -> p.getNameAsString())
                                            .orElse("");

                            String qualifiedName =
                                    packageName.isEmpty()
                                            ? ownerClass + "." + method.getNameAsString()
                                            : packageName + "." + ownerClass + "." + method.getNameAsString();

                            String signature =
                                    method.getNameAsString()
                                            + "("
                                            + method.getParameters()
                                            .stream()
                                            .map(p -> p.getType().asString())
                                            .reduce((a, b) -> a + "," + b)
                                            .orElse("")
                                            + ")";

                            return MethodMetadata.builder()
                                    .name(method.getNameAsString())
                                    .signature(signature)
                                    .qualifiedName(qualifiedName)
                                    .ownerClass(ownerClass)
                                    .returnType(method.getType().asString())
                                    .parameters(
                                            method.getParameters()
                                                    .stream()
                                                    .map(p ->
                                                            p.getTypeAsString()
                                                                    + " "
                                                                    + p.getNameAsString())
                                                    .toList()
                                    )
                                    .modifiers(
                                            method.getModifiers()
                                                    .stream()
                                                    .map(m -> m.getKeyword().asString())
                                                    .toList()
                                    )
                                    .annotations(
                                            method.getAnnotations()
                                                    .stream()
                                                    .map(a -> a.getNameAsString())
                                                    .toList()
                                    )
                                    .thrownExceptions(
                                            method.getThrownExceptions()
                                                    .stream()
                                                    .map(Object::toString)
                                                    .toList()
                                    )
                                    .documentation(
                                            method.getJavadoc()
                                                    .map(j -> j.getDescription().toText())
                                                    .orElse(null)
                                    )
                                    .startLine(
                                            method.getBegin()
                                                    .map(p -> p.line)
                                                    .orElse(-1)
                                    )
                                    .endLine(
                                            method.getEnd()
                                                    .map(p -> p.line)
                                                    .orElse(-1)
                                    )
                                    .build();
                        })
                        .toList();

        List<ConstructorMetadata> constructors =
                cu.findAll(ConstructorDeclaration.class)
                        .stream()
                        .map(c ->
                                ConstructorMetadata.builder()
                                        .name(c.getNameAsString())
                                        .parameters(
                                                c.getParameters()
                                                        .stream()
                                                        .map(p ->
                                                                p.getTypeAsString()
                                                                        + " "
                                                                        + p.getNameAsString())
                                                        .toList()
                                        )
                                        .modifiers(
                                                c.getModifiers()
                                                        .stream()
                                                        .map(m -> m.getKeyword().asString())
                                                        .toList()
                                        )
                                        .annotations(
                                                c.getAnnotations()
                                                        .stream()
                                                        .map(a -> a.getNameAsString())
                                                        .toList()
                                        )
                                        .lineNumber(
                                                c.getBegin()
                                                        .map(p -> p.line)
                                                        .orElse(-1)
                                        )
                                        .build()
                        )
                        .toList();

        List<EnumMetadata> enums =
                cu.findAll(EnumDeclaration.class)
                        .stream()
                        .map(e ->
                                EnumMetadata.builder()
                                        .name(e.getNameAsString())
                                        .values(
                                                e.getEntries()
                                                        .stream()
                                                        .map(entry -> entry.getNameAsString())
                                                        .toList()
                                        )
                                        .lineNumber(
                                                e.getBegin()
                                                        .map(p -> p.line)
                                                        .orElse(-1)
                                        )
                                        .build()
                        )
                        .toList();
        List<RecordMetadata> records =
                cu.findAll(RecordDeclaration.class)
                        .stream()
                        .map(r ->
                                RecordMetadata.builder()
                                        .name(r.getNameAsString())
                                        .components(
                                                r.getParameters()
                                                        .stream()
                                                        .map(p ->
                                                                p.getTypeAsString()
                                                                        + " "
                                                                        + p.getNameAsString())
                                                        .toList()
                                        )
                                        .lineNumber(
                                                r.getBegin()
                                                        .map(p -> p.line)
                                                        .orElse(-1)
                                        )
                                        .build()
                        )
                        .toList();

        List<ImportMetadata> imports =
                cu.getImports()
                        .stream()
                        .map(i ->
                                ImportMetadata.builder()
                                        .name(i.getNameAsString())
                                        .type("JAVA_IMPORT")
                                        .external(false)
                                        .build()
                        )
                        .toList();

        List<MethodCallMetadata> methodCalls =
                cu.findAll(MethodCallExpr.class)
                        .stream()
                        .map(call ->
                                MethodCallMetadata.builder()
                                        .methodName(call.getNameAsString())
                                        .owner(
                                                call.getScope()
                                                        .map(Object::toString)
                                                        .orElse(null)
                                        )
                                        .arguments(
                                                call.getArguments()
                                                        .stream()
                                                        .map(Object::toString)
                                                        .toList()
                                        )
                                        .lineNumber(
                                                call.getBegin()
                                                        .map(p -> p.line)
                                                        .orElse(-1)
                                        )
                                        .build()
                        )
                        .toList();

        List<ObjectMetadata> objects =
                cu.findAll(ObjectCreationExpr.class)
                        .stream()
                        .map(obj ->
                                ObjectMetadata.builder()
                                        .type(obj.getTypeAsString())
                                        .name(obj.getType().getNameAsString())
                                        .lineNumber(
                                                obj.getBegin()
                                                        .map(p -> p.line)
                                                        .orElse(-1)
                                        )
                                        .attributes(List.of())
                                        .build()
                        )
                        .toList();

        String packageName = cu.getPackageDeclaration()
                        .map(p -> p.getNameAsString())
                        .orElse(null);

        List<String> lambdaExpressions = cu.findAll(LambdaExpr.class).stream().map(Object::toString).toList();

        System.out.println("JAVA CODE PARSER FINISHED");
        return FileMetadata.builder()
                .packageName(packageName)
                .classes(classes)
                .methods(methods)
                .variables(variables)
                .constructors(constructors)
                .enums(enums)
                .records(records)
                .methodCalls(methodCalls)
                .objects(objects)
                .lambdaExpressions(lambdaExpressions)
                .imports(imports)
                .build();
    }
}
