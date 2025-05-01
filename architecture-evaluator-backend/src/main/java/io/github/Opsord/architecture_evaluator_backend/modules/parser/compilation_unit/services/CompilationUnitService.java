// JavaParserService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.*;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.statement.StatementDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.annotation.AnnotationService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.class_part.ClassService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.comment.CommentService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.exception_handler.ExceptionHandlerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.generic_usage.GenericUsageService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.interface_part.InterfaceService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method.MethodService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.package_part.PackageService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.variable.VariableService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompilationUnitService {

    private static final Logger logger = LoggerFactory.getLogger(CompilationUnitService.class);

    private final AnnotationService annotationService;
    private final ClassService classService;
    private final InterfaceService interfaceService;
    private final GenericUsageService genericUsageService;
    private final ExceptionHandlerService exceptionHandlerService;
    private final VariableService variableService;
    private final StatementService statementService;
    private final MethodService methodService;
    private final CommentService commentService;
    private final PackageService packageService;

    private List<AnnotationDTO> getAnnotations(CompilationUnit compilationUnit) {
        return annotationService.getAnnotations(compilationUnit);
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
        return exceptionHandlerService.getExceptionHandling(compilationUnit);
    }

    private List<VariableDTO> getVariables(CompilationUnit compilationUnit) {
        return variableService.getVariables(compilationUnit);
    }

    private List<StatementDTO> getStatements(CompilationUnit compilationUnit) {
        return statementService.getStatements(compilationUnit);
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

    /**
     * Parses a Java file and returns a CompilationUnitDTO object containing the parsed information.
     * @param file Path to the Java file to be parsed
     * @return CompilationUnitDTO object containing the parsed information
     */

    public CustomCompilationUnitDTO parseJavaFile(File file) throws FileNotFoundException {
        logger.info("Starting to parse file: {}", file.getAbsolutePath());
        JavaParser javaParser = new JavaParser();
        CompilationUnit compilationUnit = javaParser.parse(file).getResult()
                .orElseThrow(() -> new FileNotFoundException("File not found or could not be parsed"));
        logger.info("Successfully parsed file: {}", file.getAbsolutePath());

        CustomCompilationUnitDTO dto = new CustomCompilationUnitDTO();
        dto.setPackageName(compilationUnit.getPackageDeclaration().map(pd -> pd.getName().toString()).orElse("default"));
        dto.setClassNames(getClassNames(compilationUnit));
        dto.setInterfaceNames(getInterfaceNames(compilationUnit));
        dto.setStatements(getStatements(compilationUnit));
        dto.setMethods(getMethods(compilationUnit));
        dto.setVariables(getVariables(compilationUnit));
        dto.setImportedPackages(getImportedPackages(compilationUnit));
        dto.setComments(getComments(compilationUnit));
        dto.setExceptionHandling(getExceptionHandling(compilationUnit));
        dto.setSuperClasses(getSuperClasses(compilationUnit));
        dto.setImplementedInterfaces(getImplementedInterfaces(compilationUnit));
        dto.setAnnotations(getAnnotations(compilationUnit));
        dto.setGenericUsages(getGenericUsages(compilationUnit));

        // Calculate and set lines of code
        int linesOfCode = compilationUnit.getEnd().map(end -> end.line).orElse(0) - compilationUnit.getBegin().map(begin -> begin.line).orElse(0) + 1;
        dto.setLinesOfCode(linesOfCode);

        return dto;
    }
}