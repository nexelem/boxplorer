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

/**
 * Klasa odpowiedzialna za wymiane danyc miedzy aplikacja a baza danych
 * 
 * 
 * @author darek
 * 
 */
public class BoxService {
	private static final String TAG = BoxService.class.getName();
	private BoxDao dao = null;
	private ItemDao itemDao = null;

	public BoxService(DBHelper helper) throws BusinessException {
		try {
			this.dao = new BoxDao(helper);
			this.itemDao = new ItemDao(helper);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to create DAO object", e);
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
			Log.e(TAG, "Unable to save Box to database", e);
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
			Log.e(TAG, "Unable to update Box to database", e);
		}
	}

	public Box get(String id) throws BusinessException {
		UUID uId = null;
		try {
			uId = UUID.fromString(id);
		} catch (IllegalArgumentException iae) {
			Log.e(TAG, "Unable to find Box. Passed ID is invalid", iae);
			throw new BusinessException(iae);
		} catch (NullPointerException npe) {
			Log.e(TAG, "Unable to find Box. Passed ID is null", npe);
			throw new BusinessException(npe);
		}
		return this.get(uId);
	}

	public Box get(UUID id) throws BusinessException {
		try {
			return this.dao.get(id);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to update Box to database", e);
			throw new BusinessException(e, "Unable to get Box object");
		}
	}

	public void delete(UUID id) throws BusinessException {
		try {
			this.itemDao.deleteByBoxId(id);
			this.dao.delete(id);
		} catch (SQLException e) {
			Log.e(TAG, "Unable to delete BOX" + id, e);
			throw new BusinessException(e, "Unable to delete Box");
		}
	}

	public List<Box> list() throws BusinessException {
		try {
			return this.dao.list();
		} catch (SQLException e) {
			Log.e(TAG, "Unable to list BOXes");
			throw new BusinessException(e, "Unable to list Boxes");
		}
	}

}
