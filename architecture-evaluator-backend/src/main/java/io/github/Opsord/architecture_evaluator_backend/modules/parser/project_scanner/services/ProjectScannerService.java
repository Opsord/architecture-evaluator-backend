package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.PomFileDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectScannerService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectScannerService.class);
    private final CompilationUnitService compilationUnitService;

    public List<CustomCompilationUnitDTO> scanProject(String filePath) throws IOException {
        File projectRoot = findProjectRoot(new File(filePath));
        if (projectRoot == null) {
            logger.warn("No valid project root found for path: {}", filePath);
            return List.of();
        }

        File srcFolder = new File(projectRoot, "src");
        if (!srcFolder.exists() || !srcFolder.isDirectory()) {
            logger.warn("No 'src' folder found in project root: {}", projectRoot.getAbsolutePath());
            return List.of();
        }

        List<CustomCompilationUnitDTO> compilationUnits = scanSrcFolder(srcFolder);

        logger.info("Project scanning completed. Found {} Java files and a pom.xml file.", compilationUnits.size());
        return compilationUnits;
    }

    private File findProjectRoot(File file) {
        File current = file;
        while (current != null) {
            if (new File(current, "pom.xml").exists() && new File(current, "src").exists()) {
                logger.info("Project root found: {}", current.getAbsolutePath());
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private List<CustomCompilationUnitDTO> scanSrcFolder(File srcFolder) throws IOException {
        List<File> javaFiles = new ArrayList<>();
        try (var paths = java.nio.file.Files.walk(srcFolder.toPath())) {
            paths.filter(java.nio.file.Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }

        List<CustomCompilationUnitDTO> compilationUnits = new ArrayList<>();
        for (File javaFile : javaFiles) {
            try {
                logger.info("Parsing file: {}", javaFile.getAbsolutePath());
                CustomCompilationUnitDTO dto = compilationUnitService.parseJavaFile(javaFile);
                compilationUnits.add(dto);
            } catch (Exception e) {
                logger.error("Failed to parse file: {}", javaFile.getAbsolutePath(), e);
            }
        }
        return compilationUnits;
    }

    public PomFileDTO scanPomFile(String projectPath) {
        File pomFile = new File(projectPath, "pom.xml");
        if (!pomFile.exists()) {
            logger.warn("No pom.xml file found in project path: {}", projectPath);
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile);

            PomFileDTO pomFileDTO = new PomFileDTO();
            pomFileDTO.setGroupId(getTagValue(document, "groupId"));
            pomFileDTO.setArtifactId(getTagValue(document, "artifactId"));
            pomFileDTO.setVersion(getTagValue(document, "version"));

            NodeList dependencies = document.getElementsByTagName("dependency");
            List<String> dependencyList = new ArrayList<>();
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dependency = (Element) dependencies.item(i);
                String groupId = getTagValue(dependency, "groupId");
                String artifactId = getTagValue(dependency, "artifactId");
                dependencyList.add(groupId + ":" + artifactId);
            }
            pomFileDTO.setDependencies(dependencyList);

            return pomFileDTO;
        } catch (Exception e) {
            logger.error("Failed to parse pom.xml file at path: {}", projectPath, e);
            return null;
        }
    }

    private String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private String getTagValue(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}