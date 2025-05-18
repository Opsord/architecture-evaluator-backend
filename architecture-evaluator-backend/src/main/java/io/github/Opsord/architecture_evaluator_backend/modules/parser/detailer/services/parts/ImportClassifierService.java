package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.DependencyDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ImportCategory;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.PomFileDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportClassifierService {

    public void classifyDependencies(PomFileDTO pomFileDTO, AnalysedCompUnitDTO analysedCompUnitDTO, String internalBasePackage) {
        Map<ImportCategory, List<DependencyDTO>> classifiedDependencies = new HashMap<>();

        for (DependencyDTO dependency : pomFileDTO.getDependencies()) {
            ImportCategory category = classify(dependency.getBasePackage(), pomFileDTO.getDependencies(), internalBasePackage);
            classifiedDependencies.computeIfAbsent(category, k -> new ArrayList<>()).add(dependency);
        }

        analysedCompUnitDTO.setClassifiedDependencies(classifiedDependencies);
    }

    public ImportCategory classify(String importName, List<DependencyDTO> knownDependencies, String internalBasePackage) {
        if (importName.startsWith("java.") || importName.startsWith("javax.")) {
            return ImportCategory.JAVA_STANDARD;
        } else if (importName.startsWith(internalBasePackage)) {
            return ImportCategory.INTERNAL;
        } else if (matchesAnyDependency(importName, knownDependencies)) {
            return ImportCategory.EXTERNAL_KNOWN;
        } else {
            return ImportCategory.EXTERNAL_UNKNOWN;
        }
    }

    private boolean matchesAnyDependency(String importName, List<DependencyDTO> dependencies) {
        for (DependencyDTO dep : dependencies) {
            String basePackage = dep.getBasePackage();
            if (importName.startsWith(basePackage)) {
                return true;
            }
        }
        return false;
    }
}
