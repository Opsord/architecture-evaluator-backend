package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.SummarizedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.CCUSummarizingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/parser-detailed")
@RequiredArgsConstructor
public class DetailingController {

    private static final Logger logger = LoggerFactory.getLogger(DetailingController.class);
    private final CompilationUnitService compilationUnitService;
    private final CCUSummarizingService summarizingService;

    @PostMapping("/parse")
    public SummarizedCompUnitDTO parseJavaFile(@RequestParam String filePath) throws FileNotFoundException {
        logger.info("Received request to parse with details file: {}", filePath);
        File file = new File(filePath);
        CustomCompilationUnitDTO customCompilationUnitDTO = compilationUnitService.parseJavaFile(file);
        logger.info("Generating detailed compilation unit for file: {}", filePath);
        return summarizingService.generateSummarizedCompUnit(customCompilationUnitDTO);
    }
}