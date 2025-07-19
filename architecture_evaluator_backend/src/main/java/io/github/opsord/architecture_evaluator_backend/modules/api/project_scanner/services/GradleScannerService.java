package io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services;

import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.gradle.GradleDependencyInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.gradle.GradleFileInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GradleScannerService {

    private static final Logger logger = LoggerFactory.getLogger(GradleScannerService.class);

    private static final List<String> GRADLE_FILES = List.of("build.gradle", "build.gradle.kts");

    public Optional<GradleFileInstance> scanGradleFile(File projectDirectory) {
        if (projectDirectory == null || !projectDirectory.isDirectory()) {
            logger.warn("Invalid project directory: {}", projectDirectory != null ? projectDirectory.getAbsolutePath() : "null");
            return Optional.empty();
        }

        File gradleFile = findGradleFile(projectDirectory);
        if (gradleFile == null) {
            logger.warn("No build.gradle or build.gradle.kts found in directory: {}", projectDirectory.getAbsolutePath());
            return Optional.empty();
        }

        try {
            List<String> lines = Files.readAllLines(gradleFile.toPath());
            GradleFileInstance gradleFileInstance = new GradleFileInstance();
            gradleFileInstance.setGroup(extractProperty(lines, "group"));
            gradleFileInstance.setName(extractProperty(lines, "rootProject.name"));
            gradleFileInstance.setVersion(extractProperty(lines, "version"));
            gradleFileInstance.setDescription(extractProperty(lines, "description"));
            gradleFileInstance.setJavaVersion(extractJavaVersion(lines));
            gradleFileInstance.setDependencies(extractDependencies(lines));
            return Optional.of(gradleFileInstance);
        } catch (IOException e) {
            logger.error("Error reading gradle file: {}", gradleFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    private File findGradleFile(File dir) {
        for (String fileName : GRADLE_FILES) {
            File file = new File(dir, fileName);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private String extractProperty(List<String> lines, String property) {
        Pattern pattern = Pattern.compile(property + "\\s*=\\s*['\"]([^'\"]+)['\"]");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private String extractJavaVersion(List<String> lines) {
        Pattern pattern1 = Pattern.compile("sourceCompatibility\\s*=\\s*['\"]?([\\w.]+)['\"]?");
        Pattern pattern2 = Pattern.compile("sourceCompatibility\\s*=\\s*JavaVersion\\.VERSION_(\\d+)");
        for (String line : lines) {
            Matcher m1 = pattern1.matcher(line.trim());
            if (m1.find()) return m1.group(1);
            Matcher m2 = pattern2.matcher(line.trim());
            if (m2.find()) return m2.group(1);
        }
        return null;
    }

    private List<GradleDependencyInstance> extractDependencies(List<String> lines) {
        List<GradleDependencyInstance> dependencies = new ArrayList<>();
        Pattern depPattern = Pattern.compile(
                "(implementation|api|compile|testImplementation|runtimeOnly|compileOnly|annotationProcessor|testRuntimeOnly)\\s+['\"]([^:'\"]+):([^:'\"]+):([^'\"]+)['\"]"
        );
        for (String line : lines) {
            Matcher matcher = depPattern.matcher(line.trim());
            if (matcher.find()) {
                dependencies.add(new GradleDependencyInstance(
                        matcher.group(2), matcher.group(3), matcher.group(4)
                ));
            }
        }
        return dependencies;
    }
}