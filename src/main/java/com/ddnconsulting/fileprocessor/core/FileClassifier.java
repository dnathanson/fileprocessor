package com.ddnconsulting.fileprocessor.core;

import java.nio.file.Path;

/**
 * Given a Path, determine it's MIME type (as a string).  For Paths representing directories, returns special
 * value (since directories don't have MIME types.
 */
public interface FileClassifier {
    String FILE_TYPE_DIRECTORY = "directory";

    /**
     * Returns MIME type of file at Path.  If Path is for a directory, return {@link #FILE_TYPE_DIRECTORY}.  It type
     * cannot be determined, returns null.
     */
    String getType(Path path);
}
