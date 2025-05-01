package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement;

import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.statement.StatementDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.statement.StatementType;

import java.util.List;

public class StatementVisitor extends VoidVisitorAdapter<List<StatementDTO>> {

    @Override
    public void visit(IfStmt ifStmt, List<StatementDTO> collector) {
        super.visit(ifStmt, collector);
        addStatement(StatementType.IF, ifStmt, collector);
    }

    @Override
    public void visit(ForStmt forStmt, List<StatementDTO> collector) {
        super.visit(forStmt, collector);
        addStatement(StatementType.FOR, forStmt, collector);
    }

    @Override
    public void visit(WhileStmt whileStmt, List<StatementDTO> collector) {
        super.visit(whileStmt, collector);
        addStatement(StatementType.WHILE, whileStmt, collector);
    }

    @Override
    public void visit(SwitchStmt switchStmt, List<StatementDTO> collector) {
        super.visit(switchStmt, collector);
        addStatement(StatementType.SWITCH, switchStmt, collector);
    }

    @Override
    public void visit(ExpressionStmt expressionStmt, List<StatementDTO> collector) {
        super.visit(expressionStmt, collector);
        addStatement(StatementType.EXPRESSION, expressionStmt, collector);
    }

    @Override
    public void visit(ReturnStmt returnStmt, List<StatementDTO> collector) {
        super.visit(returnStmt, collector);
        addStatement(StatementType.RETURN, returnStmt, collector);
    }

    @Override
    public void visit(ThrowStmt throwStmt, List<StatementDTO> collector) {
        super.visit(throwStmt, collector);
        addStatement(StatementType.THROW, throwStmt, collector);
    }

    @Override
    public void visit(TryStmt tryStmt, List<StatementDTO> collector) {
        super.visit(tryStmt, collector);
        addStatement(StatementType.TRY, tryStmt, collector);
    }

    private void addStatement(StatementType type, Statement statement, List<StatementDTO> collector) {
        StatementDTO statementDTO = new StatementDTO();
        statementDTO.setType(type);
        statementDTO.setStructure(statement.toString());
        collector.add(statementDTO);
    }
}