package io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.file_instance;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.file_instance.package_part.PackageService;
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
    private final PackageService packageService;
    private final AnnotationService annotationService;

    /**
     * Parses a Java file and returns a FileInstance representing its structure and
     * metadata.
     *
     * @param file The Java source file to parse.
     * @return The parsed FileInstance.
     * @throws FileNotFoundException If the file does not exist or cannot be parsed.
     */
    public FileInstance parseJavaFile(File file, File projectRoot) throws FileNotFoundException {
        logger.info("Starting to parse file: {}", file.getAbsolutePath());
        JavaParser javaParser = new JavaParser();
        CompilationUnit compilationUnit = javaParser.parse(file).getResult()
                .orElseThrow(() -> new FileNotFoundException("File not found or could not be parsed"));
        logger.info("Successfully parsed file: {}", file.getAbsolutePath());

        FileInstance fileInstance = new FileInstance();
        fileInstance.setFileName(file.getName());

        // Compute a relative path
        String relativePath = projectRoot.toPath().toAbsolutePath()
                .relativize(file.toPath().toAbsolutePath())
                .toString();
        fileInstance.setFilePath(relativePath);

        FileInstanceVisitor visitor = new FileInstanceVisitor(packageService, annotationService, classService);
        compilationUnit.accept(visitor, fileInstance);

        return fileInstance;
    }

    /**
     * Returns a list of dependent class names used by a given ClassInstance.
     *
     * @param classInstance The ClassInstance to analyze.
     * @param allFiles      The list of all FileInstance objects in the project.
     * @return A list of dependent class names.
     */
    public List<String> getDependentClassNamesFromClass(ClassInstance classInstance, List<FileInstance> allFiles) {
        if (classInstance.getUsedClasses() == null) {
            return List.of();
        }
        return classInstance.getUsedClasses().stream()
                .distinct()
                .filter(usedClass -> allFiles.stream()
                        .anyMatch(file -> file.getClasses().stream()
                                .anyMatch(cls -> cls.getName().equals(usedClass))))
                .toList();
    }

}