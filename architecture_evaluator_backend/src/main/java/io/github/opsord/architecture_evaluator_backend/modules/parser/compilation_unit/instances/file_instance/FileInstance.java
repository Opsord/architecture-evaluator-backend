package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import lombok.Data;

import java.util.List;

@Data
public class FileInstance {
    // --- File Information ---
    private String fileName;
    private String filePath;
    private String packageName;

    // --- File-level Annotations & Imports ---
    private List<String> importedPackages;
    private List<String> fileAnnotations; // e.g. @SpringBootApplication

    // --- Contained Types ---
    private List<ClassInstance> classes;

    // --- Metrics opcionales (a nivel de fichero) ---
    private int linesOfCode;
    private int importCount;
}
