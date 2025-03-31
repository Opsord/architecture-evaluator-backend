// JavaParserService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.services;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.services.parts.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JavaParserService {

    private static final Logger logger = LoggerFactory.getLogger(JavaParserService.class);
    private final Annotation annotationService;

    public JavaParserService(Annotation annotationService) {
        this.annotationService = annotationService;
    }

    public CompilationUnitDTO parseJavaFile(File file) throws FileNotFoundException {
        logger.info("Starting to parse file: {}", file.getAbsolutePath());
        JavaParser javaParser = new JavaParser();
        CompilationUnit compilationUnit = javaParser.parse(file).getResult()
                .orElseThrow(() -> new FileNotFoundException("File not found or could not be parsed"));
        logger.info("Successfully parsed file: {}", file.getAbsolutePath());

        CompilationUnitDTO dto = new CompilationUnitDTO();
        dto.setPackageName(compilationUnit.getPackageDeclaration().map(pd -> pd.getName().toString()).orElse("default"));
        dto.setClassNames(getClassNames(compilationUnit));
        dto.setInterfaceNames(getInterfaceNames(compilationUnit));
        dto.setMethods(getMethods(compilationUnit));
        dto.setVariables(getVariables(compilationUnit));
        dto.setImportedPackages(getImportedPackages(compilationUnit));
        dto.setComments(getComments(compilationUnit));
        dto.setControlStatements(getControlStatements(compilationUnit));
        dto.setExceptionHandling(getExceptionHandling(compilationUnit));
        dto.setSuperClasses(getSuperClasses(compilationUnit));
        dto.setImplementedInterfaces(getImplementedInterfaces(compilationUnit));
        dto.setAnnotations(getAnnotations(compilationUnit));
        dto.setGenericUsages(getGenericUsages(compilationUnit));

        return dto;
    }

    private List<AnnotationDTO> getAnnotations(CompilationUnit compilationUnit) {
        return annotationService.getAnnotations(compilationUnit);
    }

    private List<String> getClassNames(CompilationUnit compilationUnit) {
        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(c -> !c.isInterface())
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .collect(Collectors.toList());
    }

    private List<String> getInterfaceNames(CompilationUnit compilationUnit) {
        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(ClassOrInterfaceDeclaration::isInterface)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .collect(Collectors.toList());
    }

    private List<MethodDTO> getMethods(CompilationUnit compilationUnit) {
        List<MethodDTO> methods = new ArrayList<>();
        for (MethodDeclaration method : compilationUnit.findAll(MethodDeclaration.class)) {
            MethodDTO methodDTO = new MethodDTO();
            methodDTO.setName(method.getNameAsString());
            methodDTO.setAccessModifier(method.getAccessSpecifier().asString());
            methodDTO.setReturnType(method.getType().asString());
            methodDTO.setParameters(method.getParameters().stream()
                    .map(p -> {
                        ParameterDTO parameterDTO = new ParameterDTO();
                        parameterDTO.setName(p.getNameAsString());
                        parameterDTO.setType(p.getType().asString());
                        return parameterDTO;
                    }).collect(Collectors.toList()));
            methods.add(methodDTO);
        }
        return methods;
    }

    private List<VariableDTO> getVariables(CompilationUnit compilationUnit) {
        List<VariableDTO> variables = new ArrayList<>();
        for (VariableDeclarator variable : compilationUnit.findAll(VariableDeclarator.class)) {
            VariableDTO variableDTO = new VariableDTO();
            variableDTO.setName(variable.getNameAsString());
            variableDTO.setType(variable.getType().asString());
            variables.add(variableDTO);
        }
        return variables;
    }

    private List<String> getImportedPackages(CompilationUnit compilationUnit) {
        return compilationUnit.getImports().stream()
                .map(importDeclaration -> importDeclaration.getName().asString())
                .collect(Collectors.toList());
    }

    private List<String> getComments(CompilationUnit compilationUnit) {
        return compilationUnit.getAllContainedComments().stream()
                .map(Comment::getContent)
                .collect(Collectors.toList());
    }

    private List<ControlStatementDTO> getControlStatements(CompilationUnit compilationUnit) {
        List<ControlStatementDTO> controlStatements = new ArrayList<>();
        for (IfStmt ifStmt : compilationUnit.findAll(IfStmt.class)) {
            ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
            controlStatementDTO.setType("if");
            controlStatementDTO.setStructure(ifStmt.toString());
            controlStatements.add(controlStatementDTO);
        }
        for (ForStmt forStmt : compilationUnit.findAll(ForStmt.class)) {
            ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
            controlStatementDTO.setType("for");
            controlStatementDTO.setStructure(forStmt.toString());
            controlStatements.add(controlStatementDTO);
        }
        for (WhileStmt whileStmt : compilationUnit.findAll(WhileStmt.class)) {
            ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
            controlStatementDTO.setType("while");
            controlStatementDTO.setStructure(whileStmt.toString());
            controlStatements.add(controlStatementDTO);
        }
        return controlStatements;
    }

    private List<ExceptionHandlingDTO> getExceptionHandling(CompilationUnit compilationUnit) {
        List<ExceptionHandlingDTO> exceptionHandling = new ArrayList<>();
        for (TryStmt tryStmt : compilationUnit.findAll(TryStmt.class)) {
            ExceptionHandlingDTO exceptionHandlingDTO = new ExceptionHandlingDTO();
            exceptionHandlingDTO.setTryBlock(tryStmt.getTryBlock().toString());
            exceptionHandlingDTO.setCatchBlocks(tryStmt.getCatchClauses().stream()
                    .map(Node::toString)
                    .collect(Collectors.toList()));
            exceptionHandlingDTO.setFinallyBlock(tryStmt.getFinallyBlock().map(BlockStmt::toString).orElse(null));
            exceptionHandling.add(exceptionHandlingDTO);
        }
        return exceptionHandling;
    }

    private List<String> getSuperClasses(CompilationUnit compilationUnit) {
        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(c -> !c.getExtendedTypes().isEmpty())
                .map(c -> c.getExtendedTypes().get(0).getNameAsString())
                .collect(Collectors.toList());
    }

    private List<String> getImplementedInterfaces(CompilationUnit compilationUnit) {
        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .flatMap(c -> c.getImplementedTypes().stream())
                .map(ClassOrInterfaceType::getNameAsString)
                .collect(Collectors.toList());
    }

    private List<GenericUsageDTO> getGenericUsages(CompilationUnit compilationUnit) {
        List<GenericUsageDTO> genericUsages = new ArrayList<>();
        for (ClassOrInterfaceType genericType : compilationUnit.findAll(ClassOrInterfaceType.class)) {
            if (genericType.getTypeArguments().isPresent()) {
                GenericUsageDTO genericUsageDTO = new GenericUsageDTO();
                genericUsageDTO.setType(genericType.getNameAsString());
                genericUsageDTO.setGenericTypes(genericType.getTypeArguments().get().stream()
                        .map(Type::asString)
                        .collect(Collectors.toList()));
                genericUsages.add(genericUsageDTO);
            }
        }
        return genericUsages;
    }
}