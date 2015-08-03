package com.ddnconsulting.fileprocessor.workers;

import java.nio.file.Path;

import com.ddnconsulting.fileprocessor.core.FileClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Prints size of any file (not directory) to the log
 *
 * @author Dan Nathanson
 */

public class FileSizeWorker implements FileWorker {
    private static final Logger LOG = LoggerFactory.getLogger(FileSizeWorker.class);

    /**
     * Prints the size of the file to the log
     *
     * @param filePath the file to process.
     * @return true if file processed successfully
     */
    @Override
    public FileWorkerResults handle(Path filePath) {
        FileSizeResults results = new FileSizeResults();
        results.setFilename(filePath.toString());
        try {
            results.setSize(filePath.toFile().length());
            results.setSuccess(true);
        }
        catch (Exception e) {
            LOG.error("Could not get size of file [" + filePath + "]");
            results.setErrorMessage("Could not get size of file");
            results.setSuccess(false);
        }
        return results;
    }

    /**
     * Returns true iff:
     *  operation == "size"
     *
     *  Works for any type of file
     */
    @Override
    public boolean canHandle(String operation, String type) {
        return "sizeof".equals(operation) &&
                !FileClassifier.FILE_TYPE_DIRECTORY.equalsIgnoreCase(type);
    }

    public static final class FileSizeResults extends FileWorkerResults {
        private String filename;
        private long size;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}
