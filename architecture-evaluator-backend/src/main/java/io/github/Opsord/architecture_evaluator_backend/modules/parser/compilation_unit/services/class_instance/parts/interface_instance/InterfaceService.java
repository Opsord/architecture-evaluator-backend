// InterfaceService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterfaceService {

    public List<String> getInterfaceNames(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<String> interfaceNames = new ArrayList<>();
        InterfaceVisitor visitor = new InterfaceVisitor();
        classOrInterfaceDeclaration.accept(visitor, interfaceNames);
        return interfaceNames;
    }

    public List<String> getImplementedInterfaces(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<String> implementedInterfaces = new ArrayList<>();
        InterfaceVisitor visitor = new InterfaceVisitor();
        classOrInterfaceDeclaration.accept(visitor, implementedInterfaces);
        return implementedInterfaces;
    }
}