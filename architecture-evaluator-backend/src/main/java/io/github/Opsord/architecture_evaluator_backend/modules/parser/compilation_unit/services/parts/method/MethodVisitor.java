package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.BasicInfo;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.MethodMetrics;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.Parameters;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.StatementsInfo;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.statement.StatementDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.variable.VariableVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MethodVisitor extends VoidVisitorAdapter<List<MethodDTO>> {

    @Override
    public void visit(MethodDeclaration method, List<MethodDTO> collector) {
        super.visit(method, collector);

        MethodDTO methodDTO = new MethodDTO();
        methodDTO.setName(method.getNameAsString());
        methodDTO.setBasicInfo(populateBasicInfo(method));
        methodDTO.setStatementsInfo(populateStatementInfo(method));
        methodDTO.setParameters(populateParameters(method));
        methodDTO.setMethodMetrics(populateMethodMetrics(method));
        methodDTO.setMethodVariables(extractMethodVariables(method)); // Agregar variables

        collector.add(methodDTO);
    }

    private BasicInfo populateBasicInfo(MethodDeclaration method) {
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setAccessModifier(method.getAccessSpecifier().asString());
        basicInfo.setReturnType(method.getType().asString());
        return basicInfo;
    }

    // Statements
    private final StatementService statementService = new StatementService();
    private StatementsInfo populateStatementInfo(MethodDeclaration method) {
        StatementsInfo statementsInfo = new StatementsInfo();
        List<StatementDTO> statements = statementService.getStatementsFromMethod(method);

        statementsInfo.setStatements(statements);
        statementsInfo.setNumberOfStatements(statements.size());
        statementsInfo.setNumberOfControlStatements(statementService.countControlStatements(statements));
        statementsInfo.setNumberOfExecutableStatements(statementService.countExecutableStatements(statements));
        return statementsInfo;
    }

    private Parameters populateParameters(MethodDeclaration method) {
        Parameters parameters = new Parameters();
        parameters.setInputs(method.getParameters().stream()
                .map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toList()));
        return parameters;
    }

    private MethodMetrics populateMethodMetrics(MethodDeclaration method) {
        MethodMetrics methodMetrics = new MethodMetrics();
        int linesOfCode = method.getBody()
                .map(body -> body.getTokenRange()
                        .map(tokens -> Math.toIntExact(StreamSupport.stream(tokens.spliterator(), false)
                                .filter(token -> !token.toString().startsWith("//") && !token.toString().startsWith("/*"))
                                .map(token -> token.getRange().map(range -> range.begin.line).orElse(0))
                                .distinct()
                                .count()))
                        .orElse(0))
                .orElse(0);
        methodMetrics.setLinesOfCode(linesOfCode);
        return methodMetrics;
    }

    private List<VariableDTO> extractMethodVariables(MethodDeclaration method) {
        List<VariableDTO> variables = new ArrayList<>();
        VariableVisitor visitor = new VariableVisitor();
        method.accept(visitor, variables); // Reutiliza el VariableVisitor
        return variables;
    }
}