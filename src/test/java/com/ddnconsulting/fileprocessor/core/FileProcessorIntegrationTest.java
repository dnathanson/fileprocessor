package com.ddnconsulting.fileprocessor.core;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import com.ddnconsulting.fileprocessor.Application;
import com.ddnconsulting.fileprocessor.resultshandlers.CollectingResultsHandler;
import com.ddnconsulting.fileprocessor.workers.DirectoryLister.DirectoryListResults;
import com.ddnconsulting.fileprocessor.workers.DirectoryLister.FileType;
import com.ddnconsulting.fileprocessor.workers.FileSizeWorker.FileSizeResults;
import com.ddnconsulting.fileprocessor.workers.FileWorker;
import com.ddnconsulting.fileprocessor.workers.FileWorkerResults;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Integration test driven by JUnit
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class FileProcessorIntegrationTest implements ApplicationContextAware {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Autowired
    FileProcessor fileProcessor;
    private CollectingResultsHandler resultsHandler;
    private ApplicationContext applicationContext;


    @Before
    public void setUp() {
        resultsHandler = new CollectingResultsHandler();
        fileProcessor.setResultsHandler(resultsHandler);

        // Manual registration of FileWorkers.
        FileWorkerRegistry registry = applicationContext.getBean(FileWorkerRegistry.class);
        Map<String, FileWorker> workers = applicationContext.getBeansOfType(FileWorker.class);
        for (FileWorker fileWorker : workers.values()) {
            registry.registerWorker(fileWorker);
        }
        fileProcessor.setFileWorkerRegistry(registry);

    }

    /**
     * Point FileProcessor to known location that has one file of known size.  Perform directory listing and file size
     * operations.  Verify results.
     */
    @Test
    public void testRun() throws Exception {
        assertNotNull(fileProcessor);

        fileProcessor.processFiles("src/test/integration-test-data", Lists.newArrayList("dir","sizeof"));

        List<FileWorkerResults> results = resultsHandler.getResults();
        assertNotNull(results);
        assertEquals("Num results", 2, results.size());

        FileSizeResults fileSizeResults = getResultsOfType(results, FileSizeResults.class);
        assertEquals("Filename", "src/test/integration-test-data/somefile.txt", fileSizeResults.getFilename());
        assertEquals("Size", 10, fileSizeResults.getSize());

        DirectoryListResults directoryListResults = getResultsOfType(results, DirectoryListResults.class);
        assertEquals("Filename", "src/test/integration-test-data", directoryListResults.getFilename());
        assertEquals("Num files in directory", 1, directoryListResults.getEntries().size());
        assertEquals("Entry name", "somefile.txt", directoryListResults.getEntries().get(0).getFilename());
        assertEquals("Entry type", FileType.FILE, directoryListResults.getEntries().get(0).getType());
    }

    private <T> T getResultsOfType(List<FileWorkerResults> results, Class<T> clazz) {
        for (FileWorkerResults result : results) {
            if (result.getClass().isAssignableFrom(clazz)) {
                return (T) result;
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}