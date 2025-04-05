// PackageService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts;

import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageService {

    private static final Logger logger = LoggerFactory.getLogger(PackageService.class);

    public List<String> getImportedPackages(CompilationUnit compilationUnit) {
        logger.info("Extracting imported packages from compilation unit");
        return compilationUnit.getImports().stream()
                .map(importDeclaration -> importDeclaration.getName().asString())
                .collect(Collectors.toList());
    }
}