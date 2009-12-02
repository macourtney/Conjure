package conjure;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


public class Main {
    private static final String CONJURE_JAR_NAME = "conjure.jar";
    private static final String DEFAULT_DIR = "default";
    
    private static final String CONJURE_VERSION = "0.3";

    private String projectName;


    public Main(String projectName) {
        this.projectName = projectName;
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
    }

    public static void createProject (String projectName) {
	            Main main = new Main(projectName);
	
	            try {
	                main.extractAll();
	
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java -jar conjure.jar <project name>");
            System.out.println("       java -jar conjure.jar --version");
            System.out.println("       java -jar conjure.jar --database=[mysql | h2] <project name>");

        } else {
        	String firstArg = args[0].trim();
        	String secondArg = "";
		if (args.length > 1) {
		    secondArg = args[1].trim();
		}

        	if (firstArg.equalsIgnoreCase("-v") || firstArg.equalsIgnoreCase("--version")) {
        		System.out.println("Conjure version: " + CONJURE_VERSION);
        		
        	} else {
		    createProject(firstArg);
        	}
        }
    }

}
