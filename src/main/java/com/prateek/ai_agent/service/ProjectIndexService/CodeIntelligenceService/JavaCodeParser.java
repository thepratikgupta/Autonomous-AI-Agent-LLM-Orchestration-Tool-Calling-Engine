package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ClassMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.MethodMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.VariableMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
                                .modifiers(
                                        c.getModifiers()
                                                .stream()
                                                .map(modifier ->
                                                        modifier.getKeyword().asString())
                                                .toList()
                                )
                                .annotations(
                                        c.getAnnotations()
                                                .stream()
                                                .map(annotation ->
                                                        annotation.getNameAsString())
                                                .toList()
                                )

                                .build())
                        .toList();

        List<VariableMetadata> fields =
                cu.findAll(FieldDeclaration.class)
                        .stream()
                        .flatMap(field ->
                                field.getVariables()
                                        .stream()
                                        .map(variable ->
                                                VariableMetadata.builder()
                                                        .name(variable.getNameAsString())
                                                        .type(variable.getTypeAsString())
                                                        .lineNumber(
                                                                variable.getBegin()
                                                                        .map(p -> p.line)
                                                                        .orElse(-1)
                                                        )
                                                        .modifiers(
                                                                field.getModifiers()
                                                                        .stream()
                                                                        .map(modifier ->
                                                                                modifier.getKeyword().asString())
                                                                        .toList()
                                                        )
                                                        .annotations(
                                                                field.getAnnotations()
                                                                        .stream()
                                                                        .map(annotation ->
                                                                                annotation.getNameAsString())
                                                                        .toList()
                                                        )
                                                        .field(true)
                                                        .build()))
                        .toList();
        List<VariableMetadata> localVariables =
                cu.findAll(VariableDeclarator.class)
                        .stream()
                        .filter(variable ->
                                variable.getParentNode()
                                        .map(parent -> !(parent instanceof FieldDeclaration))
                                        .orElse(true))
                        .map(variable ->
                                VariableMetadata.builder()
                                        .name(variable.getNameAsString())
                                        .type(variable.getTypeAsString())
                                        .lineNumber(
                                                variable.getBegin()
                                                        .map(p -> p.line)
                                                        .orElse(-1)
                                        )
                                        .modifiers(List.of())
                                        .annotations(List.of())
                                        .field(false)
                                        .build())
                        .toList();
        List<VariableMetadata> variables = new ArrayList<>();
        variables.addAll(fields);
        variables.addAll(localVariables);

        List<MethodMetadata> methods = cu.findAll(MethodDeclaration.class)
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
                                .modifiers(
                                        method.getModifiers()
                                                .stream()
                                                .map(modifier ->
                                                        modifier.getKeyword().asString())
                                                .toList()
                                )
                                .annotations(
                                        method.getAnnotations()
                                                .stream()
                                                .map(annotation ->
                                                        annotation.getNameAsString())
                                                .toList()
                                )
                                .thrownExceptions(
                                        method.getThrownExceptions()
                                                .stream()
                                                .map(Object::toString)
                                                .toList()
                                )
                                .build())
                        .toList();

        List<String> imports = cu.getImports()
                        .stream()
                        .map(i -> i.getNameAsString())
                        .toList();

        String packageName = cu.getPackageDeclaration()
                        .map(p -> p.getNameAsString())
                        .orElse(null);

        List<String> constructors = cu.findAll(ConstructorDeclaration.class).stream().map(p->p.getNameAsString()).toList();

        List<String> enums = cu.findAll(EnumDeclaration.class).stream().map(p->p.getNameAsString()).toList();
        List<String> records = cu.findAll(RecordDeclaration.class).stream().map(p->p.getNameAsString()).toList();
        List<String> methodCalls = cu.findAll(MethodCallExpr.class).stream().map(MethodCallExpr::getNameAsString).toList();
        List<String> objects = cu.findAll(ObjectCreationExpr.class).stream().map(ObjectCreationExpr::getTypeAsString).toList();
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
