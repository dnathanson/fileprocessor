package com.ddnconsulting.fileprocessor.core;

import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import com.ddnconsulting.fileprocessor.resultshandlers.CollectingResultsHandler;
import com.ddnconsulting.fileprocessor.workers.FileWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

public class FileWorkerVisitorTest {
    FileWorkerVisitor fileWorkerVisitor;
    private ExecutorService executorService;
    private FileWorkerRegistry registry;
    private CollectingResultsHandler reportHandler;
    private FileClassifier fileClassifier;

    @Before
    public void setUp() {
        executorService = mock(ExecutorService.class);
        registry = mock(FileWorkerRegistry.class);
        reportHandler = new CollectingResultsHandler();
        fileClassifier = mock(FileClassifier.class);
        fileWorkerVisitor = new FileWorkerVisitor(Lists.newArrayList("operation1", "operation2"), executorService, registry,
                                                  reportHandler, fileClassifier);
    }

    @Test
    public void testVisitFile() throws Exception {

        Path path = mock(Path.class);
        Path fileName = mock(Path.class);
        when(path.getFileName()).thenReturn(fileName);
        when(fileName.toString()).thenReturn("filename");

        when(fileClassifier.getType(path)).thenReturn("text");

        FileWorker worker1 = mock(FileWorker.class);

        when(registry.getWorkers("operation1", "text")).thenReturn(Sets.newHashSet(worker1));
        when(registry.getWorkers("operation2", "text")).thenReturn(Collections.<FileWorker>emptySet());

        fileWorkerVisitor.visitFile(path, null);

        // Verify called exactly one time (since there is no worker that supports operation2).
        verify(executorService, times(1)).submit(any(FileWorkerRunner.class));

    }
}