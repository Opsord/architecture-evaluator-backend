package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class FileInstanceVisitor extends VoidVisitorAdapter<FileInstanceVisitor.FileLevelInfo> {

    @Override
    public void visit(PackageDeclaration pd, FileLevelInfo info) {
        super.visit(pd, info);
        info.packageName = pd.getNameAsString();
    }

    @Override
    public void visit(ImportDeclaration id, FileLevelInfo info) {
        super.visit(id, info);
        info.imports.add(id.getNameAsString());
    }

    public void visit(AnnotationExpr annotation, FileLevelInfo info) {
        // Only collect annotations that are direct children of CompilationUnit
        if (annotation.getParentNode().isPresent() && annotation.getParentNode().get() instanceof CompilationUnit) {
            info.annotations.add(annotation.getNameAsString());
        }
        super.visit(annotation, info);
    }

    public static class FileLevelInfo {
        public List<String> annotations = new ArrayList<>();
        public String packageName = null;
        public List<String> imports = new ArrayList<>();
    }
}