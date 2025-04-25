package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.DetailedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.method.DetailedMethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.MethodDetailingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomCompUnitDetailingService {

    private final MethodDetailingService methodDetailingService;

    public DetailedCompUnitDTO generateDetailedCompUnit(CustomCompilationUnitDTO compilationUnitDTO) {
        DetailedCompUnitDTO detailedCompUnit = new DetailedCompUnitDTO();

        // Detail the methods
        List<DetailedMethodDTO> detailedMethods = methodDetailingService.generateDetailedMethods(compilationUnitDTO);

        // Configure the detailed compilation unit
        detailedCompUnit.setClassCount(compilationUnitDTO.getClassNames().size());
        detailedCompUnit.setMethods(detailedMethods);
        detailedCompUnit.setNumberOfMethods(detailedMethods.size());
        detailedCompUnit.setLinesOfCode(compilationUnitDTO.getLinesOfCode());

        return detailedCompUnit;
    }
}