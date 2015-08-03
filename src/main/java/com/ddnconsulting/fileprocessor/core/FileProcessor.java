package com.ddnconsulting.fileprocessor.core;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.ddnconsulting.fileprocessor.resultshandlers.ResultsHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * A file processor that walks directory structure from a given point and performs operations on each file (or
 * directory) it finds below the specified directory.  The operations to be performed are specified at runtime
 * when the processor is invoked.  More than one operation can be applied to each file.  Operations are specified with
 * simple string names (rather than some fixed set of enum constants) to allow new operations to be added without the
 * need to update enums and factories.  Although this is not typesafe, it is extensible and new workers can be added
 * without modifying the core classes (workers can just be included in the classpath).
 *
 * This implementation assumes that work done (and reported) on each file is independent.  Alternatively, all matching
 * files could be passed to a worker and the worker could work on all of them and produce a consolidated report. Since
 * there is a focus on finding "security anomalies" in most cases a worked should report nothing at all so batching up
 * reports probably doesn't produce a significant benefit.
 *
 * @author Dan Nathanson
 */
@Component
public class FileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessor.class);
    private OptionParser optionParser;
    private OptionSpec<String> directorySpec;
    private OptionSpec<String> operationSpec;

    @Autowired
    private FileWorkerRegistry fileWorkerRegistry;

    @Autowired
    private ResultsHandler resultsHandler;

    @Autowired
    private FileClassifier fileClassifier;

    // Number of threads in the pool
    @Value("${num.threads}")
    private int numThreads = 10;
    // How long to wait for all thread to complete
    @Value("${total.timeout}")
    private int totalTimeout;

    public FileProcessor() {
        optionParser = new OptionParser();
        directorySpec = optionParser.accepts("d", "directory from which to start crawling")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("entry point to filesystem")
                .required();
        operationSpec = optionParser.accepts("o", "operations to perform on each file")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("operation1,operation2,...")
                .withValuesSeparatedBy(',')
                .required();
        optionParser.acceptsAll(Arrays.asList("h", "?"), "show help").forHelp();
    }


    /**
     * Command line API.
     *
     * Handles command line input and starts processing files.  Has two required arguments:
     *  -d <directory>:  specifies the entry point to the filesystem
     *  -o operation1[,operation2,...]: operation(s) to be performed on files
     */
    public void run(String... args) throws Exception {

        OptionSet optionSet = null;
        try {
            optionSet = optionParser.parse(args);
        }
        catch (OptionException e) {
            System.out.println("\n" + e.getMessage() + "\n");
            optionParser.printHelpOn(System.out);
            System.out.println();
            System.exit(1);
        }
        String directory = directorySpec.value(optionSet);
        final List<String> operations = operationSpec.values(optionSet);

        processFiles(directory, operations);
    }


    public void processFiles(String directory, List<String> operations) throws IOException, InterruptedException {

        // Use an ExecutorService to process files in separate threads. This may or may not speed things up and is
        // probably dependent on the hardware/OS on which the program is running since by definition I/O is involved.
        // How much parallelism can be gained is dependant on how the operating system and hardware handle parallel
        // reads.

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("WorkerRunner-%d").build();
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads, threadFactory);

        // Starting at root directory, apply FileWorkerVisitor at all files in this directory and all subdirectories
        FileWorkerVisitor visitor = new FileWorkerVisitor(operations, executor, fileWorkerRegistry, resultsHandler, fileClassifier);
        Files.walkFileTree(FileSystems.getDefault().getPath(directory), Collections.EMPTY_SET, 1,
                           visitor);

        executor.shutdown();

        // There are other ways to do this that are better, but for a command line program this works just fine.
        // If this were a service that continued to handle more requests, this solution is not optimal.
        executor.awaitTermination(totalTimeout, TimeUnit.SECONDS);
    }

    public void setResultsHandler(ResultsHandler resultsHandler) {
        this.resultsHandler = resultsHandler;
    }

    public void setFileWorkerRegistry(FileWorkerRegistry fileWorkerRegistry) {
        this.fileWorkerRegistry = fileWorkerRegistry;
    }
}
