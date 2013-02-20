package com.nexelem.boxplorer.utils;

import java.util.List;

import com.nexelem.boxplorer.adapter.ListAdapter;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.service.BoxService;
import com.nexelem.boxplorer.service.ItemService;

/**
 * Klasa przechowuje utworzone obiekty aby mozna je bylo latwo przenosic miedzy
 * innymi obiektami
 * 
 * @author darek zon
 * 
 */
public class ObjectKeeper {

	private static volatile ObjectKeeper instance;

	private List<Box> boxList;

	private BoxService boxService;

	private ItemService itemService;

	private ListAdapter listAdapter;

	public List<Box> getBoxList() {
		return this.boxList;
	}

	public void setBoxList(List<Box> boxList) {
		this.boxList = boxList;
	}

	public BoxService getBoxService() {
		return this.boxService;
	}

	public void setBoxService(BoxService boxService) {
		this.boxService = boxService;
	}

	public ItemService getItemService() {
		return this.itemService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public ListAdapter getListAdapter() {
		return this.listAdapter;
	}

	public void setListAdapter(ListAdapter listAdapter) {
		this.listAdapter = listAdapter;
	}

	public static ObjectKeeper getInstance() {
		if (instance == null) {
			synchronized (ObjectKeeper.class) {
				if (instance == null) {
					instance = new ObjectKeeper();
				}
			}
		}
		return instance;
	}

	private ObjectKeeper() {
	}

}
