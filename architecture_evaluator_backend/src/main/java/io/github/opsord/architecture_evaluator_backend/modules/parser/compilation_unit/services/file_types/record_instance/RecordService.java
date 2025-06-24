package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.record_instance;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.RecordDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecordService {

    public List<String> getRecordsFromComUnit(CompilationUnit compilationUnit) {
        List<String> records = new ArrayList<>();
        RecordVisitor visitor = new RecordVisitor();
        compilationUnit.accept(visitor, records);
        return records;
    }

    public List<String> getRecordsFromRecord(RecordDeclaration recordDeclaration) {
        List<String> records = new ArrayList<>();
        RecordVisitor visitor = new RecordVisitor();
        recordDeclaration.accept(visitor, records);
        return records;
    }
}