package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part.PackageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
     * Calculates the number of lines of code in a CompilationUnit.
     *
     * @param compilationUnit The CompilationUnit to analyze.
     * @return The number of lines of code.
     */
    public int calculateLinesOfCode(CompilationUnit compilationUnit) {
        return compilationUnit.getRange()
                .map(range -> range.end.line - range.begin.line + 1)
                .orElse(0);
    }

    /**
     * Finds all class names in a given package from a list of FileInstance objects.
     *
     * @param packageName The package name to search for.
     * @param allFiles    The list of all FileInstance objects.
     * @return A list of class names in the specified package.
     */
    private List<String> findClassesInPackage(String packageName, List<FileInstance> allFiles) {
        return allFiles.stream()
                .filter(file -> file.getPackageName().equals(packageName))
                .flatMap(file -> file.getClasses().stream().map(ClassInstance::getName))
                .toList();
    }

    /**
     * Returns a list of imported class names for a given FileInstance.
     *
     * @param fileInstance The FileInstance to analyze.
     * @param allFiles     The list of all FileInstance objects in the project.
     * @return A list of imported class names.
     */
    public List<String> getImportedClasses(FileInstance fileInstance, List<FileInstance> allFiles) {
        Set<String> selfClassNames = new HashSet<>();
        if (fileInstance.getClasses() != null) {
            fileInstance.getClasses().forEach(cls -> selfClassNames.add(cls.getName()));
        }

        return fileInstance.getImportedPackages().stream()
                .flatMap(imported -> {
                    if (imported.endsWith(".*")) {
                        String packageName = imported.substring(0, imported.length() - 2);
                        return findClassesInPackage(packageName, allFiles).stream();
                    } else {
                        return Stream.of(imported);
                    }
                })
                .distinct()
                .filter(className -> !selfClassNames.contains(className))
                .toList();
    }

    /**
     * Returns a list of FileInstance objects that are imported by the given
     * FileInstance.
     *
     * @param fileInstance The FileInstance to analyze.
     * @param allFiles     The list of all FileInstance objects in the project.
     * @return A list of imported FileInstance objects.
     */
    public List<FileInstance> getImportedFileInstances(FileInstance fileInstance, List<FileInstance> allFiles) {
        Set<String> selfClassNames = new HashSet<>();
        if (fileInstance.getClasses() != null) {
            fileInstance.getClasses().forEach(cls -> selfClassNames.add(cls.getName()));
        }

        return fileInstance.getImportedPackages().stream()
                .flatMap(imported -> {
                    if (imported.endsWith(".*")) {
                        String packageName = imported.substring(0, imported.length() - 2);
                        return allFiles.stream()
                                .filter(file -> file.getPackageName().equals(packageName));
                    } else {
                        return allFiles.stream()
                                .filter(file -> file.getClasses().stream()
                                        .anyMatch(cls -> imported.equals(cls.getName())));
                    }
                })
                .distinct()
                .filter(file -> file.getClasses().stream().noneMatch(cls -> selfClassNames.contains(cls.getName())))
                .toList();
    }

    /**
     * Finds all FileInstance objects from a given package.
     *
     * @param packageName The package name to search for.
     * @param allFiles    The list of all FileInstance objects.
     * @return A list of FileInstance objects in the specified package.
     */
    public List<FileInstance> findFilesFromPackage(String packageName, List<FileInstance> allFiles) {
        return allFiles.stream()
                .filter(file -> file.getPackageName().equals(packageName))
                .toList();
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

    /**
     * Returns a list of dependent class names used by all classes in a given
     * FileInstance.
     *
     * @param fileInstance The FileInstance to analyze.
     * @param allFiles     The list of all FileInstance objects in the project.
     * @return A list of dependent class names.
     */
    public List<String> getDependentClassNamesFromFile(FileInstance fileInstance, List<FileInstance> allFiles) {
        if (fileInstance.getClasses() == null) {
            return List.of();
        }
        return fileInstance.getClasses().stream()
                .flatMap(cls -> getDependentClassNamesFromClass(cls, allFiles).stream())
                .distinct()
                .toList();
    }

    /**
     * Returns a list of dependent ClassInstance objects used by all classes in a
     * given FileInstance.
     *
     * @param fileInstance The FileInstance to analyze.
     * @param allFiles     The list of all FileInstance objects in the project.
     * @return A list of dependent ClassInstance objects.
     */
    public List<ClassInstance> getDependentClassInstancesFromFile(FileInstance fileInstance,
            List<FileInstance> allFiles) {
        return fileInstance.getClasses().stream()
                .flatMap(cls -> getDependentClassNamesFromClass(cls, allFiles).stream())
                .distinct()
                .flatMap(className -> allFiles.stream()
                        .flatMap(file -> file.getClasses().stream()
                                .filter(cls -> cls.getName().equals(className))))
                .distinct()
                .toList();
    }

    /**
     * Returns a list of FileInstance objects that import a given class or package.
     *
     * @param targetClassOrPackage The class or package name to search for.
     * @param allFiles             The list of all FileInstance objects in the
     *                             project.
     * @return A list of FileInstance objects that import the target.
     */
    public List<FileInstance> getDependentFileInstances(String targetClassOrPackage, List<FileInstance> allFiles) {
        return allFiles.stream()
                .filter(file -> file.getImportedPackages().stream()
                        .anyMatch(imported -> imported.equals(targetClassOrPackage) ||
                                (imported.endsWith(".*") && targetClassOrPackage
                                        .startsWith(imported.substring(0, imported.length() - 2)))))
                .distinct()
                .toList();
    }

}