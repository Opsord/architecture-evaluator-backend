// JavaParserService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.services;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.services.parts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Service
public class JavaParserService {

    private static final Logger logger = LoggerFactory.getLogger(JavaParserService.class);
    private final AnnotationService annotationService;
    private final ClassService classService;
    private final InterfaceService interfaceService;
    private final ControlStatementService controlStatementService;
    private final GenericUsageService genericUsageService;
    private final ExceptionHandlingService exceptionHandlingService;
    private final VariableService variableService;
    private final MethodService methodService;
    private final CommentService commentService;
    private final PackageService packageService;

    public JavaParserService(AnnotationService annotationService) {
        this.annotationService = annotationService;
        this.classService = new ClassService();
        this.interfaceService = new InterfaceService();
        this.controlStatementService = new ControlStatementService();
        this.genericUsageService = new GenericUsageService();
        this.exceptionHandlingService = new ExceptionHandlingService();
        this.variableService = new VariableService();
        this.methodService = new MethodService();
        this.commentService = new CommentService();
        this.packageService = new PackageService();
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

    private List<ControlStatementDTO> getControlStatements(CompilationUnit compilationUnit) {
        return controlStatementService.getControlStatements(compilationUnit);
    }

    private List<String> getClassNames(CompilationUnit compilationUnit) {
        return classService.getClassNames(compilationUnit);
    }

    private List<String> getSuperClasses(CompilationUnit compilationUnit) {
        return classService.getSuperClasses(compilationUnit);
    }

    private List<String> getInterfaceNames(CompilationUnit compilationUnit) {
        return interfaceService.getInterfaceNames(compilationUnit);
    }

    private List<String> getImplementedInterfaces(CompilationUnit compilationUnit) {
        return interfaceService.getImplementedInterfaces(compilationUnit);
    }

    private List<GenericUsageDTO> getGenericUsages(CompilationUnit compilationUnit) {
        return genericUsageService.getGenericUsages(compilationUnit);
    }

    private List<ExceptionHandlingDTO> getExceptionHandling(CompilationUnit compilationUnit) {
        return exceptionHandlingService.getExceptionHandling(compilationUnit);
    }

    private List<VariableDTO> getVariables(CompilationUnit compilationUnit) {
        return variableService.getVariables(compilationUnit);
    }

    private List<MethodDTO> getMethods(CompilationUnit compilationUnit) {
        return methodService.getMethods(compilationUnit);
    }

    private List<String> getComments(CompilationUnit compilationUnit) {
        return commentService.getComments(compilationUnit);
    }

    private List<String> getImportedPackages(CompilationUnit compilationUnit) {
        return packageService.getImportedPackages(compilationUnit);
    }
}