package com.ddnconsulting.fileprocessor.workers;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.ddnconsulting.fileprocessor.core.FileClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Prints a listing of files in a directory to the log (for lack of a better place).
 *
 * @author Dan Nathanson
 */
public class DirectoryLister implements FileWorker {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryLister.class);


    /**
     * List all files in a directory, distinguishing between files and subdirectories
     *
     * @param filePath the file to process.
     * @return true if file processed successfully
     */
    @Override
    public FileWorkerResults handle(Path filePath) {
        DirectoryListResults results = new DirectoryListResults();
        results.setSuccess(true);

        results.setFilename(filePath.toString());
        try {
            File[] files = filePath.toFile().listFiles();
            if (files == null) {
                results.setErrorMessage("could not read contents");
                results.setSuccess(false);
            }
            else  {
                List<DirectoryEntry> directoryEntries = new ArrayList<>();
                results.setEntries(directoryEntries);
                for (File file : files) {
                    DirectoryEntry entry = new DirectoryEntry();
                    entry.setFilename(file.getName());
                    try {
                        if (file.isDirectory()) {
                            entry.setType(FileType.DIRECTORY);
                        }
                        else {
                            entry.setType(FileType.FILE);
                        }
                    }
                    catch (SecurityException e) {
                        entry.setType(FileType.PERMISSION_DENIED);
                    }
                    directoryEntries.add(entry);
                }
            }
        }
        catch (SecurityException e) {
            results.setErrorMessage("permission denied");
            results.setSuccess(false);
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
        return FileClassifier.FILE_TYPE_DIRECTORY.equalsIgnoreCase(type) &&
               "dir".equalsIgnoreCase(operation);
    }

    public static final class DirectoryListResults extends FileWorkerResults {
        private String filename;
        private List<DirectoryEntry> entries;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public List<DirectoryEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<DirectoryEntry> entries) {
            this.entries = entries;
        }
    }

    public static final class DirectoryEntry {
        String filename;
        FileType type;

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public void setType(FileType type) {
            this.type = type;
        }

        public String getFilename() {
            return filename;
        }

        public FileType getType() {
            return type;
        }
    }

    public static enum FileType {
        FILE,
        DIRECTORY,
        PERMISSION_DENIED
    }

}
