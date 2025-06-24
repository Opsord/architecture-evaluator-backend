package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.abstract_class_instance.AbstractClassService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.record_instance.RecordService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.exception_instance.ExceptionService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part.PackageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(FileInstanceService.class);

    private final ClassService classService;
    private final InterfaceService interfaceService;
    private final AbstractClassService abstractClassService;
    private final RecordService recordService;
    private final ExceptionService exceptionService;
    private final AnnotationService annotationService;
    private final PackageService packageService;

    public FileInstance parseJavaFile(File file, File projectRoot) throws FileNotFoundException {
        // Log the start of parsing
        logger.info("Starting to parse file: {}", file.getAbsolutePath());

        // Create a new JavaParser instance
        JavaParser javaParser = new JavaParser();

        // Parse the Java file into a CompilationUnit (AST)
        CompilationUnit compilationUnit = javaParser.parse(file).getResult()
                .orElseThrow(() -> new FileNotFoundException("File not found or could not be parsed"));
        logger.info("Successfully parsed file: {}", file.getAbsolutePath());

        // Create a new FileInstance to hold the parsed information
        FileInstance fileInstance = new FileInstance();
        fileInstance.setFileName(file.getName());

        // Compute the relative path from the project root to the file
        String relativePath = projectRoot.toPath().toAbsolutePath()
                .relativize(file.toPath().toAbsolutePath())
                .toString();
        fileInstance.setFilePath(relativePath);

        // Create a visitor to extract all relevant information from the CompilationUnit
        FileInstanceVisitor visitor = new FileInstanceVisitor(
                packageService,
                annotationService,
                interfaceService,
                classService
        );

        // Visit the CompilationUnit to fill the FileInstance fields
        compilationUnit.accept(visitor, fileInstance);

        // Set the package name (redundant if already set by the visitor, but ensures it's present)
        fileInstance.setPackageName(packageService.getPackageNameFromCompUnit(compilationUnit));

        // Set file-level annotations
        fileInstance.setFileAnnotations(annotationService.getAnnotationsFromCompUnit(compilationUnit));

        // Return the fully populated FileInstance
        return fileInstance;
    }



    public List<String> getDependentClassNamesFromClass(ClassInstance classInstance, List<FileInstance> allFiles) {
        if (classInstance.getUsedClasses() == null) {
            return List.of();
        }
        return classInstance.getUsedClasses().stream()
                .distinct()
                .filter(usedClass -> allFiles.stream()
                        .anyMatch(file -> file.getJavaTypeInstance().stream()
                                .anyMatch(javaTypeInstance -> javaTypeInstance
                                        .getClass()
                                        .getName()
                                        .equals(usedClass))))
                .toList();
    }
}