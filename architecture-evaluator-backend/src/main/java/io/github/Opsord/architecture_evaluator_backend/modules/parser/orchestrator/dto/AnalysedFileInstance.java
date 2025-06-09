package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysedFileInstance {
    private FileInstance fileInstance;
    private io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.AnalysedFileInstance analysis;
}
