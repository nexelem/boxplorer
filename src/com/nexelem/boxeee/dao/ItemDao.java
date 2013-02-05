package com.nexelem.boxeee.dao;

import java.sql.SQLException;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;
import com.nexelem.boxeee.db.DBHelper;
import com.nexelem.boxeee.model.Item;

public class ItemDao {

	Dao<Item, UUID> helper;

	public ItemDao(DBHelper helper) throws SQLException {
		this.helper = helper.getDao(Item.class);
	}

	public Item get(UUID id) throws SQLException {
		return this.helper.queryForId(id);
	}

	public void delete(UUID id) throws SQLException {
		this.helper.deleteById(id);
	}

	public void update(Item item) throws SQLException {
		this.helper.update(item);
	}

	public void create(Item item) throws SQLException {
		this.helper.create(item);
	}

}
