package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.ClassAnalysis;
import lombok.Data;

import java.util.List;

@Data
public class AnalysedFileInstance {
    private FileInstance fileInstance;
    private List<ClassAnalysis> classAnalyses;
}
