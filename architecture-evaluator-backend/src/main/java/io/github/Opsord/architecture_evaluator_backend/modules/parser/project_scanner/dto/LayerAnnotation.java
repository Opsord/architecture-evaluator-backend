package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto;

import lombok.Getter;

@Getter
public enum LayerAnnotation {
    ENTITY("Entity"),
    DOCUMENT("Document"),
    REPOSITORY("Repository"),
    SERVICE("Service"),
    CONTROLLER("Controller");

    private final String annotation;

    LayerAnnotation(String annotation) {
        this.annotation = annotation;
    }
}