package com.nexelem.boxplorer.exporter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * Klasa odpowiedzialna za eksport modelu bazy do pliku tekstowego
 * Created by mzagorski on 25.07.13.
 */
public class DBExporter {

    private static final String LOG_TAG = DBExporter.class.getSimpleName();

    private static final int whiteSpaces = 2;


    private FileHelper fileHelper;

    /**
     *
     * @param extCacheDir folder external cache aplikacji
     */
    public DBExporter(File extCacheDir) {
        this.fileHelper = new FileHelper(extCacheDir);
    }

    /**
     * Eksportuje model bazy do pliku tekstowego w formacie json
     * @param boxes
     * @return
     */
    public File export(List<Box> boxes) {
        try {
            JSONArray json = new JSONArray();
            for (Box box : boxes) {
                json.put(exportBox(box));
            }
            return fileHelper.write(json.toString(whiteSpaces));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while parsing items", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while writing to file", e);
        }
        return null;
    }

    private JSONObject exportBox(Box box) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ExportConstants.BOX_PARAM_ID, box.getId());
        json.put(ExportConstants.BOX_PARAM_NAME, box.getName());
        json.put(ExportConstants.BOX_PARAM_LOCATION, box.getLocation());
        json.put(ExportConstants.BOX_PARAM_ITEMS, exportItems(box.getItemsList()));
        return json;
    }


    private JSONArray exportItems(List<Item> items) throws JSONException {
        JSONArray json = new JSONArray();
        for (Item item : items) {
            json.put(exportItem(item));
        }
        return json;
    }

    private JSONObject exportItem(Item item) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ExportConstants.ITEM_PARAM_ID, item.getId());
        json.put(ExportConstants.ITEM_PARAM_NAME, item.getName());
        return json;
    }



}
