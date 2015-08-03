package com.ddnconsulting.fileprocessor.resultshandlers;

import java.util.ArrayList;
import java.util.List;

import com.ddnconsulting.fileprocessor.workers.FileWorkerResults;

/**
 * Simply holds each handled FileWorkerResults in a list.
 *
 * @author Dan Nathanson
 */
public class CollectingResultsHandler implements ResultsHandler {
    private List<FileWorkerResults> allResults = new ArrayList<>();

    /**
     * Do something interesting with results from FileWorkers
     */
    @Override
    public void handleResults(FileWorkerResults results) {
        allResults.add(results);
    }

    public List<FileWorkerResults> getResults() {
        return allResults;
    }
}
