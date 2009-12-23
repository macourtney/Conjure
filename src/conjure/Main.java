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
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public void setDatabase(String databaseFlavour) {
	/* write out change of dataase flavour to db_config.clj */
	//	read in file by line 
	String s, s2 = new String();
	try {
	    BufferedReader in = new BufferedReader(
						   new FileReader(this.projectName+"/config/db_config.clj"));
	    while((s = in.readLine())!= null)
		s2 += s + "\n";
	    in.close();
	}
	catch(IOException e) {
	    System.err.println("Bobbins, couldn't read the db_config.clj file");
	}
	// match up the regex database flavour
	Pattern p = Pattern.compile("mysql");//(databaseFlavour);
	Matcher m = p.matcher(s2);
	while(m.find()) {
	    s2 = s2.replaceAll("mysql", databaseFlavour);
	}

	
	// 4. File output
	try {
	    BufferedReader in4 = new BufferedReader(
						    new StringReader(s2));
	    PrintWriter out1 = new PrintWriter(
				     new BufferedWriter(new FileWriter(this.projectName+"/config/db_config.clj")));
	    while((s = in4.readLine()) != null )
		out1.println(s);
	    out1.close();
	} catch(EOFException e) {
	    System.err.println("End of stream");
	} catch(IOException e) {
	    System.err.println("Bobbins, couldn't write the db_config.clj file");
	}
    }

    public static void createProject (String projectName, String databaseFlavour) {
	            Main main = new Main(projectName);
	
	            try {
	                main.extractAll();
			main.setDatabase(databaseFlavour);
	
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
    }

    public static void printHelp () {
            System.out.println("Usage: java -jar conjure.jar <project name>");
            System.out.println("       java -jar conjure.jar --version");
            System.out.println("       java -jar conjure.jar --database=[mysql | h2] <project name>");
    }

    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
	    printHelp();
        } else {
        	String firstArg = args[0].trim();
        	String secondArg = "";
		if (args.length > 1) {
		    secondArg = args[1].trim();
		}

        	if (firstArg.equalsIgnoreCase("-v") || firstArg.equalsIgnoreCase("--version")) {
        		System.out.println("Conjure version: " + CONJURE_VERSION);
        	} else if (firstArg.matches("--database.*")) {
		    if (args.length == 2) {
			String database = firstArg.substring(firstArg.length() - 5);
			if (database.equals("mysql")) {
			    createProject(secondArg, database);
			} else {
			    System.out.println("Couldn't use "+database+" so I'm falling back to h2");
			    database = "h2";
			    createProject(secondArg, database);
			}
			System.out.println("Using "+database+" to store data.");
		    } else {
			printHelp();
		    }
        	} else {
		    createProject(firstArg, "mysql");
        	}
        }
    }

}
