// InterfaceService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.interface_part;

import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public List<String> getInterfaceNames(CompilationUnit compilationUnit) {
        logger.info("Extracting interface names from compilation unit");
        List<String> interfaceNames = new ArrayList<>();
        InterfaceVisitor visitor = new InterfaceVisitor();
        compilationUnit.accept(visitor, interfaceNames);
        return interfaceNames;
    }

    public List<String> getImplementedInterfaces(CompilationUnit compilationUnit) {
        logger.info("Extracting implemented interfaces from compilation unit");
        List<String> implementedInterfaces = new ArrayList<>();
        InterfaceVisitor visitor = new InterfaceVisitor();
        compilationUnit.accept(visitor, implementedInterfaces);
        return implementedInterfaces;
    }
}