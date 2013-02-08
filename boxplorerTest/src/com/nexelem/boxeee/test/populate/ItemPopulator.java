package com.nexelem.boxeee.test.populate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import com.nexelem.boxeee.db.BusinessException;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.model.Item;
import com.nexelem.boxeee.service.ItemService;

public class ItemPopulator {

	private static final String ITEM_NAME = "item_";

	private ItemService itemService = null;

	public ItemPopulator(ItemService service) {
		this.itemService = service;
	}

	public Item createItem() throws BusinessException {
		return new Item(ITEM_NAME + RandomUtils.nextInt());
	}

	public List<Item> createItems(int itemsNum) throws BusinessException {
		List<Item> items = new ArrayList<Item>();
		for (int i = 0; i < itemsNum; ++i) {
			items.add(this.createItem());
		}
		return items;
	}

	public void populateItems(int number, Box box) throws BusinessException {
		for (int i = 0; i < number; ++i) {
			Item item = this.createItem();
			item.setBox(box);
			this.itemService.create(item);
		}
	}
}
