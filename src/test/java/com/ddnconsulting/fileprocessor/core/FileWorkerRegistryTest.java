package com.ddnconsulting.fileprocessor.core;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.Set;

import com.ddnconsulting.fileprocessor.workers.FileWorker;
import com.ddnconsulting.fileprocessor.workers.FileWorkerResults;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for FileWorkerRegistry
 */
public class FileWorkerRegistryTest {
    FileWorkerRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new FileWorkerRegistry();
    }

    @Test
    public void testGetWorkersNone() throws Exception {
        assertTrue("Empty set", registry.getWorkers("list", "xml").isEmpty());
    }

    @Test
    public void testGetWorkersNoMatch() throws Exception {
        registry.registerWorker(new SizeXmlFileWorker());
        assertTrue("Empty set", registry.getWorkers("list", "xml").isEmpty());
    }

    @Test
    public void testGetWorkersMatches() throws Exception {
        SizeXmlFileWorker sizeXmlFileWorker = new SizeXmlFileWorker();
        ListAnyFileWorker listAnyFileWorker = new ListAnyFileWorker();
        ListXmlFileWorker listXmlFileWorker = new ListXmlFileWorker();

        registry.registerWorker(sizeXmlFileWorker);
        registry.registerWorker(listAnyFileWorker);
        registry.registerWorker(listXmlFileWorker);

        Set<FileWorker> workers = registry.getWorkers("size", "xml");
        assertEquals("Num workers matched", 1, workers.size());
        assertTrue("Found Size/XML", workers.contains(sizeXmlFileWorker));

        workers = registry.getWorkers("list", "xml");
        assertEquals("Num workers matched", 2, workers.size());
        assertTrue("Found List/XML", workers.contains(listXmlFileWorker));
        assertTrue("Found List/*", workers.contains(listAnyFileWorker));

        // Verify that caching is working
        Set<FileWorker> workers2 = registry.getWorkers("list", "xml");
        assertSame("Should return cached set", workers, workers2);

        workers = registry.getWorkers("list", "txt");
        assertEquals("Num workers matched", 1, workers.size());
        assertTrue("Found List/*", workers.contains(listAnyFileWorker));
    }

    /**
     * Test that you cannot register a worker after the registry has been queried
     */
    @Test
    public void testRegisterAfterGet() {
        SizeXmlFileWorker sizeXmlFileWorker = new SizeXmlFileWorker();
        registry.registerWorker(sizeXmlFileWorker);

        Set<FileWorker> workers = registry.getWorkers("size", "xml");

        ListAnyFileWorker listAnyFileWorker = new ListAnyFileWorker();
        try {
            registry.registerWorker(listAnyFileWorker);
            fail("Should not be able to register workers after first lookup");
        }
        catch (IllegalStateException expected) {
            // expected
        }
    }

    // Handles list/xml
    public static final class ListXmlFileWorker implements FileWorker {

        @Override
        public FileWorkerResults handle(Path filePath) {
            return null;
        }

        @Override
        public boolean canHandle(String operation, String type) {
            return "xml".equalsIgnoreCase(type) && "list".equalsIgnoreCase(operation);
        }
    }

    // Handles cat/*
    public static final class ListAnyFileWorker implements FileWorker {

        @Override
        public FileWorkerResults handle(Path filePath) {
            return null;
        }

        @Override
        public boolean canHandle(String operation, String type) {
            return "list".equalsIgnoreCase(operation);
        }
    }


    // Handles size/xml
    public static final class SizeXmlFileWorker implements FileWorker {

        @Override
        public FileWorkerResults handle(Path filePath) {
            return null;
        }

        @Override
        public boolean canHandle(String operation, String type) {
            return "xml".equalsIgnoreCase(type) && "size".equalsIgnoreCase(operation);
        }
    }
}