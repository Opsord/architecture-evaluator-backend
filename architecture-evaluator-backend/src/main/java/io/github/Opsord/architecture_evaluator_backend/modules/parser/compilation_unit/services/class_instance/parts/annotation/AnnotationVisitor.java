package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.List;

public class AnnotationVisitor extends VoidVisitorAdapter<List<String>> {
    @Override
    public void visit(MarkerAnnotationExpr annotation, List<String> collector) {
        super.visit(annotation, collector);
        collector.add(annotation.getNameAsString());
    }

    @Override
    public void visit(SingleMemberAnnotationExpr annotation, List<String> collector) {
        super.visit(annotation, collector);
        collector.add(annotation.getNameAsString());
    }

    @Override
    public void visit(NormalAnnotationExpr annotation, List<String> collector) {
        super.visit(annotation, collector);
        collector.add(annotation.getNameAsString());
    }
}