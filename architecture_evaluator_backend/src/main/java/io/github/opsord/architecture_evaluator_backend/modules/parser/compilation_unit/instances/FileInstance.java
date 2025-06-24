package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances;

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
    private List<JavaTypeInstance> javaTypeInstance;

    // --- Metrics opcionales (a nivel de fichero) ---
    private int linesOfCode;
    private int importCount;
}
