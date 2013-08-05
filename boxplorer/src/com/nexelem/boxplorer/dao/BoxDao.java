package com.nexelem.boxplorer.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.nexelem.boxplorer.db.DBHelper;
import com.nexelem.boxplorer.model.Box;

/**
 * Klasa DAO odpowiedzialna za obsluge tabeli Box w bazie danych
 * 
 * @author darek
 * 
 */
public class BoxDao {

	private final Dao<Box, UUID> helper;

	public BoxDao(DBHelper helper) throws SQLException {
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

    public ConnectionSource getConnectionSource() {
        return this.helper.getConnectionSource();
    }

    public void deleteAll() throws SQLException {
        helper.deleteBuilder().delete();
    }

}
