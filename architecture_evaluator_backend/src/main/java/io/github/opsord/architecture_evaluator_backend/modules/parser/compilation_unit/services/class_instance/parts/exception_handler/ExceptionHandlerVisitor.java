// ExceptionHandlerVisitor.java
//package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.exception_handler;
//
//import com.github.javaparser.ast.Node;
//import com.github.javaparser.ast.stmt.TryStmt;
//import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
//import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.ExceptionHandlingInstance;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class ExceptionHandlerVisitor extends VoidVisitorAdapter<List<ExceptionHandlingInstance>> {
//
//    @Override
//    public void visit(TryStmt tryStmt, List<ExceptionHandlingInstance> collector) {
//        super.visit(tryStmt, collector);
//        ExceptionHandlingInstance exceptionHandlingInstance = new ExceptionHandlingInstance();
//        exceptionHandlingInstance.setTryBlock(tryStmt.getTryBlock().toString());
//        exceptionHandlingInstance.setCatchBlocks(tryStmt.getCatchClauses().stream()
//                .map(Node::toString)
//                .collect(Collectors.toList()));
//        exceptionHandlingInstance
//                .setFinallyBlock(tryStmt.getFinallyBlock().map(Node::toString).orElse(null));
//        collector.add(exceptionHandlingInstance);
//    }
//}