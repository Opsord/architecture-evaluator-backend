// InterfaceService.java
package io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.interface_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterfaceService {

    public List<String> getImplementedInterfaces(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<String> implementedInterfaces = new ArrayList<>();
        InterfaceVisitor visitor = new InterfaceVisitor();
        classOrInterfaceDeclaration.accept(visitor, implementedInterfaces);
        return implementedInterfaces;
    }
}