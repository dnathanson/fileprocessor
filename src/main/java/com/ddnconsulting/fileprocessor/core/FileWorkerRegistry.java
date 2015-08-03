package com.ddnconsulting.fileprocessor.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ddnconsulting.fileprocessor.workers.FileWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Registry for FileWorkers.  FileWorker are aware of the types of files they support and the "operation(s)" that they
 * perform.  Types are the MIME type as specified by RFC-2045.  Operations are generic strings and can be any value
 * the worker wants to recognize.  Could use an enum of operation and type, but that would require a code change to
 * those enum values every time a new type or operation is supported.  Using strings for these values sacrifices type
 * safety for easy of maintenance and extensibility. Now, assuming self-registration is implemented, new FileWorkers
 * can be used simply by adding their implementations to the classpath.
 *
 * TODO: options to populate registry
 * 1. Scan class path to find classes implementing FileWorker
 * 2. Use spring Component scanning to scan for custom annotation
 * 3. FileWorkers register themselves upon construction.
 * 4. For this exercise, we will use Spring to manage FileWorkers and manually register them.
 *
 * @author Dan Nathanson
 */
public class FileWorkerRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(FileWorkerRegistry.class);

    private Set<FileWorker> allWorkers = new HashSet<>();

    // Optimization: lazily keep track of workers that match operation/type pair. This optimization relies on the fact
    // that works are added to the registry during initialization only.
    private Map<OperationTypePair, Set<FileWorker>> matchedWorkers = new ConcurrentHashMap<>();

    /**
     * Returns all registered FileWorkers that can perform the specified operation on files of the specified type
     * @param operation operation to perform
     * @param type type of file to perform operation on
     * @return set of all FileWorkers that can perform that operation on specified file type
     */
    public Set<FileWorker> getWorkers(String operation, String type) {
        OperationTypePair key = new OperationTypePair(operation, type);
        Set<FileWorker> workers = matchedWorkers.get(key);
        if (workers != null) {
            return workers;
        }

        workers = new HashSet<>();
        for (FileWorker worker : allWorkers) {
            if (worker.canHandle(operation, type)) {
                workers.add(worker);
            }
        }

        workers = Collections.unmodifiableSet(workers);
        matchedWorkers.put(key, workers);

        return workers;
    }

    /**
     * Registers a FileWorker.
     */
    public void registerWorker(FileWorker worker) {
        if (matchedWorkers.size() > 0) {
            throw new IllegalStateException("All workers must be registered prior to any lookups of workers in the registry");
        }

        allWorkers.add(worker);
    }

    /**
     * Key for matchedWorkers map
     */
    private static final class OperationTypePair {
        String operation;
        String type;

        public OperationTypePair(String operation, String type) {
            this.operation = operation;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            OperationTypePair pair = (OperationTypePair) o;

            if (operation != null ? !operation.equals(pair.operation) : pair.operation != null) {
                return false;
            }
            if (type != null ? !type.equals(pair.type) : pair.type != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = operation != null ? operation.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }
}
