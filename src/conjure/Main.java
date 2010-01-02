package conjure;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {
    private static final String CONJURE_JAR_NAME = "conjure.jar";
    private static final String DEFAULT_DIR = "default";
    
    private static final String CONJURE_VERSION = "0.3";
    
    private static final String DEFAULT_DATABASE = "h2";
    
    private static final String DATABASE_OPTION = "database";
    private static final String HELP_OPTION = "help";
    private static final String HELP_SHORT_OPTION = "h";
    private static final String VERSION_OPTION = "version";
    private static final String VERSION_SHORT_OPTION = "v";

    private OptionSet options;
    private String projectName;


    public Main(OptionSet options) {
        this.options = options;
        
        List<String> nonOptionArguments = this.options.nonOptionArguments();
        
        if ((nonOptionArguments != null) && (nonOptionArguments.size() > 0)) {
            this.projectName = nonOptionArguments.get(0);
            
        } else {
            this.projectName = null;
        }
    }

    private void extractZipEntry(JarFile conjureJar, ZipEntry zipEntry) throws IOException {
        String defaultDirPrefix = DEFAULT_DIR + '/';

        String entryFileName = this.projectName + '/' + zipEntry.getName().substring(defaultDirPrefix.length());

        if (zipEntry.isDirectory()) {
            File dirFile = new File(entryFileName);
            dirFile.mkdir();
        
        } else {
            File file = new File(entryFileName);

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
            System.out.println("classPath: " + classPath);

            if (classPath.endsWith(CONJURE_JAR_NAME)) {
                System.out.println("classPath ends with conjure.jar.");
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
    
    public void extractAll() throws IOException {
        File projectDir = new File(this.projectName);
        projectDir.mkdir();

        JarFile conjureJar = findConjureJarFile();

        for (Enumeration<? extends ZipEntry> zipEntryEnumeration = conjureJar.entries(); zipEntryEnumeration.hasMoreElements(); ) {
            ZipEntry entry = zipEntryEnumeration.nextElement();
            String entryName = entry.getName();

            String defaultDirPrefix = DEFAULT_DIR + '/';

            if (!entryName.equals(defaultDirPrefix) 
                    && entryName.startsWith(defaultDirPrefix)) {

                System.out.println(entry.getName());
                extractZipEntry(conjureJar, entry);
            }
        }
        
        //Update the database flavor.
        if (this.options.has(DATABASE_OPTION)) {
            String database = (String) this.options.valueOf(
                    DATABASE_OPTION);
            System.out.println("database = " + database);
            setDatabase(database);
        }
    }
    
    public void run() throws IOException {
        boolean showVersion = this.options.has(VERSION_OPTION) 
                || this.options.has(VERSION_SHORT_OPTION);
        
        if (showVersion) {
            System.out.println("Conjure version: " + CONJURE_VERSION);
        }
        
        if (this.projectName != null) {
            extractAll();

        } else if (!showVersion 
                || (this.options.has(HELP_SHORT_OPTION)) 
                || (this.options.has(HELP_OPTION))) {
            
            printHelp();
        }
    }

    public static void createProject(OptionSet options) {
        Main main = new Main(options);

        try {
            main.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printHelp() {
        System.out.println("Usage: java -jar conjure.jar [<options>] <project name>\n"
                + "\n"
                + "Valid options include:\n"
                + "  --" + DATABASE_OPTION + " - Changes the database for your project. Example: --" + DATABASE_OPTION + "=mysql will set your project to use the mysql database.\n"
                + "  -" + HELP_SHORT_OPTION + " or --" + HELP_OPTION + " - Displays this text.\n"
                + "  -" + VERSION_SHORT_OPTION + " or --" + VERSION_OPTION + " - Displays your Conjure's version number.\n");
    }
    
    public static OptionParser createOptionParser() {
        OptionParser parser = new OptionParser();
        parser.accepts(DATABASE_OPTION).withRequiredArg();
        parser.accepts(HELP_OPTION);
        parser.accepts(HELP_SHORT_OPTION);
        parser.accepts(VERSION_OPTION);
        parser.accepts(VERSION_SHORT_OPTION);

        return parser;
    }

    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
            printHelp();
        } else {
            OptionParser parser = createOptionParser();
            createProject(parser.parse(args));
        }
    }

}
