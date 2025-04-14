package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement;

import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.StatementDTO;

import java.util.List;

public class StatementVisitor extends VoidVisitorAdapter<List<StatementDTO>> {

    @Override
    public void visit(IfStmt ifStmt, List<StatementDTO> collector) {
        super.visit(ifStmt, collector);
        StatementDTO controlStatementDTO = new StatementDTO();
        controlStatementDTO.setType("if");
        controlStatementDTO.setStructure(ifStmt.toString());
        collector.add(controlStatementDTO);
    }

    @Override
    public void visit(ForStmt forStmt, List<StatementDTO> collector) {
        super.visit(forStmt, collector);
        StatementDTO controlStatementDTO = new StatementDTO();
        controlStatementDTO.setType("for");
        controlStatementDTO.setStructure(forStmt.toString());
        collector.add(controlStatementDTO);
    }

    @Override
    public void visit(WhileStmt whileStmt, List<StatementDTO> collector) {
        super.visit(whileStmt, collector);
        StatementDTO controlStatementDTO = new StatementDTO();
        controlStatementDTO.setType("while");
        controlStatementDTO.setStructure(whileStmt.toString());
        collector.add(controlStatementDTO);
    }

    @Override
    public void visit(SwitchStmt switchStmt, List<StatementDTO> collector) {
        super.visit(switchStmt, collector);
        StatementDTO controlStatementDTO = new StatementDTO();
        controlStatementDTO.setType("switch");
        controlStatementDTO.setStructure(switchStmt.toString());
        collector.add(controlStatementDTO);
    }
}