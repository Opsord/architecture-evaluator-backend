package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.VariableInstance;

import java.util.List;

public class VariableVisitor extends VoidVisitorAdapter<List<VariableInstance>> {

    @Override
    public void visit(VariableDeclarator variable, List<VariableInstance> collector) {
        super.visit(variable, collector);
        VariableInstance variableInstance = new VariableInstance();
        variableInstance.setName(variable.getNameAsString());
        variableInstance.setType(variable.getType().asString());

        // Distinguish between instance and local variables
        var parentNodeOpt = variable.getParentNode();
        if (parentNodeOpt.isPresent() && parentNodeOpt.get() instanceof FieldDeclaration) {
            variableInstance.setScope("instance");
        } else {
            variableInstance.setScope("local");
        }

        collector.add(variableInstance);
    }
}