package io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services;

import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom.PomDependencyInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom.ParentSectionDTO;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom.PomFileInstance;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for scanning and parsing Maven pom.xml files.
 */
@Service
@RequiredArgsConstructor
public class PomScannerService {

    // XML Tag Constants
    private static final String TAG_GROUP_ID = "groupId";
    private static final String TAG_ARTIFACT_ID = "artifactId";
    private static final String TAG_VERSION = "version";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_URL = "url";
    private static final String TAG_LICENSE = "license";
    private static final String TAG_DEVELOPER = "developer";
    private static final String TAG_NAME = "name";
    private static final String TAG_JAVA_VERSION = "java.version";
    private static final String TAG_DEPENDENCY = "dependency";
    private static final String TAG_PARENT = "parent";

    // XML Security Feature Constants
    private static final String FEATURE_DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    private static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    private static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private static final Logger logger = LoggerFactory.getLogger(PomScannerService.class);
    private final ScannerService scannerService;

    /**
     * Scans and parses the pom.xml file in the given project directory.
     *
     * @param projectDirectory the directory containing the pom.xml
     * @return Optional containing PomFileInstance if parsing succeeds, otherwise empty
     */
    public Optional<PomFileInstance> scanPomFile(File projectDirectory) {
        if (projectDirectory == null || !projectDirectory.isDirectory()) {
            logger.warn("Invalid project directory: {}",
                    projectDirectory != null ? projectDirectory.getAbsolutePath() : "null");
            return Optional.empty();
        }

        File pomFile = scannerService.findPomFile(projectDirectory);
        if (pomFile == null) {
            logger.warn("No pom.xml found in directory: {}", projectDirectory.getAbsolutePath());
            return Optional.empty();
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(FEATURE_DISALLOW_DOCTYPE, true);
            factory.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            factory.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
            factory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile);
            document.getDocumentElement().normalize();

            PomFileInstance pomFileInstance = new PomFileInstance();
            pomFileInstance.setParentSection(getParentInfo(document));
            pomFileInstance.setGroupId(getTagValue(document, TAG_GROUP_ID));
            pomFileInstance.setArtifactId(getTagValue(document, TAG_ARTIFACT_ID));
            pomFileInstance.setVersion(getTagValue(document, TAG_VERSION));
            pomFileInstance.setDescription(getTagValue(document, TAG_DESCRIPTION));
            pomFileInstance.setUrl(getTagValue(document, TAG_URL));
            pomFileInstance.setLicense(getLicense(document));
            pomFileInstance.setDevelopers(getDevelopers(document));
            pomFileInstance.setJavaVersion(getTagValue(document, TAG_JAVA_VERSION));
            pomFileInstance.setDependencies(parseDependencies(document));

            return Optional.of(pomFileInstance);
        } catch (ParserConfigurationException e) {
            logger.error("Parser configuration error while parsing pom.xml in directory: {}", projectDirectory.getAbsolutePath(), e);
        } catch (SAXException e) {
            logger.error("SAX error while parsing pom.xml in directory: {}", projectDirectory.getAbsolutePath(), e);
        } catch (IOException e) {
            logger.error("IO error while parsing pom.xml in directory: {}", projectDirectory.getAbsolutePath(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while parsing pom.xml in directory: {}", projectDirectory.getAbsolutePath(), e);
        }
        return Optional.empty();
    }

    /**
     * Parses the dependencies from the given XML document.
     *
     * @param document the XML document representing the pom.xml
     * @return a list of PomDependencyInstance objects
     */
    private List<PomDependencyInstance> parseDependencies(Document document) {
        List<PomDependencyInstance> dependencyList = new ArrayList<>();
        NodeList dependencyNodes = document.getElementsByTagName(TAG_DEPENDENCY);

        for (int i = 0; i < dependencyNodes.getLength(); i++) {
            Element dependency = (Element) dependencyNodes.item(i);
            String groupId = getTagValue(dependency, TAG_GROUP_ID);
            String artifactId = getTagValue(dependency, TAG_ARTIFACT_ID);
            String version = getTagValue(dependency, TAG_VERSION);

            if (groupId != null && artifactId != null) {
                dependencyList.add(new PomDependencyInstance(groupId, artifactId, version));
            }
        }

        return dependencyList;
    }

    /**
     * Extracts the parent section information from the pom.xml document.
     *
     * @param document the XML document representing the pom.xml
     * @return a ParentSectionDTO with parent info, or null if not present
     */
    private ParentSectionDTO getParentInfo(Document document) {
        NodeList parentNodes = document.getElementsByTagName(TAG_PARENT);
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            ParentSectionDTO parentDTO = new ParentSectionDTO();
            parentDTO.setGroupId(getTagValue(parentElement, TAG_GROUP_ID));
            parentDTO.setArtifactId(getTagValue(parentElement, TAG_ARTIFACT_ID));
            parentDTO.setVersion(getTagValue(parentElement, TAG_VERSION));
            return parentDTO;
        }
        return null;
    }

    /**
     * Extracts the license name from the pom.xml document.
     *
     * @param document the XML document representing the pom.xml
     * @return the license name or null if not present
     */
    private String getLicense(Document document) {
        NodeList licenses = document.getElementsByTagName(TAG_LICENSE);
        if (licenses.getLength() > 0) {
            Element license = (Element) licenses.item(0);
            return getTagValue(license, TAG_NAME);
        }
        return null;
    }

    /**
     * Extracts the list of developer names from the pom.xml document.
     *
     * @param document the XML document representing the pom.xml
     * @return a list of developer names
     */
    private List<String> getDevelopers(Document document) {
        List<String> developers = new ArrayList<>();
        NodeList developerNodes = document.getElementsByTagName(TAG_DEVELOPER);

        for (int i = 0; i < developerNodes.getLength(); i++) {
            Element developer = (Element) developerNodes.item(i);
            String name = getTagValue(developer, TAG_NAME);
            if (name != null) {
                developers.add(name);
            }
        }

        return developers;
    }

    /**
     * Retrieves the value of a tag from a given XML element.
     *
     * @param element the XML element
     * @param tagName the tag name to search for
     * @return the tag value, or null if not found
     */
    private String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0 && nodeList.item(0).getTextContent() != null) {
            return nodeList.item(0).getTextContent().trim();
        }
        return null;
    }

    /**
     * Retrieves the value of a tag from the root of the XML document.
     *
     * @param document the XML document
     * @param tagName the tag name to search for
     * @return the tag value, or null if not found
     */
    private String getTagValue(Document document, String tagName) {
        try {
            NodeList nodes = document.getDocumentElement().getElementsByTagName(tagName);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                if ("project".equals(element.getParentNode().getNodeName())) {
                    String value = element.getTextContent();
                    if (value != null) {
                        return value.trim();
                    }
                }
            }
        } catch (Exception exception) {
            logger.error("Error retrieving tag value for: {}", tagName, exception);
        }
        return null;
    }
}