// MethodVisitor.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.ParameterDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.control_statement.ControlStatementService;

import java.util.List;
import java.util.stream.Collectors;

public class MethodVisitor extends VoidVisitorAdapter<List<MethodDTO>> {

    private final ControlStatementService controlStatementService;

    public MethodVisitor(ControlStatementService controlStatementService) {
        this.controlStatementService = controlStatementService;
    }

    @Override
    public void visit(MethodDeclaration method, List<MethodDTO> collector) {
        super.visit(method, collector);
        MethodDTO methodDTO = new MethodDTO();
        methodDTO.setName(method.getNameAsString());
        methodDTO.setAccessModifier(method.getAccessSpecifier().asString());
        methodDTO.setReturnType(method.getType().asString());
        methodDTO.setParameters(method.getParameters().stream()
                .map(p -> {
                    ParameterDTO parameterDTO = new ParameterDTO();
                    parameterDTO.setName(p.getNameAsString());
                    parameterDTO.setType(p.getType().asString());
                    return parameterDTO;
                }).collect(Collectors.toList()));
        methodDTO.setControlStatements(controlStatementService.getControlStatementsFromMethod(method));
        collector.add(methodDTO);
    }
}