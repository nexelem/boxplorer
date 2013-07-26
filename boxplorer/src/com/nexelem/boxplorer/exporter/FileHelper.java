package com.nexelem.boxplorer.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Klasa wykonujaca operacje na plikach w czasie importu/eksportu bazy
 *
 * Created by mzagorski on 26.07.13.
 */
public class FileHelper {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm");

    private static final String DIR_NAME = "boxplorer";
    private File dbDir;

    /**
     * Konstruktor tworzy folder DIR_NAME wewnatrz cacheDir o ile wczesniej nie istnieje
     * @param cacheDir folder external cache aplikacji
     */
    public FileHelper(File cacheDir) {
        dbDir = new File(cacheDir, DIR_NAME);
        if(!dbDir.exists()) {
            dbDir.mkdir();
        }
    }

    /**
     * Zapisuje text do pliku w folderze DIR_NAME
     * @param text
     * @return
     * @throws IOException
     */
    public File write(String text) throws IOException {
        File file = new File(dbDir, getFilename());
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        out.write(text.getBytes());
        out.close();
        return file;
    }


    private String getFilename() {
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        return String.format("boxplorer%s.json", date);
    }


}
