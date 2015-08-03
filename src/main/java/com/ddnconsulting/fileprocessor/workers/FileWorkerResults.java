package com.ddnconsulting.fileprocessor.workers;

/**
 * Common results object returned by FileWorkers.  FileWorkers should extend this to provide structure specific
 * to their operation.
 *
 * @author Dan Nathanson
 */
public class FileWorkerResults {
    private boolean success;
    private String errorMessage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
