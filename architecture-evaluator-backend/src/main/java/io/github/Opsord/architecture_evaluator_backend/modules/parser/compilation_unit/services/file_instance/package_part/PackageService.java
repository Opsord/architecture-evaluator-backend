// PackageService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part;

import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PackageService {

    public List<String> getImportedPackages(CompilationUnit compilationUnit) {
        List<String> importedPackages = new ArrayList<>();
        PackageVisitor visitor = new PackageVisitor();
        compilationUnit.accept(visitor, importedPackages);
        return importedPackages;
    }
}