// Annotation.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.AnnotationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnotationService {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationService.class);

    public List<AnnotationDTO> getAnnotations(CompilationUnit compilationUnit) {
        logger.info("Extracting annotations from compilation unit");
        return compilationUnit.findAll(AnnotationExpr.class).stream()
                .map(annotation -> {
                    AnnotationDTO annotationDTO = new AnnotationDTO();
                    annotationDTO.setName(annotation.getNameAsString());
                    annotationDTO.setAttributes(annotation.getChildNodes().stream()
                            .filter(node -> node instanceof MemberValuePair)
                            .map(node -> (MemberValuePair) node)
                            .collect(Collectors.toMap(
                                    MemberValuePair::getNameAsString,
                                    memberValuePair -> memberValuePair.getValue().toString()
                            )));
                    return annotationDTO;
                })
                .collect(Collectors.toList());
    }
}