package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.annotation;

import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.AnnotationDTO;

import java.util.List;
import java.util.stream.Collectors;

public class AnnotationVisitor extends VoidVisitorAdapter<List<AnnotationDTO>> {

    @Override
    public void visit(MarkerAnnotationExpr annotation, List<AnnotationDTO> collector) {
        super.visit(annotation, collector);
        AnnotationDTO annotationDTO = new AnnotationDTO();
        annotationDTO.setName(annotation.getNameAsString());
        annotationDTO.setAttributes(annotation.getChildNodes().stream()
                .filter(node -> node instanceof MemberValuePair)
                .map(node -> (MemberValuePair) node)
                .collect(Collectors.toMap(
                        MemberValuePair::getNameAsString,
                        memberValuePair -> memberValuePair.getValue().toString()
                )));
        collector.add(annotationDTO);
    }

}