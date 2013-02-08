package com.nexelem.boxeee.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.nexelem.boxeee.db.DBHelper;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.model.Item;

public class ItemDao {

	Dao<Item, UUID> helper;
	private final Dao<Box, UUID> boxHelper;

	public ItemDao(DBHelper helper) throws SQLException {
		this.helper = helper.getDao(Item.class);
		this.boxHelper = helper.getDao(Box.class);
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

	/**
	 * Metoda szuka przedmiotow wg wzorca LIKE %NAZWA%
	 * 
	 * @param name
	 * @return
	 * @throws SQLException
	 */
	public List<Box> getByLikelyItemName(List<Box> boxes, String name)
			throws SQLException {
		List<Box> filteredBoxes = new ArrayList<Box>();
		Iterator<Box> boxIt = this.boxHelper.iterator();
		while (boxIt.hasNext()) {
			Box inBox = boxIt.next();
			List<Item> filteredItems = new ArrayList<Item>();
			Iterator<Item> itIt = inBox.getItems().iterator();
			while (itIt.hasNext()) {
				Item inItem = itIt.next();
				if (inItem.getName().matches("(?i).*" + name + ".*")) {
					filteredItems.add(inItem);
				}
			}
			if (filteredItems.size() > 0) {
				filteredBoxes.add(new Box(inBox.getId(), inBox.getName(), inBox
						.getLocation(), filteredItems));
			}
		}
		return filteredBoxes;
	}

	public List<Item> list() throws SQLException {
		return this.helper.queryForAll();
	}

	public void deleteByBoxId(UUID id) throws SQLException {
		DeleteBuilder<Item, UUID> builder = this.helper.deleteBuilder();
		builder.where().eq("box_id", id);
		builder.delete();
	}
}
