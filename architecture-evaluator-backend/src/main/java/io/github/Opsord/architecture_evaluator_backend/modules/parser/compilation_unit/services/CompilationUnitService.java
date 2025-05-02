// JavaParserService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;

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
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CompilationUnitService {

    private static final Logger logger = LoggerFactory.getLogger(CompilationUnitService.class);

    private final AnnotationService annotationService;
    private final ClassService classService;
    private final InterfaceService interfaceService;
    private final StatementService statementService;
    private final MethodService methodService;
    private final VariableService variableService;
    private final PackageService packageService;
    private final CommentService commentService;
    private final ExceptionHandlerService exceptionHandlerService;
    private final GenericUsageService genericUsageService;

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
        dto.setClassNames(classService.getClassNames(compilationUnit));
        dto.setSuperClasses(classService.getSuperClasses(compilationUnit));
        dto.setInterfaceNames(interfaceService.getInterfaceNames(compilationUnit));
        dto.setStatements(statementService.getStatements(compilationUnit));
        dto.setMethods(methodService.getMethods(compilationUnit));
        dto.setVariables(variableService.getVariables(compilationUnit));
        dto.setImportedPackages(packageService.getImportedPackages(compilationUnit));
        dto.setComments(commentService.getComments(compilationUnit));
        dto.setExceptionHandling(exceptionHandlerService.getExceptionHandling(compilationUnit));
        dto.setImplementedInterfaces(interfaceService.getImplementedInterfaces(compilationUnit));
        dto.setAnnotations(annotationService.getAnnotations(compilationUnit));
        dto.setGenericUsages(genericUsageService.getGenericUsages(compilationUnit));

        // Calculate and set lines of code
        dto.setLinesOfCode(calculateLinesOfCode(compilationUnit));

        return dto;
    }

    /**
     * Calculates the number of lines of code in a given CompilationUnit.
     * @param compilationUnit The CompilationUnit to calculate lines of code for
     * @return The number of lines of code
     */
    private int calculateLinesOfCode(CompilationUnit compilationUnit) {
        return compilationUnit.getTokenRange()
                .map(tokens -> Math.toIntExact(StreamSupport.stream(tokens.spliterator(), false)
                        .filter(token -> !token.toString().startsWith("//") && !token.toString().startsWith("/*"))
                        .map(token -> token.getRange().map(range -> range.begin.line).orElse(0))
                        .distinct()
                        .count()))
                .orElse(0);
    }
}