package com.ddnconsulting.fileprocessor.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Uses Java's {@link Files#probeContentType} method to try to classify files.
 *
 * @author Dan Nathanson
 */
public class FilesProbeClassifier implements FileClassifier {

    /**
     * Returns MIME type of file at Path.  If Path is for a directory, return {@link #FILE_TYPE_DIRECTORY}.  It type
     * cannot be determined, returns null.
     * <p/>
     * NOTE: how well Files.probeContentType() works is dependent on the OS and the version of the JVM in use.  On Mac
     * OSX with Java 7, it doesn't work at all unless you add a file to the JRE's lib/ext directory and add .mime.types
     * file to your home directory and even then, it is just using file extension to determine type. I haven't tested,
     * but I've read that with JDK 8 on OSX you do not need to add those files, but I don't know if it does a better job
     * of determining file type.  There should be a way to get results as good as the unix "file" command which uses
     * "magic" but I don't know how to do it easily.  And it's not worth the effort for a interview take home exercise.
     *
     * Due to the system-dependant results that are returned, it is impossible to write a good unit test for this
     * implementation.
     */
    @Override
    public String getType(Path path)  {
        String fileType;

        // For files, find type.  For directories, use special case FILE_TYPE_DIRECTORY. Yuck.
        if (path.toFile().isFile()) {

            try {
                fileType = Files.probeContentType(path);
            }
            catch (IOException e) {
                return null;
            }
        }
        else {
            fileType = FileClassifier.FILE_TYPE_DIRECTORY;
        }
        return fileType;
    }

}
