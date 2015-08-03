package com.ddnconsulting.fileprocessor.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.ddnconsulting.fileprocessor.resultshandlers.ResultsHandler;
import com.ddnconsulting.fileprocessor.workers.FileWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Visitor for use with {@link Files#walkFileTree}.  For each file, find the type of the file then look for FileWorkers
 * that can perform the requested operations on files of that type.  When a FileWorker is found, the worker is wrapped
 * with a Runnable and dispatched to an ExecutorService to be run in a separate thread.
 *
 * @author Dan Nathanson
 */
class FileWorkerVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOG = LoggerFactory.getLogger(FileWorkerVisitor.class);

    private final List<String> operations;
    private final ExecutorService executor;
    private FileWorkerRegistry registry;
    private ResultsHandler resultsHandler;
    private FileClassifier fileClassifier;

    public FileWorkerVisitor(List<String> operations, ExecutorService executor, FileWorkerRegistry registry,
                             ResultsHandler resultsHandler, FileClassifier fileClassifier) {
        this.operations = operations;
        this.executor = executor;
        this.registry = registry;
        this.resultsHandler = resultsHandler;
        this.fileClassifier = fileClassifier;
    }



    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes ignored) throws IOException {
        String fileType = fileClassifier.getType(path);

        boolean handled = false;
        for (String operation : operations) {
            Set<FileWorker> workers = registry.getWorkers(operation, fileType);
            for (FileWorker worker : workers) {
                FileWorkerRunner runner = new FileWorkerRunner(worker, path, resultsHandler);
                executor.submit(runner);
                handled = true;
            }
        }

        if (!handled) {
            LOG.info("No worker found for: " + path.getFileName() + ": " + fileType);
        }

        return FileVisitResult.CONTINUE;
    }


    /**
     * Handles directories the same as files.
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return visitFile(dir, attrs);
    }
}
