package com.ddnconsulting.fileprocessor.resultshandlers;

import com.ddnconsulting.fileprocessor.workers.FileWorkerResults;

/**
 * Handles results produced by FileWorkers.  The idea here is that result handling is probably dependant on the
 * environment (and probably the type of FileWorker, too, but that is not supported yet).  For instance, if the
 * FileProcessor were running on a remote machine, the report handler would send results back to some central
 * server via async messaging.  Or maybe results are written to some data store.  Or notify some locally running
 * application on a mobile device to alert the user.
 *
 * @author Dan Nathanson
 */
public interface ResultsHandler {

    /**
     * Do something interesting with results from FileWorkers
     */
    void handleResults(FileWorkerResults results);
}
