package com.ddnconsulting.fileprocessor.workers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Prints a listing of files in a JAR archive to the log (for lack of a better place).   A more realistic FileWorker
 * for JAR files (or any compressed archive) would be to recurse into the archive applying FileWorkers to the files
 * found inside.
 *
 * @author Dan Nathanson
 */
public class JarFileContentsLister implements FileWorker {
    private static final Logger LOG = LoggerFactory.getLogger(JarFileContentsLister.class);


    /**
     * Handle a file.  It is recommended that long running operations are performed asynchronously.
     *
     * @param filePath the file to process.
     * @return true if file processed successfully
     */
    @Override
    public FileWorkerResults handle(Path filePath) {
        JarContentsResults results = new JarContentsResults();
        results.setFilename(filePath.toString());
        results.setSuccess(true);

        try {
            ZipInputStream zip = new ZipInputStream(Files.newInputStream(filePath));
            List<String> files = new ArrayList<>();

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                files.add(entry.getName());
            }

            results.setFiles(files);
        }
        catch (IOException e) {
            results.setErrorMessage("Could not open file");
            LOG.error("Failed to list contents of JAR file [" + filePath + "]", e);
        }

        return results;
    }

    /**
     * Returns true iff:
     *  operation == "dir"
     *  type = "application/java-archive"
     *
     * @param operation operation to perform
     * @param type the type of the file as specified MIME spec (RFC-2045)
     * @return true iff this worker can perform the specified operation for the given file type
     */
    @Override
    public boolean canHandle(String operation, String type) {
        return "application/java-archive".equalsIgnoreCase(type) &&
               "dir".equalsIgnoreCase(operation);
    }

    public static final class JarContentsResults extends FileWorkerResults {
        private String filename;
        private List<String> files;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public List<String> getFiles() {
            return files;
        }

        public void setFiles(List<String> files) {
            this.files = files;
        }
    }

}
