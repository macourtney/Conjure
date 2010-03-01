package conjure;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {
    private static final String CONJURE_JAR_NAME = "conjure.jar";
    private static final String DEFAULT_DIR = "default";
    
    private static final String CONJURE_VERSION = "0.5";
    
    private static final String DEFAULT_DATABASE = "h2";
    
    private static final String DATABASE_OPTION = "database";
    private static final String HELP_OPTION = "help";
    private static final String HELP_SHORT_OPTION = "h";
    private static final String UPDATE_OPTION = "update";
    private static final String UPDATE_SHORT_OPTION = "u";
    private static final String VERSION_OPTION = "version";
    private static final String VERSION_SHORT_OPTION = "v";

    private OptionSet options;
    private String projectName;


    public Main(String projectName, OptionSet options) {
        this.options = options;
        
        this.projectName = projectName;
    }

    private void extractZipEntry(JarFile conjureJar, ZipEntry zipEntry) throws IOException {
        String defaultDirPrefix = DEFAULT_DIR + '/';

        String entryFileName = this.projectName + '/' + zipEntry.getName().substring(defaultDirPrefix.length());

        if (zipEntry.isDirectory()) {
            File dirFile = new File(entryFileName);
            
            System.out.println("Creating: " + dirFile.getPath());
            
            dirFile.mkdir();
        
        } else {
            File file = new File(entryFileName);
            
            System.out.println("Creating: " + file.getPath());

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            InputStream fileInputStream = ClassLoader.getSystemResourceAsStream(zipEntry.getName());
            fileInputStream = new BufferedInputStream(fileInputStream);

            byte[] buffer = new byte[1024];
            int bytesRead = fileInputStream.read(buffer, 0, buffer.length);

            while (bytesRead >= 0) {
                fileOutputStream.write(buffer, 0, bytesRead);

                bytesRead = fileInputStream.read(buffer, 0, buffer.length);
            }

            fileOutputStream.flush();
            fileOutputStream.close();

            fileInputStream.close();
        }
    }
    
    /**
     * @return The conjure jar file from the class path.
     * @throws IOException 
     */
    public JarFile findConjureJarFile() throws IOException {

        for (StringTokenizer classPathTokens = new StringTokenizer(
                System.getProperty("java.class.path"),
                System.getProperty("path.separator")); 
                classPathTokens.hasMoreTokens(); ) {

            String classPath = classPathTokens.nextToken();

            if (classPath.endsWith(CONJURE_JAR_NAME)) {
                return new JarFile(classPath);
            }
        }

        return null;
    }

    public void setDatabase(String databaseFlavour) {
        /* write out change of dataase flavour to db_config.clj */
        // read in file by line 
        String s;
        StringBuffer fileBuffer = new StringBuffer();
        
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(this.projectName+"/config/db_config.clj"));
            
            while((s = in.readLine())!= null) {
                fileBuffer.append(s).append('\n');
            }

            in.close();

        } catch(IOException e) {
            System.err.println("Bobbins, couldn't read the db_config.clj file");
        }

        // match up the regex database flavour
        String dbConfigContents = fileBuffer.toString().replaceAll(DEFAULT_DATABASE, 
                databaseFlavour);

        // 4. File output
        try {
            PrintWriter out1 = new PrintWriter(
                    new BufferedWriter(new FileWriter(this.projectName+"/config/db_config.clj")));

            out1.println(dbConfigContents);
            out1.close();

        } catch(EOFException e) {
            System.err.println("End of stream");
            
        } catch(IOException e) {
            System.err.println("Bobbins, couldn't write the db_config.clj file");
        }
    }
    
    private void extractZipEntries(String zipEntryDirPrefix) throws IOException {
        JarFile conjureJar = findConjureJarFile();

        for (Enumeration<? extends ZipEntry> zipEntryEnumeration = conjureJar.entries(); 
                zipEntryEnumeration.hasMoreElements(); ) {

            ZipEntry entry = zipEntryEnumeration.nextElement();
            String entryName = entry.getName();

            if (!entryName.equals(zipEntryDirPrefix) 
                    && entryName.startsWith(zipEntryDirPrefix)) {

                extractZipEntry(conjureJar, entry);
            }
        }
    }
    
    public void extractAll() throws IOException {
        File projectDir = new File(this.projectName);
        projectDir.mkdir();

        extractZipEntries(DEFAULT_DIR + '/');
        
        //Update the database flavor.
        if (this.options.has(DATABASE_OPTION)) {
            String database = (String) this.options.valueOf(
                    DATABASE_OPTION);
            System.out.println("database = " + database);
            setDatabase(database);
        }
    }
    
    public void update() throws IOException {
        List<String> updateDirs = null;
        
        if (this.options.has(UPDATE_SHORT_OPTION)) {
            updateDirs = (List<String>) this.options.valuesOf(UPDATE_SHORT_OPTION);
            
        } else if (this.options.has(UPDATE_OPTION)) {
            updateDirs = (List<String>) this.options.valuesOf(UPDATE_OPTION);
        }

        if ((updateDirs != null) && (updateDirs.size() > 0)) {
            
            for (String updateDir : updateDirs) {
                updateDir.replace('\\', '/');
                
                if (!updateDir.startsWith("/")) {
                    updateDir = "/" + updateDir;
                }
                
                System.out.println("\nUpdating directory: " + updateDir + "\n");
                
                extractZipEntries(DEFAULT_DIR + updateDir);
            }
            
        } else {
            System.out.println("\nUpdating directory: /vendor\n");
            extractZipEntries(DEFAULT_DIR + "/vendor");
        }
    }
    
    public void run() throws IOException {
        boolean showVersion = this.options.has(VERSION_OPTION) 
                || this.options.has(VERSION_SHORT_OPTION);
        
        if (showVersion) {
            System.out.println("Conjure version: " + CONJURE_VERSION);
        }

        if (this.projectName != null) {
            
            if ((this.options.has(UPDATE_SHORT_OPTION)) || (this.options.has(UPDATE_OPTION))) {
                update();
                
            } else {
                extractAll();
            }

        } else if (!showVersion 
                || (this.options.has(HELP_SHORT_OPTION)) 
                || (this.options.has(HELP_OPTION))) {
            
            printHelp();
        }
    }

    public static void createProject(String projectName, OptionSet options) {
        Main main = new Main(projectName, options);

        try {
            main.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printHelp() {
        System.out.println("Usage: java -jar conjure.jar [<options>] <project name>\n"
                + "\n"
                + "Valid options include:\n\n"
                + "  --" + DATABASE_OPTION + " - Changes the database for your project. Example: --" + DATABASE_OPTION + "=mysql will set your project to use the mysql database.\n\n"
                + "  -" + HELP_SHORT_OPTION + " or --" + HELP_OPTION + " - Displays this text.\n\n"
                + "  -" + UPDATE_SHORT_OPTION + " or --"  + UPDATE_OPTION + " - Only updates the given project. If no option is passed, then conjure will only update the vendor/conjure directory. Otherwise, conjure will update the directories specified. For example, -" + UPDATE_SHORT_OPTION + "=script will update your script directory.\n\n"
                + "  -" + VERSION_SHORT_OPTION + " or --" + VERSION_OPTION + " - Displays your Conjure's version number.\n\n");
    }
    
    public static OptionParser createOptionParser() {
        OptionParser parser = new OptionParser();
        parser.accepts(DATABASE_OPTION).withRequiredArg();
        parser.accepts(HELP_OPTION);
        parser.accepts(HELP_SHORT_OPTION);
        parser.accepts(UPDATE_OPTION).withOptionalArg();
        parser.accepts(UPDATE_SHORT_OPTION).withOptionalArg();
        parser.accepts(VERSION_OPTION);
        parser.accepts(VERSION_SHORT_OPTION);

        return parser;
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            printHelp();
            
        } else {
            OptionParser parser = createOptionParser();
            
            String projectName = args[args.length - 1];
            
            if (projectName.startsWith("-")) {
                createProject(null, parser.parse(args));
                
            } else {
                String[] parserArgs = new String[args.length - 1];
                
                for (int x = 0; x < parserArgs.length; x++) {
                    parserArgs[x] = args[x];
                }
                
                createProject(projectName, parser.parse(parserArgs));
            }
        }
    }

}
