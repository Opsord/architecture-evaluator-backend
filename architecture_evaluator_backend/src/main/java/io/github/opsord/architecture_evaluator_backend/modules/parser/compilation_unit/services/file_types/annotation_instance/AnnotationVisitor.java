package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance;

import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import java.util.List;

public class AnnotationVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration node, List<String> collector) {
        collectAnnotations(node, collector);
        super.visit(node, collector);
    }

    @Override
    public void visit(EnumDeclaration node, List<String> collector) {
        collectAnnotations(node, collector);
        super.visit(node, collector);
    }

    @Override
    public void visit(AnnotationDeclaration node, List<String> collector) {
        collectAnnotations(node, collector);
        super.visit(node, collector);
    }

    private void collectAnnotations(NodeWithAnnotations<?> node, List<String> collector) {
        node.getAnnotations().forEach(a -> collector.add(a.getNameAsString()));
    }
}