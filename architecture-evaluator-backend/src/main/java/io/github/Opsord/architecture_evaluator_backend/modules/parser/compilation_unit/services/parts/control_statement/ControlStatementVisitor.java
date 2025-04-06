// ControlStatementVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.control_statement;

import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.ControlStatementDTO;

import java.util.List;

public class ControlStatementVisitor extends VoidVisitorAdapter<List<ControlStatementDTO>> {

    @Override
    public void visit(IfStmt ifStmt, List<ControlStatementDTO> collector) {
        super.visit(ifStmt, collector);
        ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
        controlStatementDTO.setType("if");
        controlStatementDTO.setStructure(ifStmt.toString());
        collector.add(controlStatementDTO);
    }

    @Override
    public void visit(ForStmt forStmt, List<ControlStatementDTO> collector) {
        super.visit(forStmt, collector);
        ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
        controlStatementDTO.setType("for");
        controlStatementDTO.setStructure(forStmt.toString());
        collector.add(controlStatementDTO);
    }

    @Override
    public void visit(WhileStmt whileStmt, List<ControlStatementDTO> collector) {
        super.visit(whileStmt, collector);
        ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
        controlStatementDTO.setType("while");
        controlStatementDTO.setStructure(whileStmt.toString());
        collector.add(controlStatementDTO);
    }

    @Override
    public void visit(SwitchStmt switchStmt, List<ControlStatementDTO> collector) {
        super.visit(switchStmt, collector);
        ControlStatementDTO controlStatementDTO = new ControlStatementDTO();
        controlStatementDTO.setType("switch");
        controlStatementDTO.setStructure(switchStmt.toString());
        collector.add(controlStatementDTO);
    }
}