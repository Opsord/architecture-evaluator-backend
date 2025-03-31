// GenericUsageService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.parts.GenericUsageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for extracting generic usage information from a CompilationUnit.
 * This service identifies all instances of generic types (e.g., List<String>, Map<K, V>)
 * within the provided CompilationUnit and collects their type and generic type arguments.
 */
@Service
public class GenericUsageService {

    private static final Logger logger = LoggerFactory.getLogger(GenericUsageService.class);

    public List<GenericUsageDTO> getGenericUsages(CompilationUnit compilationUnit) {
        logger.info("Extracting generic usages from compilation unit");
        List<GenericUsageDTO> genericUsages = new ArrayList<>();
        for (ClassOrInterfaceType genericType : compilationUnit.findAll(ClassOrInterfaceType.class)) {
            if (genericType.getTypeArguments().isPresent()) {
                GenericUsageDTO genericUsageDTO = new GenericUsageDTO();
                genericUsageDTO.setType(genericType.getNameAsString());
                genericUsageDTO.setGenericTypes(genericType.getTypeArguments().get().stream()
                        .map(Type::asString)
                        .collect(Collectors.toList()));
                genericUsages.add(genericUsageDTO);
            }
        }
        return genericUsages;
    }
}