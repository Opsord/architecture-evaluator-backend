package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part.PackageService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileInstanceVisitor extends VoidVisitorAdapter<FileInstance> {

    private final PackageService packageService;
    private final AnnotationService annotationService;
    private final ClassService classService;

    @Override
    public void visit(CompilationUnit cu, FileInstance fileInstance) {
        super.visit(cu, fileInstance);

        // --- File Information ---
        fileInstance.setPackageName(
                cu.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("default")
        );

        // --- File-level Annotations & Imports ---
        fileInstance.setImportedPackages(packageService.getImportedPackages(cu));
        fileInstance.setFileAnnotations(
                annotationService.getAnnotationsFromFile(cu)
        );

        // --- Contained Types ---
        fileInstance.setClasses(classService.getClasses(cu));

        // --- Metrics ---
        fileInstance.setLinesOfCode(
                cu.getRange().map(r -> r.end.line - r.begin.line + 1).orElse(0)
        );
        fileInstance.setImportCount(
                fileInstance.getImportedPackages() != null ? fileInstance.getImportedPackages().size() : 0
        );
    }
}