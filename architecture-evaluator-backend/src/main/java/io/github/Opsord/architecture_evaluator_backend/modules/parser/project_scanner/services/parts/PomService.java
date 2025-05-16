package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.PomFileDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScanningService;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PomService {

    private static final Logger logger = LoggerFactory.getLogger(PomService.class);
    private final ScanningService scanningService;

    public PomFileDTO scanPomFile(String projectPath) {
        File pomFile = scanningService.findPomFile(projectPath);
        if (pomFile == null) {
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