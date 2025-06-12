package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.ConstructorInstance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConstructorService {

    public List<ConstructorInstance> getConstructors(ClassOrInterfaceDeclaration declaration) {
        List<ConstructorInstance> constructors = new ArrayList<>();
        ConstructorVisitor visitor = new ConstructorVisitor();
        declaration.accept(visitor, constructors);
        return constructors;
    }
}