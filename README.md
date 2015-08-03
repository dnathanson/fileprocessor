# File Processor

Simple, extensible file processor that walks the file system and performs operations on files it finds there.
Operations are performed by implementations of the FileWorker interface.  When file processor is run, it is
provided with a directory root and a list of operations to perform on the files and directories found under that
root.  For each file, the registered FileWorkers are checked to see if they can perform the operation on the file
type.




## Design
* When specifying which "operations" a FileWorker supports simple strings matching is used in order to allow
  new FileWorkers to be registered with the File Processor without modifying the File Processor core code.
  * This is not fully implemented yet.  Classpath scanning and automatic registration of FileWorkers in not done.
* Uses Java's Files.walkFileTree(...) method to walk files applying FileWorkerVisitor to each file
* FileWorkersVisitor finds FileWorkers that can be applied to the file/operation. FileWorkers are run in subthreads
  using ThreadPoolExecutor.
  * Number of threads is configurable in application.properties
  * It is recommended that very long running workers should sends work to a queue for truly asynchronous processing
* Handling of results is also extensible
  * Current default results handler serializes resuts to JSON and dumps to log
* There are currently three implementation of FileWorker built in
  * FileSizeWorker gets the size of any file (not directory), regardless of type. Operation: "sizeof"
  * DirectoryLister gets the contents of a directory. Operation: "dir"
  * JarFileContentsLister gets the contents of any JAR file. Operation: "dir"
* Maven build


## Shortcuts taken (or things not yet implemented)

* Autoscanning of classpath to find and self-register any FileWorker implementations
* Standardize pattern for further asynchronous processing of long-running FileWorkers
* More unit tests
* The method of determining the type of a file is not consistent across platforms or even JDK version
* Ability to tie ResultsHandler implementations to FileWorkers

## Running the server

You will need at least Java 7 and Maven 3 installed on your computer.

If you are using Java 7 on Mac OSX you will need to copy a couple of files in order for file type probing to work.
* copy extras/jdk7osx/.mime.types to your home directory
* copy extras/jdk7osx/mimeutils.jar to your Java installation's jre/lib/ext directory

### Build project using maven

```
>  mvn clean package
```

### Run as command line program

Note that results of work done by FileWorkers is displayed on console since currently configured handler for all
FileWorkers is just to dump JSON serialized results to log.

Lists contents of all directories starting at current directory

```
> java -jar target/fileprocessor-0.1.0.jar -d "." -o dir
```

Lists size of all files under the current directory and the contents of all JAR files found under the current
directory (this happens because both of those FileWorkers recognize the string "dir" as their operation).

```
> java -jar target/fileprocessor-0.1.0.jar -d "." -o dir
```

Run both of the above operations

```
> java -jar target/fileprocessor-0.1.0.jar -d "." -o dir,sizeof
```


