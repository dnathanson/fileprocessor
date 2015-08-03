package com.ddnconsulting.fileprocessor;

import java.util.Map;

import com.ddnconsulting.fileprocessor.core.FileClassifier;
import com.ddnconsulting.fileprocessor.core.FileProcessor;
import com.ddnconsulting.fileprocessor.core.FileWorkerRegistry;
import com.ddnconsulting.fileprocessor.core.FilesProbeClassifier;
import com.ddnconsulting.fileprocessor.resultshandlers.LoggingResultsHandler;
import com.ddnconsulting.fileprocessor.resultshandlers.ResultsHandler;
import com.ddnconsulting.fileprocessor.workers.DirectoryLister;
import com.ddnconsulting.fileprocessor.workers.FileSizeWorker;
import com.ddnconsulting.fileprocessor.workers.FileWorker;
import com.ddnconsulting.fileprocessor.workers.JarFileContentsLister;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Main application using Spring Boot.  Replacement for application context XML file.  Useful for little POCs like this.
 */
@SpringBootApplication
public class Application
{
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /*
     * Common Jackson object mapper for JSON serialization/deserialization
     */
    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_DEFAULT);
        return objectMapper;
    }


    @Bean
    FileWorkerRegistry fileWorkerRegistry() {
        return new FileWorkerRegistry();
    }

    @Bean
    FileClassifier fileClassifier() {
        return new FilesProbeClassifier();
    }

    @Bean
    ResultsHandler reportHandler() {
        return new LoggingResultsHandler();
    }

    @Bean
    FileSizeWorker fileSizeWorker() {
        return new FileSizeWorker();
    }

    @Bean
    JarFileContentsLister jarFileContentsLister() {
        return new JarFileContentsLister();
    }

    @Bean
    DirectoryLister directoryLister() {
        return new DirectoryLister();
    }

    /*
     * Main class for this application.
     */
    @Bean
    FileProcessor fileProcessor() {
        return new FileProcessor();
    }


    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        // Manual registration of FileWorkers.
        // TODO: remove when auto-discovery or auto-registration implemented
        FileWorkerRegistry registry = ctx.getBean(FileWorkerRegistry.class);
        Map<String, FileWorker> workers = ctx.getBeansOfType(FileWorker.class);
        for (FileWorker fileWorker : workers.values()) {
            registry.registerWorker(fileWorker);
        }

        FileProcessor fileProcessor = ctx.getBean(FileProcessor.class);

        fileProcessor.run(args);
    }
}