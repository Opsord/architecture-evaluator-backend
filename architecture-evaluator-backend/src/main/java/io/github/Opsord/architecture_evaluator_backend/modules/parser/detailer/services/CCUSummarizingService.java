package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CouplingMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ProgramMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.CouplingMetricsService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.ProgramMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CCUSummarizingService {

    private final ProgramMetricsService programMetricsService;
    private final CouplingMetricsService couplingMetricsService;

    public AnalysedCompUnitDTO analyseCompUnit(CustomCompilationUnitDTO compilationUnitDTO, List<CustomCompilationUnitDTO> allUnits) {
        AnalysedCompUnitDTO detailedCompUnit = new AnalysedCompUnitDTO();

        // Basic metrics
        detailedCompUnit.setClassCount(compilationUnitDTO.getClassNames().size());
        detailedCompUnit.setInterfaceCount(compilationUnitDTO.getInterfaceNames().size());
        detailedCompUnit.setStatementCount(compilationUnitDTO.getStatements().size());

        // Set the program metrics
        ProgramMetricsDTO programMetrics = programMetricsService.generateProgramMetrics(compilationUnitDTO);
        detailedCompUnit.setProgramMetrics(programMetrics);

        // Set the coupling metrics
        CouplingMetricsDTO couplingMetrics = couplingMetricsService.calculateCouplingMetrics(compilationUnitDTO, allUnits);
        detailedCompUnit.setCouplingMetrics(couplingMetrics);

        return detailedCompUnit;
    }
}