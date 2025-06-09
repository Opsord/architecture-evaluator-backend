package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part.PackageService;
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

    public FileInstance parseJavaFile(File file) throws FileNotFoundException {
        logger.info("Starting to parse file: {}", file.getAbsolutePath());
        JavaParser javaParser = new JavaParser();
        CompilationUnit compilationUnit = javaParser.parse(file).getResult()
                .orElseThrow(() -> new FileNotFoundException("File not found or could not be parsed"));
        logger.info("Successfully parsed file: {}", file.getAbsolutePath());

        FileInstanceVisitor.FileLevelInfo info = getFileLevelInfo(compilationUnit);

        FileInstance fileInstance = new FileInstance();

        // --- File Information ---
        fileInstance.setFileName(file.getName());
        fileInstance.setFilePath(file.getAbsolutePath());
        fileInstance.setPackageName(info.packageName != null ? info.packageName : "default");

        // --- File-level Annotations & Imports ---
        fileInstance.setImportedPackages(info.imports);
        fileInstance.setFileAnnotations(info.annotations);

        // --- Contained Types ---
        fileInstance.setClasses(classService.getClasses(compilationUnit));

        // --- Metrics ---
        fileInstance.setLinesOfCode(calculateLinesOfCode(compilationUnit));
        fileInstance.setImportCount(info.imports != null ? info.imports.size() : 0);

        return fileInstance;
    }

    private FileInstanceVisitor.FileLevelInfo getFileLevelInfo(CompilationUnit cu) {
        FileInstanceVisitor.FileLevelInfo info = new FileInstanceVisitor.FileLevelInfo();
        cu.accept(new FileInstanceVisitor(), info);
        return info;
    }

    public int calculateLinesOfCode(CompilationUnit compilationUnit) {
        return compilationUnit.getRange()
                .map(range -> range.end.line - range.begin.line + 1)
                .orElse(0);
    }

    private List<String> findClassesInPackage(String packageName, List<FileInstance> allFiles) {
        return allFiles.stream()
                .filter(file -> file.getPackageName().equals(packageName))
                .flatMap(file -> file.getClasses().stream().map(ClassInstance::getName))
                .toList();
    }

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
                                .filter(file -> file.getClasses().stream().anyMatch(cls -> imported.equals(cls.getName())));
                    }
                })
                .distinct()
                .filter(file -> file.getClasses().stream().noneMatch(cls -> selfClassNames.contains(cls.getName())))
                .toList();
    }

    public List<FileInstance> findFilesFromPackage(String packageName, List<FileInstance> allFiles) {
        return allFiles.stream()
                .filter(file -> file.getPackageName().equals(packageName))
                .toList();
    }

    public List<String> getDependentClassesFromClass(ClassInstance classInstance, List<FileInstance> allFiles) {
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

    public List<String> getDependentClassesFromFile(FileInstance fileInstance, List<FileInstance> allFiles) {
        if (fileInstance.getClasses() == null) {
            return List.of();
        }
        return fileInstance.getClasses().stream()
                .flatMap(cls -> getDependentClassesFromClass(cls, allFiles).stream())
                .distinct()
                .toList();
    }

    public List<FileInstance> getDependentFileInstances(String targetClassOrPackage, List<FileInstance> allFiles) {
        return allFiles.stream()
                .filter(file -> file.getImportedPackages().stream()
                        .anyMatch(imported -> imported.equals(targetClassOrPackage) ||
                                (imported.endsWith(".*") && targetClassOrPackage.startsWith(imported.substring(0, imported.length() - 2)))))
                .distinct()
                .toList();
    }
}