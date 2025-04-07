package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.variable;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;

import java.util.List;

public class VariableVisitor extends VoidVisitorAdapter<List<VariableDTO>> {

    @Override
    public void visit(VariableDeclarator variable, List<VariableDTO> collector) {
        super.visit(variable, collector);
        VariableDTO variableDTO = new VariableDTO();
        variableDTO.setName(variable.getNameAsString());
        variableDTO.setType(variable.getType().asString());
        collector.add(variableDTO);
    }
}