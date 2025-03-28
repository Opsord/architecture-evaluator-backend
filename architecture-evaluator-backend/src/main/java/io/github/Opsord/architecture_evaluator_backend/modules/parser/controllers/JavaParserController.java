// JavaParserController.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.CompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.services.JavaParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/parser")
public class JavaParserController {

    private static final Logger logger = LoggerFactory.getLogger(JavaParserController.class);
    private final JavaParserService javaParserService;

    public JavaParserController(JavaParserService javaParserService) {
        this.javaParserService = javaParserService;
    }

    @GetMapping("/parse")
    public CompilationUnitDTO parseJavaFile(@RequestParam String filePath) throws FileNotFoundException {
        logger.info("Received request to parse file: {}", filePath);
        File file = new File(filePath);
        CompilationUnitDTO compilationUnitDTO = javaParserService.parseJavaFile(file);
        logger.info("Returning parsed AST for file: {}", filePath);
        return compilationUnitDTO;
    }
}