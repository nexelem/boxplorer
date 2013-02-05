package com.nexelem.boxeee.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nexelem.boxeee.dao.BoxDao;
import com.nexelem.boxeee.db.BusinessException;
import com.nexelem.boxeee.db.DBHelper;
import com.nexelem.boxeee.model.Box;

/**
 * Klasa odpowiedzialna za wymiane danyc miedzy aplikacja a baza danych
 * 
 * 
 * @author darek
 * 
 */
public class BoxService {

	Logger log = Logger.getLogger(BoxService.class.getName());

	private BoxDao dao = null;

	public BoxService(DBHelper helper) throws BusinessException {
		try {
			this.dao = new BoxDao(helper);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Unable to create DAO object", e);
			throw new BusinessException(e, "Unable to create DAO object");
		}
	}

	public void create(Box box) throws BusinessException {

		try {
			if (this.dao.getByName(box.getName()).size() > 0) {
				throw new BusinessException(null, "Box already exists");
			}
		} catch (SQLException e) {
			throw new BusinessException(e, "Unable to get Box by name");
		}

		try {
			this.dao.create(box);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to save Box to database", e);
			throw new BusinessException(e, "Unable to create Box by name");
		}
	}

	public void update(Box box) throws BusinessException {
		if (this.get(box.getId()) == null) {
			throw new BusinessException(null, "Error 404");
		}
		try {
			this.dao.update(box);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to update Box to database", e);
		}
	}

	public Box get(UUID id) throws BusinessException {
		try {
			return this.dao.get(id);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to update Box to database", e);
			throw new BusinessException(e, "Unable to get Box object");
		}
	}

	public void delete(UUID id) throws BusinessException {
		try {
			this.dao.delete(id);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to delete BOX %s", id);
			throw new BusinessException(e, "Unable to delete Box");
		}
	}

	public List<Box> list() throws BusinessException {
		try {
			return this.dao.list();
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to list BOXes");
			throw new BusinessException(e, "Unable to list Boxes");
		}
	}

}
