package com.nexelem.boxplorer.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import android.util.Log;

import com.nexelem.boxplorer.dao.BoxDao;
import com.nexelem.boxplorer.dao.ItemDao;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.db.DBHelper;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;

public class ItemService {

	private static final String TAG = ItemService.class.getName();

	private ItemDao dao = null;

	public ItemService(DBHelper helper) throws BusinessException {
		try {
			this.dao = new ItemDao(helper);
			new BoxDao(helper);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to create DAO object", e);
			throw new BusinessException(e, "Unable to create DAO object");
		}
	}

	public void create(Item item) throws BusinessException {
		try {
			this.dao.create(item);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to save Item in Box", e);
			throw new BusinessException(e, "Unable to save Item in Box");
		}
	}

	public void update(Item item) throws BusinessException {
		try {
			this.dao.update(item);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to update Item in database", e);
			throw new BusinessException(e, "Unable to update Item");
		}
	}

	public Item get(UUID id) throws BusinessException {
		try {
			return this.dao.get(id);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to get Item object", e);
			throw new BusinessException(e, "Unable to get Item object $s", id);
		}
	}

	public void delete(UUID id) throws BusinessException {
		try {
			this.dao.delete(id);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to delete Item object", e);
			throw new BusinessException(e, "Unable to delete Item object $s", id);
		}
	}

	public List<Item> list() throws BusinessException {
		try {
			return this.dao.list();
		} catch (SQLException e) {
			Log.e(TAG, "Unable to list Items", e);
			throw new BusinessException(e, "Unable to list Items");
		}
	}

	public List<Box> getByLikelyItemName(List<Box> boxes, String name) throws BusinessException {
		try {
			return this.dao.getByLikelyItemName(boxes, name);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to find Items", e);
			throw new BusinessException(e, "Unable to find Items with name $s", name);
		}
	}

}
