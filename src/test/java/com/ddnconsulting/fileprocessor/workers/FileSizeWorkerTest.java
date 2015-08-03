package com.ddnconsulting.fileprocessor.workers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;

import com.ddnconsulting.fileprocessor.core.FileClassifier;
import com.ddnconsulting.fileprocessor.workers.FileSizeWorker.FileSizeResults;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for FileSizeWorker
 */
public class FileSizeWorkerTest {

    FileSizeWorker worker ;

    @Before
    public void setUp() {
        worker = new FileSizeWorker();
    }

    /**
     * Test the canHandle method.  Works for any file type except directory. Only works for "sizeof" operation.
     */
    @Test
    public void testCanHandle() throws Exception {
        assertTrue("Can handle sizeof/text", worker.canHandle("sizeof", "text"));
        assertTrue("Can handle sizeof/foo", worker.canHandle("sizeof", "foo"));
        assertFalse("Can not handle sizeof/directory", worker.canHandle("sizeof", FileClassifier.FILE_TYPE_DIRECTORY));
        assertFalse("Can not handle badop/text", worker.canHandle("badop", "text"));
    }

    /**
     * Test success.  Use mocks.
     */
    @Test
    public void testHandle() throws Exception {
        Path path = mock(Path.class);
        File file = mock(File.class);

        when(path.toFile()).thenReturn(file);
        when(path.toString()).thenReturn("file-path");
        when(file.length()).thenReturn(123L);

        FileSizeResults results = (FileSizeResults) worker.handle(path);

        assertEquals("Success", true, results.isSuccess());
        assertEquals("File name", "file-path", results.getFilename());
        assertEquals("Size", 123L, results.getSize());
    }

    /**
     * Test exception handling.  Use mocks.
     */
    @Test
    public void testHandleError() throws Exception {
        Path path = mock(Path.class);
        File file = mock(File.class);

        when(path.toFile()).thenReturn(file);
        when(path.toString()).thenReturn("file-path");
        when(file.length()).thenThrow(new RuntimeException("doh!"));

        FileSizeResults results = (FileSizeResults) worker.handle(path);

        assertEquals("Success", false, results.isSuccess());
        assertEquals("File name", "file-path", results.getFilename());
        assertEquals("Error message", "Could not get size of file", results.getErrorMessage());
    }

}