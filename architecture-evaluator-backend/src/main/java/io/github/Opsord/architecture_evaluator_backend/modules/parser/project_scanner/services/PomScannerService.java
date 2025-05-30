package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.DependencyDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.ParentSectionDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
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
public class PomScannerService {

    private static final Logger logger = LoggerFactory.getLogger(PomScannerService.class);
    private final ScannerService scannerService;

    public PomFileDTO scanPomFile(File projectDirectory) {
        if (projectDirectory == null || !projectDirectory.isDirectory()) {
            logger.warn("Invalid project directory: {}", projectDirectory != null ? projectDirectory.getAbsolutePath() : "null");
            return null;
        }

        File pomFile = scannerService.findPomFile(projectDirectory);
        if (pomFile == null) {
            logger.warn("No pom.xml found in directory: {}", projectDirectory.getAbsolutePath());
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile);
            document.getDocumentElement().normalize();

            PomFileDTO pomFileDTO = new PomFileDTO();
            pomFileDTO.setParentSection(getParentInfo(document));
            pomFileDTO.setGroupId(getTagValue(document, "groupId"));
            pomFileDTO.setArtifactId(getTagValue(document, "artifactId"));
            pomFileDTO.setVersion(getTagValue(document, "version"));
            pomFileDTO.setDescription(getTagValue(document, "description"));
            pomFileDTO.setUrl(getTagValue(document, "url"));
            pomFileDTO.setLicense(getLicense(document));
            pomFileDTO.setDevelopers(getDevelopers(document));
            pomFileDTO.setJavaVersion(getTagValue(document, "java.version"));
            pomFileDTO.setDependencies(parseDependencies(document));

            return pomFileDTO;
        } catch (Exception e) {
            logger.error("Failed to parse pom.xml file in directory: {}", projectDirectory.getAbsolutePath(), e);
            return null;
        }
    }

    private List<DependencyDTO> parseDependencies(Document document) {
        List<DependencyDTO> dependencyList = new ArrayList<>();
        NodeList dependencyNodes = document.getElementsByTagName("dependency");

        for (int i = 0; i < dependencyNodes.getLength(); i++) {
            Element dependency = (Element) dependencyNodes.item(i);
            String groupId = getTagValue(dependency, "groupId");
            String artifactId = getTagValue(dependency, "artifactId");
            String version = getTagValue(dependency, "version");

            if (groupId != null && artifactId != null) {
                dependencyList.add(new DependencyDTO(groupId, artifactId, version));
            }
        }

        return dependencyList;
    }

    private ParentSectionDTO getParentInfo(Document document) {
        NodeList parentNodes = document.getElementsByTagName("parent");
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            ParentSectionDTO parentDTO = new ParentSectionDTO();
            parentDTO.setGroupId(getTagValue(parentElement, "groupId"));
            parentDTO.setArtifactId(getTagValue(parentElement, "artifactId"));
            parentDTO.setVersion(getTagValue(parentElement, "version"));
            return parentDTO;
        }
        return null;
    }

    private String getLicense(Document document) {
        NodeList licenses = document.getElementsByTagName("license");
        if (licenses.getLength() > 0) {
            Element license = (Element) licenses.item(0);
            return getTagValue(license, "name");
        }
        return null;
    }

    private List<String> getDevelopers(Document document) {
        List<String> developers = new ArrayList<>();
        NodeList developerNodes = document.getElementsByTagName("developer");

        for (int i = 0; i < developerNodes.getLength(); i++) {
            Element developer = (Element) developerNodes.item(i);
            String name = getTagValue(developer, "name");
            if (name != null) {
                developers.add(name);
            }
        }

        return developers;
    }

    private String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0 && nodeList.item(0).getTextContent() != null) {
            return nodeList.item(0).getTextContent().trim();
        }
        return null;
    }

    private String getTagValue(Document document, String tagName) {
        try {
            NodeList nodes = document.getDocumentElement().getElementsByTagName(tagName);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                if (element.getParentNode().getNodeName().equals("project")) {
                    return element.getTextContent().trim();
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving tag value for: {}", tagName, e);
        }
        return null;
    }
}