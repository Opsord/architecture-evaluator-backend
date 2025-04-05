// InterfaceService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceService.class);

    public List<String> getInterfaceNames(CompilationUnit compilationUnit) {
        logger.info("Extracting interface names from compilation unit");
        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(ClassOrInterfaceDeclaration::isInterface)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .collect(Collectors.toList());
    }

    public List<String> getImplementedInterfaces(CompilationUnit compilationUnit) {
        logger.info("Extracting implemented interfaces from compilation unit");
        return compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .flatMap(c -> c.getImplementedTypes().stream())
                .map(ClassOrInterfaceType::getNameAsString)
                .collect(Collectors.toList());
    }
}