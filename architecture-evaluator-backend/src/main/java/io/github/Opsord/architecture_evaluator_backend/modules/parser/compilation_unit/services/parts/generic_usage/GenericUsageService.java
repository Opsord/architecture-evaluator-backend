// GenericUsageService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.generic_usage;

import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.GenericUsageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GenericUsageService {

    private static final Logger logger = LoggerFactory.getLogger(GenericUsageService.class);

    public List<GenericUsageDTO> getGenericUsages(CompilationUnit compilationUnit) {
        logger.info("Extracting generic usages from compilation unit");
        List<GenericUsageDTO> genericUsages = new ArrayList<>();
        GenericUsageVisitor visitor = new GenericUsageVisitor();
        compilationUnit.accept(visitor, genericUsages);
        return genericUsages;
    }
}