package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CouplingMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ImportCategory;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ProgramMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.CouplingMetricsService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.ImportClassifierService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.ProgramMetricsService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.DependencyDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CCUSummarizingService {

    private final ProgramMetricsService programMetricsService;
    private final CouplingMetricsService couplingMetricsService;
    private final ImportClassifierService importClassifierService;

    public AnalysedCompUnitDTO analyseCompUnit(CustomCompilationUnitDTO compilationUnitDTO,
                                               List<CustomCompilationUnitDTO> allUnits,
                                               String internalBasePackage,
                                               PomFileDTO pomFileDTO) {
        AnalysedCompUnitDTO detailedCompUnit = new AnalysedCompUnitDTO();

        // Set the classified dependencies
        Map<ImportCategory, List<String>> classifiedDependencies = importClassifierService.classifyDependencies(
                pomFileDTO,
                compilationUnitDTO,
                internalBasePackage);
        detailedCompUnit.setClassifiedDependencies(classifiedDependencies);

        // Basic metrics
        detailedCompUnit.setClassCount(compilationUnitDTO.getClassName().size());
        detailedCompUnit.setInterfaceCount(compilationUnitDTO.getInterfaceNames().size());
        detailedCompUnit.setStatementCount(compilationUnitDTO.getStatements().size());

        // Set the program metrics
        ProgramMetricsDTO programMetrics = programMetricsService.generateProgramMetrics(compilationUnitDTO);
        detailedCompUnit.setProgramMetrics(programMetrics);

        // Set the coupling metrics
        CouplingMetricsDTO couplingMetrics = couplingMetricsService.calculateCouplingMetrics(compilationUnitDTO, allUnits, classifiedDependencies);
        detailedCompUnit.setCouplingMetrics(couplingMetrics);

        return detailedCompUnit;
    }
}