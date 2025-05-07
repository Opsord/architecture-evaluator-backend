// PackageService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.package_part;

import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PackageService {

    private static final Logger logger = LoggerFactory.getLogger(PackageService.class);

    public List<String> getImportedPackages(CompilationUnit compilationUnit) {
//        logger.info("Extracting imported packages from compilation unit");
        List<String> importedPackages = new ArrayList<>();
        PackageVisitor visitor = new PackageVisitor();
        compilationUnit.accept(visitor, importedPackages);
        return importedPackages;
    }
}