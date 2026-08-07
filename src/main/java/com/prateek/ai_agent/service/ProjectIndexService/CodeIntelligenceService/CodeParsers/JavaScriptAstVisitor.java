package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers;

//import com.github.javaparser.ast.ImportDeclaration;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.*;
import lombok.Getter;
import org.mozilla.javascript.ast.*;

import java.util.ArrayList;
import java.util.List;

@Getter
public class JavaScriptAstVisitor implements NodeVisitor {

    private final List<MethodMetadata> methods = new ArrayList<>();
    private final List<VariableMetadata> variables = new ArrayList<>();
    private final List<ImportMetadata> imports = new ArrayList<>();
    private final List<ObjectMetadata> objects = new ArrayList<>();
    private final List<MethodCallMetadata> methodCalls = new ArrayList<>();
    private final String source;

    public JavaScriptAstVisitor(String source) {
        this.source = source;
    }

    @Override
    public boolean visit(AstNode node) {

        if (node instanceof FunctionNode functionNode) {
            processFunction(functionNode);
        }
        if (node instanceof VariableDeclaration declaration) {
            processVariableDeclaration(declaration);
        }
        if (node instanceof FunctionCall functionCall) {
            processMethodCall(functionCall);
        }
        return true;
    }
//    private void processFunction(FunctionNode functionNode) {
//
//        MethodMetadata method = MethodMetadata.builder()
//                .name(getFunctionName(functionNode))
//                .parameters(getParameters(functionNode))
//                .async(false)
//                .arrowFunction(false)
//                .exported(false)
//                .startLine(functionNode.getLineno())
//                .endLine(-1)
//                .build();
//
//        methods.add(method);
//    }
    private void processFunction(FunctionNode functionNode) {

        int startLine = functionNode.getLineno();
        int endLine = getEndLine(functionNode);

        MethodMetadata method = MethodMetadata.builder()
                .name(getFunctionName(functionNode))
                .parameters(getParameters(functionNode))
                .async(isAsyncFunction(functionNode))
                .arrowFunction(isArrowFunction(functionNode))
                .exported(isExportedFunction(functionNode))
                .startLine(startLine)
                .endLine(endLine)
                .build();
        methods.add(method);
    }
    private boolean isAsyncFunction(FunctionNode functionNode) {
        return functionNode.toSource().stripLeading().startsWith("async ");
    }
    private boolean isArrowFunction(FunctionNode functionNode) {
        return functionNode.toSource().contains("=>");
    }
    private boolean isExportedFunction(FunctionNode functionNode) {
        AstNode parent = functionNode.getParent();
        if (parent == null) return false;
        return parent.toSource().stripLeading().startsWith("export ");
    }
    private int getEndLine(FunctionNode functionNode) {

        int startPosition = functionNode.getAbsolutePosition();
        int length = functionNode.getLength();

        if (startPosition < 0 || length <= 0) {
            return functionNode.getLineno();
        }

        int endPosition = Math.min(
                startPosition + length,
                source.length()
        );

        int endLine = functionNode.getLineno();

        for (int i = startPosition; i < endPosition; i++) {
            if (source.charAt(i) == '\n') {
                endLine++;
            }
        }
        return endLine;
    }
    private String getFunctionName(FunctionNode functionNode) {
        Name functionName = functionNode.getFunctionName();
        if (functionName == null) {
            return "<anonymous>";
        }
        return functionName.getIdentifier();
    }
    private List<String> getParameters(FunctionNode functionNode) {

        List<String> parameters = new ArrayList<>();
        for (AstNode parameter : functionNode.getParams()) {
            if (parameter instanceof Name name) {
                parameters.add(name.getIdentifier());
            }
            else {
                parameters.add(parameter.toSource());
            }
        }
        return parameters;
    }

    private void processVariableDeclaration(
            VariableDeclaration declaration
    ) {

        String variableType = declaration.isConst()
                ? "const"
                : declaration.isLet()
                ? "let"
                : "var";

        for (VariableInitializer initializer : declaration.getVariables()) {

            AstNode target = initializer.getTarget();
            if (!(target instanceof Name name)) {
                continue;
            }
            AstNode initializerNode = initializer.getInitializer();

            variables.add(
                    VariableMetadata.builder()
                            .name(name.getIdentifier())
                            .type(variableType)
                            .initializer(initializerNode == null ? null : initializerNode.toSource())
                            .ownerClass(null)
                            .ownerMethod(null)
                            .modifiers(List.of(variableType))
                            .annotations(List.of())
                            .field(false)
                            .lineNumber(name.getLineno())
                            .build()
            );
            // Object literal
            if (initializerNode instanceof ObjectLiteral objectLiteral) {
                processObjectLiteral(
                        name.getIdentifier(),
                        objectLiteral
                );
            }
        }
    }

    private void processMethodCall(FunctionCall functionCall) {

        AstNode target = functionCall.getTarget();
        MethodCallMetadata metadata = MethodCallMetadata.builder()
                .methodName(getMethodName(target))
                .owner(getOwner(target))
                .arguments(
                        functionCall.getArguments()
                                .stream()
                                .map(AstNode::toSource)
                                .toList()
                )
                .lineNumber(functionCall.getLineno())
                .build();
        methodCalls.add(metadata);
    }
    private String getMethodName(AstNode target) {

        if (target instanceof Name name) {
            return name.getIdentifier();
        }
        if (target instanceof PropertyGet propertyGet) {
            return propertyGet.getProperty().getIdentifier();
        }
        return target.toSource();
    }
    private String getOwner(AstNode target) {

        if (target instanceof PropertyGet propertyGet) {
            return propertyGet.getTarget().toSource();
        }
        return null;
    }

    private void processObjectLiteral(String variableName, ObjectLiteral objectLiteral) {

        List<String> attributes = new ArrayList<>();
        for (ObjectProperty property : objectLiteral.getElements()) {
            AstNode left = property.getLeft();
            if (left instanceof Name name) {
                attributes.add(name.getIdentifier());
            }
            else {
                attributes.add(left.toSource());
            }
        }
        objects.add(
                ObjectMetadata.builder()
                        .type("OBJECT_LITERAL")
                        .name(variableName)
                        .lineNumber(objectLiteral.getLineno())
                        .attributes(attributes)
                        .text(null)
                        .parent(null)
                        .children(List.of())
                        .build()
        );
    }

}