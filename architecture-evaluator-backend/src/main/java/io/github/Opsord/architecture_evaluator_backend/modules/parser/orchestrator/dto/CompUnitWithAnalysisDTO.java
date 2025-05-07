package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompUnitWithAnalysisDTO {
    private CustomCompilationUnitDTO compilationUnit;
    private AnalysedCompUnitDTO analysis;
}
