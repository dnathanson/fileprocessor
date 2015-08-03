package com.ddnconsulting.fileprocessor.core;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.ddnconsulting.fileprocessor.resultshandlers.ResultsHandler;
import com.ddnconsulting.fileprocessor.workers.FileWorker;
import com.ddnconsulting.fileprocessor.workers.FileWorkerResults;


/**
 * Wrapper for FileWorkers that allows them to be run in their own threads.
 *
 * @author Dan Nathanson
 */
public class FileWorkerRunner implements Callable<FileWorkerResults> {

    private FileWorker worker;
    private Path path;
    private ResultsHandler resultsHandler;

    public FileWorkerRunner(FileWorker worker, Path path, ResultsHandler resultsHandler) {
        this.worker = worker;
        this.path = path;
        this.resultsHandler = resultsHandler;
    }

    /**
     * Calls FileWorker#handle.  Eventually should do something interesting with the report produced.
     */
    @Override
    public FileWorkerResults call() throws Exception {
        FileWorkerResults results = worker.handle(path);
        resultsHandler.handleResults(results);
        return results;
    }
}
