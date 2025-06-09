package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.MethodInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MethodService {

    private final StatementService statementService;

    public List<MethodInstance> getMethods(ClassOrInterfaceDeclaration classDeclaration) {
        List<MethodInstance> methods = new ArrayList<>();
        MethodVisitor visitor = new MethodVisitor(statementService);
        classDeclaration.accept(visitor, methods);
        return methods;
    }
}