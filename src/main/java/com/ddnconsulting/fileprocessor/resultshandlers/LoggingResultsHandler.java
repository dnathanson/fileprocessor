package com.ddnconsulting.fileprocessor.resultshandlers;

import com.ddnconsulting.fileprocessor.workers.FileWorkerResults;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles results by serializing to JSON and dumping to log
 *
 * @author Dan Nathanson
 */
public class LoggingResultsHandler implements ResultsHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingResultsHandler.class);
    private final ObjectMapper objectMapper;

    public LoggingResultsHandler() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Dump results to log as JSON
     */
    @Override
    public void handleResults(FileWorkerResults results) {
        try {
            LOG.info(objectMapper.writeValueAsString(results));
        }
        catch (JsonProcessingException e) {
            LOG.error("Error serializing results", e);
        }
    }
}
