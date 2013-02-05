package com.nexelem.boxeee.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.nexelem.boxeee.db.DBHelper;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.model.Item;

/**
 * Klasa DAO odpowiedzialna za obsluge tabeli Box w bazie danych
 * 
 * 
 * @author darek
 * 
 */
public class BoxDao {

	private Dao<Item, UUID> itemHelper;
	private Dao<Box, UUID> helper;

	public BoxDao(DBHelper helper) throws SQLException {
		this.itemHelper = helper.getDao(Item.class);
		this.helper = helper.getDao(Box.class);
	}

	public List<Box> getByName(String name) throws SQLException {
		return this.helper.queryForEq("name", name);
	}

	public void create(Box box) throws SQLException {
		this.helper.create(box);
	}

	public void update(Box box) throws SQLException {
		this.helper.update(box);

	}

	public void delete(UUID id) throws SQLException {
		this.helper.deleteById(id);
	}

	public List<Box> list() throws SQLException {
		return this.helper.queryForAll();
	}

	public Box get(UUID id) throws SQLException {
		return this.helper.queryForId(id);
	}

	public List<Box> getByLikelyItemName(String name) throws SQLException {
		QueryBuilder<Box, UUID> query = this.helper.queryBuilder();
		QueryBuilder<Item, UUID> itemBuilder = this.itemHelper.queryBuilder();
		itemBuilder.where().like("name", name);
		query.join(itemBuilder);
		return query.query();
	}
}
