package com.ddnconsulting.fileprocessor.workers;

import java.nio.file.Path;

/**
 * Interface for FileWorkers.  FileWorkers are classes that perform operations on files (or directories).  A file
 * worker can do almost anything to a file.  There can be many registered FileWorkers. For each file being processed,
 * the {@link #canHandle} method is invoked to see if the worker can support the given operation for the
 * specified file type.  If the worker can handle it, then the {@link #handle} method is called for that file.
 * The implementation of the {@code handle} method is totally up to the developer but it is <b>strongly</b>
 * suggested that long running operation be handled in an asynchronous manner.
 *
 * @author Dan Nathanson
 */
public interface FileWorker {

    /**
     * Handle a file. Return JSON report of results.
     *
     * It is recommended that long running operations are performed asynchronously.
     *
     * @param filePath the file to process.
     * @return JSON report
     */
    FileWorkerResults handle(Path filePath);

    /**
     * Returns true iff this worker can perform the specified operation for the given file type.   "operation" can be
     * any string - it just needs to be recognized by the FileWorker implementation.   "type" is a MIME type name
     * as specified by RFC-2045 or the string "directory".
     *
     * @param operation operation to perform
     * @param type the type of the file as specified MIME spec (RFC-2045) or "directory" for directories.
     * @return true iff this worker can perform the specified operation for the given file type
     */
    boolean canHandle(String operation, String type);

}
