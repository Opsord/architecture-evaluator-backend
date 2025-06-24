package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.enum_instance;

import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class EnumVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(EnumDeclaration enumDeclaration, List<String> collector) {
        collector.add(enumDeclaration.getNameAsString());
        super.visit(enumDeclaration, collector);
    }
}