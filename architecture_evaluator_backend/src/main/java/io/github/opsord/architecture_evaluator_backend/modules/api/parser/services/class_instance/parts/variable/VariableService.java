package io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.variable;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.VariableInstance;
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