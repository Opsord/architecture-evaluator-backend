package io.github.opsord.architecture_evaluator_backend.modules.api.processor.services;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ClassAnalysisInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ProcessedClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.ImportCategory;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.ImportClassifierService;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom.PomFileInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileAnalysisService {

    private final ClassAnalysisService classAnalysisService;
    private final ImportClassifierService importClassifierService;

    public List<ProcessedClassInstance> analyseFileInstance(
            FileInstance fileInstance,
            String internalBasePackage,
            Optional<PomFileInstance> pomFileInstance) {
        int[] metrics = computeBasicMetrics(fileInstance);
        Map<ImportCategory, List<String>> classifiedDependencies = classifyFileDependencies(
                pomFileInstance, fileInstance, internalBasePackage);
        if (fileInstance.getClasses() == null || fileInstance.getClasses().isEmpty()) {
            return List.of();
        }

        // Replace stream with direct list creation for better performance
        List<ProcessedClassInstance> result = new ArrayList<>(fileInstance.getClasses().size());
        for (var classInstance : fileInstance.getClasses()) {
            ClassAnalysisInstance analysis = classAnalysisService.analyseClassInstance(
                    classInstance,
                    classifiedDependencies);
            analysis.setClassCount(metrics[0]);
            analysis.setInterfaceCount(metrics[1]);
            analysis.setStatementCount(metrics[2]);

            ProcessedClassInstance pci = new ProcessedClassInstance();
            pci.setClassInstance(classInstance);
            pci.setClassAnalysisInstance(analysis);
            result.add(pci);
        }

        return result;
    }

    private int[] computeBasicMetrics(FileInstance fileInstance) {
        int classCount = 0;
        int interfaceCount = 0;
        int statementCount = 0;

        if (fileInstance.getClasses() != null) {
            // Replace multiple streams with a single pass through the collection
            for (var classInstance : fileInstance.getClasses()) {
                // Count class types
                if (classInstance.getJavaFileType() != null) {
                    String type = classInstance.getJavaFileType().name();
                    if ("CLASS".equals(type)) {
                        classCount++;
                    } else if ("INTERFACE".equals(type)) {
                        interfaceCount++;
                    }
                }

                // Count statements in a single pass
                if (classInstance.getStatements() != null) {
                    statementCount += classInstance.getStatements().size();
                }
            }
        }

        return new int[] { classCount, interfaceCount, statementCount };
    }

    private Map<ImportCategory, List<String>> classifyFileDependencies(
            Optional<PomFileInstance> pomFileInstance,
            FileInstance fileInstance,
            String internalBasePackage) {
        return importClassifierService.classifyDependencies(pomFileInstance, fileInstance, internalBasePackage);
    }
}