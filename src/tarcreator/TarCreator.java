package tarcreator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 *
 * @author rachelmills
 */
public class TarCreator {

    Path filePath;
    private String fileName;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    Writer writer;
    private int i;
    TarArchiveEntry tar_file;
    private List<File> files;
    int folderNumber;

    //private final static String TXT_FILE_TO_READ = "/Users/rachelmills/Desktop/ClueWeb/WikiParser/ID_Text.txt";
    //private final static String OUTPUT_FILE_PATH = "/Users/rachelmills/Desktop/ClueWeb/TarCreator/Files/";
    //private final static String TXT_FILE_TO_READ = "/home/wikiprep/wikiprep/work/WikiParser/ID_Text.txt";
    //private final static String OUTPUT_FILE_PATH = "/home/wikiprep/wikiprep/work/TarCreator/Files/";
    private final static String TXT_FILE_TO_READ = "/Volumes/Untitled/wikiprep/WikiOutput/ID_Text.txt";
    private final static String OUTPUT_FILE_PATH = "/Volumes/Untitled/wikiprep/WikiOutput/Files/";

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     * @throws org.apache.commons.compress.archivers.ArchiveException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ArchiveException {
        TarCreator tc = new TarCreator();
        File f = new File("wikiTar");
        try (FileOutputStream fos = new FileOutputStream(new File(f.getCanonicalPath() + ".tar" + ".gz"));
                TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)))) {

//        tc.readFiles();
            tc.readFile(tc.getFileName());
            tc.processLineByLine();
            for (int i = 0; i < tc.folderNumber; i++) {
                tc.readFilesJustWritten(OUTPUT_FILE_PATH + "Files" + i);
                tc.compressFiles(tc.getFiles(), new File("wikiTar"), fos, taos);
            }
        }
    }

//    public void readFiles() {
//        File folder = new File("/Volumes/Untitled/wikiprep/WikiOutput/Files/");
//        File[] listOfFiles = folder.listFiles();
//        for (File file : listOfFiles) {
//            System.out.println("file  " + file.getName());
//        }
//    }
    public TarCreator() throws FileNotFoundException {
        fileName = TXT_FILE_TO_READ;
        writer = null;
        files = new ArrayList<>();
    }

    private void readFile(String fileName) {
        filePath = Paths.get(fileName);
    }

    private void processLineByLine() throws IOException, ArchiveException {
        try (Scanner scanner = new Scanner(filePath, ENCODING.name())) {

            File outputFile = new File(OUTPUT_FILE_PATH + "Files" + "/");
            outputFile.mkdir();

            while (scanner.hasNextLine()) {
                processLine(scanner.nextLine(), i);
                i++;
            }
        }
    }

    private void processLine(String nextLine, int i) throws IOException, ArchiveException {

        //use a second Scanner to parse the content of each line 
        Scanner sc = new Scanner(nextLine);
        sc.useDelimiter("~~}~~");

        int src = sc.nextInt();

        fileName = src + "-" + i + ".txt";

        String title = sc.next();
        String description = sc.next();

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(OUTPUT_FILE_PATH + "Files" + "/" + fileName), "utf-8"));
            writer.write(title + " " + description + "\n");
        } catch (IOException ex) {
            System.out.println("Error:  " + ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
            }
        }
    }

    private void readFilesJustWritten(String path) {
        String target_dir = path + "/";
        File dir = new File(target_dir);
        files = new ArrayList<>(Arrays.asList(dir.listFiles()));
    }

    public void compressFiles(ArrayList<File> files, File file, FileOutputStream fos, TarArchiveOutputStream taos) throws IOException {
        System.out.println("Compressing " + files.size() + " to " + file.getAbsoluteFile());

        // Put all the files in the compressed output file
        for (File f : files) {
            addFilesToCompression(taos, f, ".");
        }
    }

    //add entries to archive file...
    private void addFilesToCompression(ArchiveOutputStream taos, File file, String dir) throws IOException {

        // Create an entry for the file
        taos.putArchiveEntry(new TarArchiveEntry(file, dir + "/" + file.getName()));

        if (file.isFile()) {
            // Add the file to the archive
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(bis, taos);
            taos.closeArchiveEntry();
            bis.close();

        } else if (file.isDirectory()) {
            // close the archive entry
            taos.closeArchiveEntry();
            // go through all the files in the directory and using recursion, add them to the archive

            for (File childFile : file.listFiles()) {
                addFilesToCompression(taos, childFile, file.getName());
            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the files
     */
    public ArrayList<File> getFiles() {
        return (ArrayList<File>) files;
    }
}
