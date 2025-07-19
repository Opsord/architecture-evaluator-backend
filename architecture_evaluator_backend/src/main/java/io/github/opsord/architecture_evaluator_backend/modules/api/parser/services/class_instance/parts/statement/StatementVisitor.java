package io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.statement;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementType;

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

    @Override
    public void visit(CatchClause catchClause, List<StatementInstance> collector) {
        super.visit(catchClause, collector);
        StatementInstance statementInstance = new StatementInstance();
        statementInstance.setType(StatementType.CATCH);
        statementInstance.setStructure(catchClause.toString());
        collector.add(statementInstance);
    }

    private void addStatement(StatementType type, Statement statement, List<StatementInstance> collector) {
        StatementInstance statementInstance = new StatementInstance();
        statementInstance.setType(type);
        statementInstance.setStructure(statement.toString());
        collector.add(statementInstance);
    }

    // Utility methods for counting operators in expressions
    public static int countLogicalOperators(Expression expr) {
        if (expr == null) return 0;
        int count = 0;
        if (expr instanceof BinaryExpr binaryExpr) {
            if (binaryExpr.getOperator() == BinaryExpr.Operator.AND || binaryExpr.getOperator() == BinaryExpr.Operator.OR) {
                count++;
            }
            count += countLogicalOperators(binaryExpr.getLeft());
            count += countLogicalOperators(binaryExpr.getRight());
        } else {
            for (Expression child : expr.getChildNodesByType(Expression.class)) {
                count += countLogicalOperators(child);
            }
        }
        return count;
    }

    public static int countTernaryOperators(Expression expr) {
        if (expr == null) return 0;
        int count = 0;
        if (expr instanceof ConditionalExpr conditionalExpr) {
            count++;
            count += countTernaryOperators(conditionalExpr.getCondition());
            count += countTernaryOperators(conditionalExpr.getThenExpr());
            count += countTernaryOperators(conditionalExpr.getElseExpr());
        } else {
            for (Expression child : expr.getChildNodesByType(Expression.class)) {
                count += countTernaryOperators(child);
            }
        }
        return count;
    }
}