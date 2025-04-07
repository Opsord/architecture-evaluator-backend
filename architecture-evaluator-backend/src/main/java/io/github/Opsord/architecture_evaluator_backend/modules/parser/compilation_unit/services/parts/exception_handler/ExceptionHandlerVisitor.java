// ExceptionHandlerVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.exception_handler;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.ExceptionHandlingDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ExceptionHandlerVisitor extends VoidVisitorAdapter<List<ExceptionHandlingDTO>> {

    @Override
    public void visit(TryStmt tryStmt, List<ExceptionHandlingDTO> collector) {
        super.visit(tryStmt, collector);
        ExceptionHandlingDTO exceptionHandlingDTO = new ExceptionHandlingDTO();
        exceptionHandlingDTO.setTryBlock(tryStmt.getTryBlock().toString());
        exceptionHandlingDTO.setCatchBlocks(tryStmt.getCatchClauses().stream()
                .map(Node::toString)
                .collect(Collectors.toList()));
        exceptionHandlingDTO.setFinallyBlock(tryStmt.getFinallyBlock().map(finallyBlock -> finallyBlock.toString()).orElse(null));
        collector.add(exceptionHandlingDTO);
    }
}