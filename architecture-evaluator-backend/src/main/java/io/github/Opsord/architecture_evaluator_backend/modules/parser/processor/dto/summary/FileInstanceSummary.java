package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.parts.ClassInstanceSummary;
import lombok.Data;

import java.util.List;

@Data
public class FileInstanceSummary {
    private String fileName;
    private String filePath;
    private String packageName;
    private List<String> fileAnnotations;
    private List<String> importedPackages;
    private List<ClassInstanceSummary> classes;
    private int linesOfCode;
    private int importCount;
}
