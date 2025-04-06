// AnnotationService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.annotation;

import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.AnnotationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnotationService {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationService.class);

    public List<AnnotationDTO> getAnnotations(CompilationUnit compilationUnit) {
        logger.info("Extracting annotations from compilation unit");
        List<AnnotationDTO> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        compilationUnit.accept(visitor, annotations);
        return annotations;
    }
}