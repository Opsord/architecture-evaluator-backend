package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement;

import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.statement.StatementInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.statement.StatementType;

import java.util.List;

public class StatementVisitor extends VoidVisitorAdapter<List<StatementInstance>> {

    @Override
    public void visit(IfStmt ifStmt, List<StatementInstance> collector) {
        super.visit(ifStmt, collector);
        addStatement(StatementType.IF, ifStmt, collector);
    }

    @Override
    public void visit(ForStmt forStmt, List<StatementInstance> collector) {
        super.visit(forStmt, collector);
        addStatement(StatementType.FOR, forStmt, collector);
    }

    @Override
    public void visit(WhileStmt whileStmt, List<StatementInstance> collector) {
        super.visit(whileStmt, collector);
        addStatement(StatementType.WHILE, whileStmt, collector);
    }

    @Override
    public void visit(SwitchStmt switchStmt, List<StatementInstance> collector) {
        super.visit(switchStmt, collector);
        addStatement(StatementType.SWITCH, switchStmt, collector);
    }

    @Override
    public void visit(ExpressionStmt expressionStmt, List<StatementInstance> collector) {
        super.visit(expressionStmt, collector);
        addStatement(StatementType.EXPRESSION, expressionStmt, collector);
    }

    @Override
    public void visit(ReturnStmt returnStmt, List<StatementInstance> collector) {
        super.visit(returnStmt, collector);
        addStatement(StatementType.RETURN, returnStmt, collector);
    }

    @Override
    public void visit(ThrowStmt throwStmt, List<StatementInstance> collector) {
        super.visit(throwStmt, collector);
        addStatement(StatementType.THROW, throwStmt, collector);
    }

    @Override
    public void visit(TryStmt tryStmt, List<StatementInstance> collector) {
        super.visit(tryStmt, collector);
        addStatement(StatementType.TRY, tryStmt, collector);
    }

    private void addStatement(StatementType type, Statement statement, List<StatementInstance> collector) {
        StatementInstance statementInstance = new StatementInstance();
        statementInstance.setType(type);
        statementInstance.setStructure(statement.toString());
        collector.add(statementInstance);
    }
}