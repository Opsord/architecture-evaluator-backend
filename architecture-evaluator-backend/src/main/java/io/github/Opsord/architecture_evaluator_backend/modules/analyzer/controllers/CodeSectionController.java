package io.github.Opsord.architecture_evaluator_backend.modules.analyzer.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.analyzer.services.CodeSectionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeSectionController {
    // CodeSectionService injection
    private final CodeSectionService codeSectionService;
    public CodeSectionController(CodeSectionService codeSectionService) {
        this.codeSectionService = codeSectionService;
    }



}
