// ExceptionHandlerService.java
//package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.exception_handler;
//
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.ExceptionHandlingInstance;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class ExceptionHandlerService {
//
//    public List<ExceptionHandlingInstance> getExceptionHandling(
//            ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
//        List<ExceptionHandlingInstance> exceptionHandling = new ArrayList<>();
//        ExceptionHandlerVisitor visitor = new ExceptionHandlerVisitor();
//        classOrInterfaceDeclaration.accept(visitor, exceptionHandling);
//        return exceptionHandling;
//    }
//}