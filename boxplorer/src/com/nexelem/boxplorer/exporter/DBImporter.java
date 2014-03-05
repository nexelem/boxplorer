package com.nexelem.boxplorer.exporter;

import android.util.Log;

import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;
import com.nexelem.boxplorer.utils.ObjectKeeper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Klasa odpowiedzialna za import danych z pliku do bazy
 * Created by mzagorski on 05.08.13.
 */
public class DBImporter {

    private static final String LOG_TAG = DBImporter.class.getSimpleName();

    private FileHelper fileHelper;

    public DBImporter(File extCacheDir) {
        this.fileHelper = new FileHelper(extCacheDir);
    }

    /**
     * Importuje pudelka z pliku o formacie json, oraz nazwie i polozeniu okreslonym w FileHelper
     * @param objectKeeper
     * @throws BusinessException
     */
    public void importDB(ObjectKeeper objectKeeper) throws BusinessException {
        try {
            String content = fileHelper.readLatest();
            List<Box> boxes = parse(content);
            objectKeeper.getBoxService().restore(boxes);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while reading from file", e);
        }
    }

    private List<Box> parse(String text) throws BusinessException {
        try {
            List<Box> boxes = new ArrayList<Box>();
            JSONArray jsonBoxes = new JSONArray(text);
            for(int i=0; i< jsonBoxes.length(); i++) {
                boxes.add(parseBox(jsonBoxes.getJSONObject(i)));
            }
            return boxes;
        } catch (JSONException e) {
            throw new BusinessException(e, "Invalid file format");
        }
    }

    private Box parseBox(JSONObject jsonBox) throws JSONException {
        Box box = new Box();
        box.setLocation(jsonBox.getString(ExportConstants.BOX_PARAM_LOCATION));
        box.setName(jsonBox.getString(ExportConstants.BOX_PARAM_NAME));
        box.setId(UUID.fromString(jsonBox.getString(ExportConstants.BOX_PARAM_ID)));
        JSONArray jsonItems = jsonBox.getJSONArray(ExportConstants.BOX_PARAM_ITEMS);
        List<Item> items = new ArrayList<Item>();
        for(int i = 0; i< jsonItems.length(); i++) {
            items.add(parseItem(jsonItems.getJSONObject(i), box));
        }
        box.setItems(items);
        return box;
    }

    private Item parseItem(JSONObject jsonItem, Box box) throws JSONException {
        Item item = new Item();
        item.setName(jsonItem.getString(ExportConstants.ITEM_PARAM_NAME));
        item.setBox(box);
        return item;
    }

}
