package io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

@Service
public class FileManagerService {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerService.class);

    public File downloadGitHubRepository(String repoUrl) throws IOException {
        // Build the URL for the ZIP file of the main branch
        if (!repoUrl.endsWith("/")) {
            repoUrl += "/";
        }
        String zipUrl = repoUrl + "archive/refs/heads/main.zip";

        // Create a temporary directory to store the downloaded ZIP file
        File tempDir = Files.createTempDirectory("githubRepo").toFile();
        File zipFile = new File(tempDir, "repo.zip");

        // Download the ZIP file
        try (InputStream in = new URL(zipUrl).openStream();
                OutputStream out = new FileOutputStream(zipFile)) {
            in.transferTo(out);
        }

        return zipFile;
    }

    public File saveUploadedFile(MultipartFile file) throws IOException {
        File tempDir = Files.createTempDirectory("uploadedProject").toFile();
        File uploadedFile = new File(tempDir, Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(uploadedFile);
        return uploadedFile;
    }

    public File extractArchive(File archive) throws IOException {
        File extractedDir = new File(archive.getParent(), "extracted");
        if (!extractedDir.mkdir()) {
            throw new IOException("Failed to create extraction directory");
        }

        try (InputStream fis = new FileInputStream(archive);
             BufferedInputStream bis = new BufferedInputStream(fis);
             var archiveStream = new ArchiveStreamFactory().createArchiveInputStream(bis)) {

            ArchiveEntry entry;
            while ((entry = archiveStream.getNextEntry()) != null) {
                processArchiveEntry(entry, archiveStream, extractedDir);
            }
        } catch (Exception e) {
            throw new IOException("Error extracting archive: " + archive.getName(), e);
        }

        return extractedDir;
    }

    private void processArchiveEntry(ArchiveEntry entry, InputStream archiveStream, File extractedDir) throws IOException {
        String entryName = entry.getName().replace("\\", "/");
        File outputFile = new File(extractedDir, entryName);
        String canonicalExtractedDir = extractedDir.getCanonicalPath();
        String canonicalOutputFile = outputFile.getCanonicalPath();

        if (!canonicalOutputFile.startsWith(canonicalExtractedDir + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + entry.getName());
        }

        if (entry.isDirectory()) {
            if (!outputFile.exists() && !outputFile.mkdirs()) {
                throw new IOException("Failed to create directory: " + outputFile.getAbsolutePath());
            }
        } else {
            File parent = outputFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Failed to create parent directory: " + parent.getAbsolutePath());
            }
            try (OutputStream os = Files.newOutputStream(outputFile.toPath())) {
                archiveStream.transferTo(os);
            }
        }
    }

    public void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                deleteRecursively(child);
            }
        }
        if (!file.delete()) {
            logger.warn("Failed to delete file or directory: {}", file.getAbsolutePath());
        }
    }
}