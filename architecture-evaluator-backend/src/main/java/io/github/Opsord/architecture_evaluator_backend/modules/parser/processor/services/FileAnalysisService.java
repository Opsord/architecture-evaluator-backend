package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.ClassAnalysis;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.ProcessedClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ImportCategory;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.ImportClassifierService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileAnalysisService {

    private final ClassAnalysisService classAnalysisService;
    private final ImportClassifierService importClassifierService;

    public List<ProcessedClassInstance> analyseFileInstance(
            FileInstance fileInstance,
            String internalBasePackage,
            PomFileDTO pomFileDTO
    ) {
        int[] metrics = computeBasicMetrics(fileInstance);
        Map<ImportCategory, List<String>> classifiedDependencies = classifyFileDependencies(
                pomFileDTO, fileInstance, internalBasePackage
        );
        if (fileInstance.getClasses() == null || fileInstance.getClasses().isEmpty()) {
            return List.of();
        }

        return fileInstance.getClasses().stream()
                .map(classInstance -> {
                    ClassAnalysis analysis = classAnalysisService.analyseClassInstance(
                            classInstance,
                            classifiedDependencies
                    );
                    analysis.setClassCount(metrics[0]);
                    analysis.setInterfaceCount(metrics[1]);
                    analysis.setStatementCount(metrics[2]);
                    ProcessedClassInstance pci = new ProcessedClassInstance();
                    pci.setClassInstance(classInstance);
                    pci.setClassAnalysis(analysis);
                    return pci;
                })
                .toList();
    }

    // Returns int[]{classCount, interfaceCount, statementCount}
    private int[] computeBasicMetrics(FileInstance fileInstance) {
        int classCount = 0;
        int interfaceCount = 0;
        int statementCount = 0;
        if (fileInstance.getClasses() != null) {
            classCount = (int) fileInstance.getClasses().stream()
                    .filter(c -> c.getJavaFileType() != null && c.getJavaFileType().name().equals("CLASS"))
                    .count();
            interfaceCount = (int) fileInstance.getClasses().stream()
                    .filter(c -> c.getJavaFileType() != null && c.getJavaFileType().name().equals("INTERFACE"))
                    .count();
            statementCount = fileInstance.getClasses().stream()
                    .mapToInt(c -> c.getStatements() != null ? c.getStatements().size() : 0)
                    .sum();
        }
        return new int[]{classCount, interfaceCount, statementCount};
    }

    private Map<ImportCategory, List<String>> classifyFileDependencies(
            PomFileDTO pomFileDTO,
            FileInstance fileInstance,
            String internalBasePackage
    ) {
        return importClassifierService.classifyDependencies(pomFileDTO, fileInstance, internalBasePackage);
    }
}