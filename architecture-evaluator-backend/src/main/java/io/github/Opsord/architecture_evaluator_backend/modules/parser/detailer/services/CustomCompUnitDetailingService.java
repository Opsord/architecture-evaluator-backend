package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.DetailedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.ProgramMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.ProgramMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomCompUnitDetailingService {

    private final ProgramMetricsService programMetricsService;

    public DetailedCompUnitDTO generateDetailedCompUnit(CustomCompilationUnitDTO compilationUnitDTO) {
        DetailedCompUnitDTO detailedCompUnit = new DetailedCompUnitDTO();
        // Set the class count
        int classCount = compilationUnitDTO.getClassNames().size();
        detailedCompUnit.setClassCount(classCount);
        // Set the interface count
        int interfaceCount = compilationUnitDTO.getInterfaceNames().size();
        detailedCompUnit.setInterfaceCount(interfaceCount);
        // Set the statement count
        int statementCount = compilationUnitDTO.getStatements().size();
        detailedCompUnit.setStatementCount(statementCount);

        // Set the program metrics
        ProgramMetricsDTO programMetrics = programMetricsService.generateProgramMetrics(compilationUnitDTO);
        detailedCompUnit.setProgramMetrics(programMetrics);

        return detailedCompUnit;
    }
}