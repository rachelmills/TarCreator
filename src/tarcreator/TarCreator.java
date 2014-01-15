/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tarcreator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    FileOutputStream fos;
    Path filePath;
    private String fileName;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    Writer writer;
    private int i;
    TarArchiveEntry tar_file;
    BufferedOutputStream bOut;
    TarArchiveOutputStream tOut;
    private List<File> files;

    private final static String TXT_FILE_TO_READ = "/Users/rachelmills/Desktop/ClueWeb/WikiParser/ID_Text.txt";
    private final static String OUTPUT_FILE_PATH = "/Users/rachelmills/Desktop/ClueWeb/TarCreator/Files/";

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     * @throws org.apache.commons.compress.archivers.ArchiveException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ArchiveException {
        TarCreator tc = new TarCreator();
        tc.readFile(tc.getFileName());
        tc.processLineByLine();
        tc.readFilesJustWritten();
        tc.compressFiles(tc.getFiles(), new File("f"));
    }

    public TarCreator() throws FileNotFoundException {
        fileName = TXT_FILE_TO_READ;
        writer = null;
        bOut = new BufferedOutputStream(fos);
        tOut = new TarArchiveOutputStream(bOut);
        files = new ArrayList<>();
    }

    private void readFile(String fileName) {
        filePath = Paths.get(fileName);
    }

    private void processLineByLine() throws IOException, ArchiveException {
        try (Scanner scanner = new Scanner(filePath, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                processLine(scanner.nextLine());
            }
        }
    }

    private void processLine(String nextLine) throws IOException, ArchiveException {
        fileName = "filename" + i + ".txt";
        i++;

        //use a second Scanner to parse the content of each line 
        Scanner sc = new Scanner(nextLine);
        sc.useDelimiter("~~}~~");

        int src = sc.nextInt();
        String description = sc.next();
        String j = String.valueOf(i);

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(OUTPUT_FILE_PATH + fileName), "utf-8"));
            writer.write(src + "," + description + "\n");
        } catch (IOException ex) {
            System.out.println("Error:  " + ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
            }
        }
    }

    private void readFilesJustWritten() {
        String target_dir = OUTPUT_FILE_PATH;
        File dir = new File(target_dir);
        files = new ArrayList<>(Arrays.asList(dir.listFiles()));
    }

    public void compressFiles(ArrayList<File> files, File file) throws IOException {
        System.out.println("Compressing " + files.size() + " to " + file.getAbsoluteFile());

        fos = new FileOutputStream(new File(file.getCanonicalPath() + ".tar" + ".gz"));
        // Wrap the output file stream in streams that will tar and gzip everything
        TarArchiveOutputStream taos = new TarArchiveOutputStream(
                new GZIPOutputStream(new BufferedOutputStream(fos)));
        // TAR has an 8 gig file limit by default, this gets around that
//        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to get past the 8 gig limit
        // TAR originally didn't support long file names, so enable the support for it
  //      taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        // Get to putting all the files in the compressed output file
        for (File f : files) {
            addFilesToCompression(taos, f, ".");
        }
        // Close everything up
        taos.close();
        fos.close();
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
