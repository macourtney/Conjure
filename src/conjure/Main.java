package conjure;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;

import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


public class Main {
    private static final String CONJURE_JAR_NAME = "conjure.jar";
    private static final String DEFAULT_DIR = "default";

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

    public void extractAll() throws IOException {
        File projectDir = new File(this.projectName);
        projectDir.mkdir();

        JarFile conjureJar = new JarFile(CONJURE_JAR_NAME);

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

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java -jar conjure.jar <project name>");

        } else {

            Main main = new Main(args[0]);

            try {
                main.extractAll();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
