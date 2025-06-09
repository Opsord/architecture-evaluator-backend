// JavaParserController.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.CustomCompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part.FileInstanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class CompilationUnitController {

    private static final Logger logger = LoggerFactory.getLogger(CompilationUnitController.class);
    private final FileInstanceService fileInstanceService;

    @PostMapping("/parse")
    public CustomCompilationUnit parseJavaFile(@RequestParam String filePath) throws FileNotFoundException {
        logger.info("Received request to parse file: {}", filePath);
        File file = new File(filePath);
        CustomCompilationUnit customCompilationUnit = fileInstanceService.parseJavaFile(file);
        logger.info("Returning parsed AST for file: {}", filePath);
        return customCompilationUnit;
    }
}