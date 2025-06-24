package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part.PackageService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.interface_instance.InterfaceService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FileInstanceVisitor extends VoidVisitorAdapter<FileInstance> {

        private final PackageService packageService;
        private final AnnotationService annotationService;
        private final InterfaceService interfaceService;
        private final ClassService classService;

        @Override
        public void visit(CompilationUnit compilationUnit, FileInstance fileInstance) {
                super.visit(compilationUnit, fileInstance);

                // --- File Information ---
                fileInstance.setPackageName(
                        compilationUnit.getPackageDeclaration().map(NodeWithName::getNameAsString)
                                .orElse("default"));

                // --- File-level Annotations & Imports ---
                fileInstance.setImportedPackages(packageService.getImportedPackages(compilationUnit));
                fileInstance.setFileAnnotations(
                        annotationService.getAnnotationsFromCompUnit(compilationUnit));

                // --- Contained Types ---
                fileInstance.setJavaTypeInstance(
                        collectJavaTypeInstances(compilationUnit)
                );

                // --- Metrics ---
                fileInstance.setLinesOfCode(
                        compilationUnit.getRange().map(r -> r.end.line - r.begin.line + 1).orElse(0));
                fileInstance.setImportCount(
                        fileInstance.getImportedPackages() != null ? fileInstance.getImportedPackages().size() : 0);
        }

        /**
         * Collects all JavaTypeInstance objects from a CompilationUnit.
         */
        public List<JavaTypeInstance> collectJavaTypeInstances(CompilationUnit compilationUnit) {
            List<JavaTypeInstance> javaTypeInstances = new ArrayList<>();

            // Collect all type declarations
            List<ClassOrInterfaceDeclaration> typeDeclarations =
                    compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            // Separate by type
            List<ClassOrInterfaceDeclaration> classDecls = new ArrayList<>();
            List<ClassOrInterfaceDeclaration> interfaceDecls = new ArrayList<>();

            for (var decl : typeDeclarations) {
                if (decl.isInterface()) {
                    interfaceDecls.add(decl);
                } else {
                    classDecls.add(decl);
                }
            }

            // Parse classes
            classService.getClassesFromCompUnit(compilationUnit).stream()
                    .filter(cls -> classDecls.stream().anyMatch(d -> d.getNameAsString().equals(cls.getName())))
                    .forEach(cls -> {
                        JavaTypeInstance jti = new JavaTypeInstance();
                        jti.setType(cls.getJavaFileType());
                        jti.setContent(cls);
                        javaTypeInstances.add(jti);
                    });

            // Parse interfaces
            interfaceService.getInterfacesFromFile(compilationUnit).stream()
                    .filter(interfaceInstance -> interfaceDecls.stream().anyMatch(d -> d.getNameAsString().equals(interfaceInstance.getName())))
                    .forEach(interfaceInstance -> {
                        JavaTypeInstance javaTypeInstance = new JavaTypeInstance();
                        javaTypeInstance.setType(JavaFileType.INTERFACE);
                        javaTypeInstance.setContent(interfaceInstance);
                        javaTypeInstances.add(javaTypeInstance);
                    });

            return javaTypeInstances;
        }
}