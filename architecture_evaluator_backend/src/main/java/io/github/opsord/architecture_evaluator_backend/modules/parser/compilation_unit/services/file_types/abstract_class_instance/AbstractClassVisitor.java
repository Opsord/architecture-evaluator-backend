package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.abstract_class_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class AbstractClassVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<String> collector) {
        if (!declaration.isInterface() && declaration.isAbstract()) {
            collector.add(declaration.getNameAsString());
        }
        super.visit(declaration, collector);
    }
}