// MethodVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.StatementDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;

import java.util.List;
import java.util.stream.Collectors;

public class MethodVisitor extends VoidVisitorAdapter<List<MethodDTO>> {

    private final StatementService statementService;

    public MethodVisitor(StatementService statementService) {
        this.statementService = statementService;
    }

    // MethodVisitor.java
    @Override
    public void visit(MethodDeclaration method, List<MethodDTO> collector) {
        super.visit(method, collector);
        MethodDTO methodDTO = new MethodDTO();
        // Basic information
        methodDTO.setName(method.getNameAsString());
        methodDTO.setAccessModifier(method.getAccessSpecifier().asString());
        methodDTO.setReturnType(method.getType().asString());

        // All statements
        methodDTO.setStatements(method.getBody().stream()
                .flatMap(body -> body.getStatements().stream())
                .map(statement -> {
                    StatementDTO statementDTO = new StatementDTO();
                    statementDTO.setType(statement.getClass().getSimpleName());
                    statementDTO.setStructure(statement.toString());
                    return statementDTO;
                }).collect(Collectors.toList()));
        // Control statements only
        methodDTO.setControlStatements(method.getBody().stream()
                .flatMap(body -> body.getStatements().stream())
                .filter(this::isControlStatement)
                .map(statement -> {
                    StatementDTO statementDTO = new StatementDTO();
                    statementDTO.setType(statement.getClass().getSimpleName());
                    statementDTO.setStructure(statement.toString());
                    return statementDTO;
                }).collect(Collectors.toList()));
        // Executable statements
        methodDTO.setExecutableStatements(method.getBody().stream()
                .flatMap(body -> body.getStatements().stream())
                .filter(statement -> !isControlStatement(statement) && isExecutableStatement(statement))
                .map(statement -> {
                    StatementDTO statementDTO = new StatementDTO();
                    statementDTO.setType(statement.getClass().getSimpleName());
                    statementDTO.setStructure(statement.toString());
                    return statementDTO;
                }).collect(Collectors.toList()));

        // Inputs
        methodDTO.setInputs(method.getParameters().stream()
                .map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toList()));
        // Outputs
        methodDTO.setOutputs(extractOutputs(method));
        // Lines of code
        methodDTO.setLinesOfCode(method.getBody()
                .map(body -> body.getEnd().map(end -> end.line).orElse(0) - body.getBegin().map(begin -> begin.line).orElse(0) + 1)
                .orElse(0));

        collector.add(methodDTO);
    }

    private boolean isControlStatement(Statement statement) {
        // Define logic to identify control statements (e.g., if, for, while)
        return statement.isIfStmt() || statement.isForStmt() || statement.isWhileStmt() || statement.isSwitchStmt();
    }

    private List<String> extractOutputs(MethodDeclaration method) {
        return method.getBody().stream()
                .flatMap(body -> body.getStatements().stream())
                .filter(Statement::isReturnStmt)
                .map(statement -> statement.asReturnStmt().getExpression()
                        .map(expression -> {
                            if (expression.isLiteralExpr()) {
                                return expression.toString(); // Literales como enteros, cadenas, etc.
                            } else if (expression.isNameExpr()) {
                                return expression.asNameExpr().getNameAsString(); // Variables
                            } else if (expression.isObjectCreationExpr()) {
                                return expression.asObjectCreationExpr().getType().asString(); // Instancias de clases
                            } else {
                                return expression.toString(); // Otros casos
                            }
                        }).orElse("void"))
                .collect(Collectors.toList());
    }

    private boolean isExecutableStatement(Statement statement) {
        // Excluir declaraciones de variables
        return !statement.isExpressionStmt() ||
                (statement.isExpressionStmt() && !statement.asExpressionStmt().getExpression().isVariableDeclarationExpr());
    }
}