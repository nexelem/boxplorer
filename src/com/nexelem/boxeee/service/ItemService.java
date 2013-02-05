package com.nexelem.boxeee.service;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nexelem.boxeee.dao.BoxDao;
import com.nexelem.boxeee.dao.ItemDao;
import com.nexelem.boxeee.db.BusinessException;
import com.nexelem.boxeee.db.DBHelper;
import com.nexelem.boxeee.model.Item;

public class ItemService {

	Logger log = Logger.getLogger(BoxService.class.getName());

	private ItemDao dao = null;

	private BoxDao bDao = null;

	public ItemService(DBHelper helper) throws BusinessException {
		try {
			this.dao = new ItemDao(helper);
			this.bDao = new BoxDao(helper);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Unable to create DAO object", e);
			throw new BusinessException(e, "Unable to create DAO object");
		}
	}

	public void create(Item item) throws BusinessException {
		try {
			dao.create(item);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to save Item in Box", e);
			throw new BusinessException(e, "Unable to save Item in Box");
		}
	}

	public void update(Item item) throws BusinessException {
		try {
			this.dao.update(item);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to update Item in database", e);
			throw new BusinessException(e, "Unable to update Item");
		}
	}

	public Item get(UUID id) throws BusinessException {
		try {
			return this.dao.get(id);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to get Item object", e);
			throw new BusinessException(e, "Unable to get Item object $s", id);
		}
	}

	public void delete(UUID id) throws BusinessException {
		try {
			this.dao.delete(id);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to delete Item object", e);
			throw new BusinessException(e, "Unable to delete Item object $s",
					id);
		}
	}

}
