// GenericUsageService.java
//package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.generic_usage;
//
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.GenericUsageInstance;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class GenericUsageService {
//
//    public List<GenericUsageInstance> getGenericUsages(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
//        List<GenericUsageInstance> genericUsages = new ArrayList<>();
//        GenericUsageVisitor visitor = new GenericUsageVisitor();
//        classOrInterfaceDeclaration.accept(visitor, genericUsages);
//        return genericUsages;
//    }
//}