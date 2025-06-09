package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.VariableInstance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VariableService {

    public List<VariableInstance> getVariables(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<VariableInstance> variables = new ArrayList<>();
        VariableVisitor visitor = new VariableVisitor();
        classOrInterfaceDeclaration.accept(visitor, variables);
        return variables;
    }
}