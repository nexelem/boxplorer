package com.nexelem.boxplorer.exporter;

import com.nexelem.boxplorer.db.BusinessException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Klasa wykonujaca operacje na plikach w czasie importu/eksportu bazy
 *
 * Created by mzagorski on 26.07.13.
 */
public class FileHelper {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm");
    private static final String FILE_PREFIX = "boxplorer";
    private static final String FILE_SUFFIX = ".json";
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
        return String.format("%s%s%s", FILE_PREFIX, date, FILE_SUFFIX);
    }

    /**
     * Znajduje najnowszy plik z danymi w folderze dbDir.
     * Wiek pliku okresla na podstawie daty w nazwie
     * @return najnowszy plik lub null gdy zaden nie zostanie odnaleziony
     */
    public File findLatest() {
        File[] files = dbDir.listFiles();
        File latest = null;
        Date latestDate = new Date(Long.MIN_VALUE);
        for(File file : files) {
            String fileName = file.getName();
            if(fileName.startsWith(FILE_PREFIX) && fileName.endsWith(FILE_SUFFIX)) {
                Date fileDate = dateFormat.parse(fileName, new ParsePosition(FILE_PREFIX.length()));
                if(fileDate.after(latestDate)) {
                    latestDate = fileDate;
                    latest = file;
                }
            }
        }
        return latest;
    }


    /**
     * Odczytuje zawartosc najnowszego pliku.
     * Wiek pliku okresla na podstawie daty w nazwie
     * @return zawartosc pliku
     * @throws BusinessException gdy nie odnajdzie zadnego pliku z danymi
     * @throws IOException
     */
    public String readLatest() throws BusinessException, IOException {
        File file = findLatest();
        if(file == null) {
            throw new BusinessException("Cannot find any file with data");
        }
        StringBuilder sb =  new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new BusinessException(e, "Cannot find any file with data");
        }
        return sb.toString();
    }


}
