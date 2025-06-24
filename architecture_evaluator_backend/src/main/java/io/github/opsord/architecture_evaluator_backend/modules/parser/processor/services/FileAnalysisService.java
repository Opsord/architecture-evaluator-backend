package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.JavaTypeAnalysis;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.ProcessedJavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ImportCategory;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.ImportClassifierService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.instances.pom.PomFileInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileAnalysisService {

    private final JavaTypeInstanceAnalysisService javaTypeInstanceAnalysisService;
    private final ImportClassifierService importClassifierService;

    public List<ProcessedJavaTypeInstance> analyseFileInstance(
            FileInstance fileInstance,
            String internalBasePackage,
            Optional<PomFileInstance> pomFileInstance) {
        int[] metrics = computeBasicMetrics(fileInstance);
        Map<ImportCategory, List<String>> classifiedDependencies = classifyFileDependencies(
                pomFileInstance, fileInstance, internalBasePackage);
        if (fileInstance.getJavaTypeInstance() == null || fileInstance.getJavaTypeInstance().isEmpty()) {
            return List.of();
        }

        // Replace stream with direct list creation for better performance
        List<ProcessedJavaTypeInstance> result = new ArrayList<>(fileInstance.getJavaTypeInstance().size());
        for (var classInstance : fileInstance.getJavaTypeInstance()) {
            JavaTypeAnalysis analysis = javaTypeInstanceAnalysisService.analyseClassInstance(
                    classInstance,
                    classifiedDependencies);
            analysis.setClassCount(metrics[0]);
            analysis.setInterfaceCount(metrics[1]);
            analysis.setStatementCount(metrics[2]);

            ProcessedJavaTypeInstance pci = new ProcessedJavaTypeInstance();
            pci.setClassInstance(classInstance);
            pci.setJavaTypeAnalysis(analysis);
            result.add(pci);
        }

        return result;
    }

    private int[] computeBasicMetrics(FileInstance fileInstance) {
        int classCount = 0;
        int interfaceCount = 0;
        int statementCount = 0;

        if (fileInstance.getJavaTypeInstance() != null) {
            // Replace multiple streams with a single pass through the collection
            for (var javaTypeInstance : fileInstance.getJavaTypeInstance()) {
                if (javaTypeInstance.getType() != null) {
                    String type = javaTypeInstance.getType().name();
                    if ("CLASS".equals(type)) {
                        classCount++;
                        // Cast content to ClassInstance and get statements
                        if (javaTypeInstance.getContent() instanceof ClassInstance classInstance && classInstance.getStatements() != null) {
                                statementCount += classInstance.getStatements().size();
                            }

                    } else if ("INTERFACE".equals(type)) {
                        interfaceCount++;
                    }
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