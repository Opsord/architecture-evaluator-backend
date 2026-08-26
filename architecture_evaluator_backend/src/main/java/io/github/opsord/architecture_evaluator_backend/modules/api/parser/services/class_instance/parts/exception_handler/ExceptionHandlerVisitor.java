//ExceptionHandlerVisitor.java
package io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.exception_handler;

import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.ExceptionHandlingInstance;

import java.util.List;

public class ExceptionHandlerVisitor extends VoidVisitorAdapter<List<ExceptionHandlingInstance>> {

    @Override
    public void visit(TryStmt tryStmt, List<ExceptionHandlingInstance> collector) {
        super.visit(tryStmt, collector);
        ExceptionHandlingInstance exceptionHandlingInstance = new ExceptionHandlingInstance();
        exceptionHandlingInstance.setCatchBlocks(tryStmt.getCatchClauses().stream()
                .map(clause -> clause.getParameter().getType().asString())
                .toList());
        collector.add(exceptionHandlingInstance);
    }
}