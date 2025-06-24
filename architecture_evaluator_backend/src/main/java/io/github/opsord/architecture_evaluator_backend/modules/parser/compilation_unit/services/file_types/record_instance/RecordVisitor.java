package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.record_instance;

import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

public class RecordVisitor extends VoidVisitorAdapter<List<String>> {

    @Override
    public void visit(RecordDeclaration recordDeclaration, List<String> collector) {
        collector.add(recordDeclaration.getNameAsString());
        super.visit(recordDeclaration, collector);
    }
}