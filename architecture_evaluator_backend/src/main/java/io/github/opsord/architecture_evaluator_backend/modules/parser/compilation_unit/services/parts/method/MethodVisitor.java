package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.ParameterInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.VariableInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.BasicInfo;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.MethodMetrics;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.StatementsInfo;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.variable.VariableVisitor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class MethodVisitor extends VoidVisitorAdapter<List<MethodInstance>> {

    private final StatementService statementService;

    @Override
    public void visit(MethodDeclaration method, List<MethodInstance> collector) {
        super.visit(method, collector);

        MethodInstance methodInstance = new MethodInstance();
        methodInstance.setName(method.getNameAsString());
        methodInstance.setBasicInfo(populateBasicInfo(method));
        methodInstance.setStatementsInfo(populateStatementInfo(method));
        methodInstance.setInputParameters(populateInputParameters(method));
        methodInstance.setOutputParameters(populateOutputParameters(method));
        methodInstance.setMethodMetrics(populateMethodMetrics(method));
        methodInstance.setMethodVariables(extractMethodVariables(method));

        collector.add(methodInstance);
    }

    private BasicInfo populateBasicInfo(MethodDeclaration method) {
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setAccessModifier(method.getAccessSpecifier().asString());
        basicInfo.setReturnType(method.getType().asString());
        return basicInfo;
    }

    private StatementsInfo populateStatementInfo(MethodDeclaration method) {
        return statementService.getStatementsInfoFromMethod(method);
    }

    private List<ParameterInstance> populateInputParameters(MethodDeclaration method) {
        return method.getParameters().stream().map(p -> {
            ParameterInstance param = new ParameterInstance();
            param.setName(p.getNameAsString());
            param.setType(p.getType().asString());
            param.setAnnotations(
                    p.getAnnotations().stream().map(NodeWithName::getNameAsString).toList());
            return param;
        }).toList();
    }

    private List<ParameterInstance> populateOutputParameters(MethodDeclaration method) {
        List<ParameterInstance> outputs = new ArrayList<>();
        if (!method.getType().isVoidType()) {
            ParameterInstance output = new ParameterInstance();
            output.setName("return");
            output.setType(method.getType().asString());
            output.setAnnotations(new ArrayList<>());
            outputs.add(output);
        }
        return outputs;
    }

    private MethodMetrics populateMethodMetrics(MethodDeclaration method) {
        MethodMetrics methodMetrics = new MethodMetrics();
        int linesOfCode = method.getBody()
                .map(body -> body.getTokenRange()
                        .map(tokens -> Math.toIntExact(StreamSupport.stream(tokens.spliterator(), false)
                                .filter(token -> !token.toString().startsWith("//")
                                        && !token.toString().startsWith("/*"))
                                .map(token -> token.getRange().map(range -> range.begin.line).orElse(0))
                                .distinct()
                                .count()))
                        .orElse(0))
                .orElse(0);
        methodMetrics.setLinesOfCode(linesOfCode);
        return methodMetrics;
    }

    private List<VariableInstance> extractMethodVariables(MethodDeclaration method) {
        List<VariableInstance> variables = new ArrayList<>();
        VariableVisitor visitor = new VariableVisitor();
        method.accept(visitor, variables);
        return variables;
    }
}