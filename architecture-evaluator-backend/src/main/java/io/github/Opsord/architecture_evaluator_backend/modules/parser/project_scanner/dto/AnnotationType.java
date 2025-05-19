package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto;

import lombok.Getter;

@Getter
public enum AnnotationType {
    ENTITY("Entity"),
    DOCUMENT("Document"),
    REPOSITORY("Repository"),
    SERVICE("Service"),
    CONTROLLER("Controller"),
    SPRINGBOOT_TEST("SpringBootTest");

    private final String annotation;

    AnnotationType(String annotation) {
        this.annotation = annotation;
    }
}