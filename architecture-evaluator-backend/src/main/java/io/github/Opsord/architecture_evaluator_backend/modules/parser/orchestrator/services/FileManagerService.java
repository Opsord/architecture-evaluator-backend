// File: backend/architecture-evaluator-backend/src/main/java/io/github/opsord/architecture_evaluator_backend/modules/parser/orchestrator/services/FileManagerService.java
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
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.Set;

/**
 * Service for managing file operations such as downloading, saving, extracting, and deleting files/directories.
 */
@Service
public class FileManagerService {

    // --- Logger ---
    private static final Logger logger = LoggerFactory.getLogger(FileManagerService.class);

    // === Public API ===

    /**
     * Downloads a GitHub repository as a ZIP file and stores it in a secure temporary directory.
     * @param repoUrl The URL of the GitHub repository.
     * @return The downloaded ZIP file.
     * @throws IOException If an error occurs during download or file creation.
     */
    public File downloadGitHubRepository(String repoUrl) throws IOException {
        // Ensure the URL ends with a slash
        if (!repoUrl.endsWith("/")) {
            repoUrl += "/";
        }
        String zipUrl = repoUrl + "archive/refs/heads/main.zip";

        // Create a secure temporary directory for the ZIP file
        File tempDir = createSecureTempDirectory("githubRepo");
        File zipFile = new File(tempDir, "repo.zip");

        // Download the ZIP file
        try (InputStream in = new URL(zipUrl).openStream();
             OutputStream out = new FileOutputStream(zipFile)) {
            in.transferTo(out);
        }

        return zipFile;
    }

    /**
     * Saves an uploaded file to a secure temporary directory.
     * @param file The uploaded MultipartFile.
     * @return The saved file.
     * @throws IOException If an error occurs during file saving.
     */
    public File saveUploadedFile(MultipartFile file) throws IOException {
        File tempDir = createSecureTempDirectory("uploadedProject");
        File uploadedFile = new File(tempDir, Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(uploadedFile);
        return uploadedFile;
    }

    /**
     * Extracts an archive file (ZIP, TAR, etc.) into a directory.
     * @param archive The archive file to extract.
     * @return The directory containing the extracted files.
     * @throws IOException If extraction fails.
     */
    public File extractArchive(File archive) throws IOException {
        File extractedDir = new File(archive.getParent(), "extracted");
        if (!extractedDir.mkdir()) {
            throw new IOException("Failed to create extraction directory");
        }

        // Open the archive and process each entry
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

    /**
     * Recursively deletes a file or directory and its contents.
     * Uses java.nio.file.Files.delete for better error messages.
     * @param file The file or directory to delete.
     */
    public void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                deleteRecursively(child);
            }
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            logger.warn("Failed to delete file or directory: {}", file.getAbsolutePath(), e);
        }
    }

    // === Private Helpers ===

    /**
     * Processes a single entry from an archive, extracting it to the target directory.
     * Prevents directory traversal attacks by checking canonical paths.
     */
    private void processArchiveEntry(ArchiveEntry entry, InputStream archiveStream, File extractedDir) throws IOException {
        String entryName = entry.getName().replace("\\", "/");
        File outputFile = new File(extractedDir, entryName);
        String canonicalExtractedDir = extractedDir.getCanonicalPath();
        String canonicalOutputFile = outputFile.getCanonicalPath();

        // Security check: ensure the entry is within the extraction directory
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

    /**
     * Creates a secure temporary directory with permissions restricted to the owner.
     * Handles both UNIX (POSIX) and Windows systems.
     * @param prefix The prefix for the directory name.
     * @return The created temporary directory.
     * @throws IOException If directory creation fails.
     */
    private File createSecureTempDirectory(String prefix) throws IOException {
        File tempDir;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            tempDir = Files.createTempDirectory(prefix).toFile();
            // Set permissions and log if any fail
            if (!tempDir.setReadable(true, true)) {
                logger.warn("Failed to set readable permission on temp directory: {}", tempDir.getAbsolutePath());
            }
            if (!tempDir.setWritable(true, true)) {
                logger.warn("Failed to set writable permission on temp directory: {}", tempDir.getAbsolutePath());
            }
            if (!tempDir.setExecutable(true, true)) {
                logger.warn("Failed to set executable permission on temp directory: {}", tempDir.getAbsolutePath());
            }
        } else {
            FileAttribute<Set<PosixFilePermission>> attr =
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            tempDir = Files.createTempDirectory(prefix, attr).toFile();
        }
        return tempDir;
    }
}