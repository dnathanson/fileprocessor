package com.ddnconsulting.fileprocessor.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

public class FilesProbeClassifierTest {

    FilesProbeClassifier classifier;

    @Before
    public void setUp() throws Exception {
        classifier = new FilesProbeClassifier();
    }

    /**
     * We can only test the branch for when file is a directly.
     */
    @Test
    public void testGetTypeDirectory() throws Exception {
        Path path = mock(Path.class);
        File file = mock(File.class);
        when(path.toFile()).thenReturn(file);
        when(file.isFile()).thenReturn(false);

        assertEquals("File type", FileClassifier.FILE_TYPE_DIRECTORY, classifier.getType(path));
    }

    /**
     * Useless, really
     */
    @Test
    public void testGetTypeFile() throws Exception {
        Path path = mock(Path.class);
        File file = mock(File.class);
        when(path.toFile()).thenReturn(file);
        when(file.isFile()).thenReturn(true);

        assertNull("File type is null", classifier.getType(path));
    }
}