package com.nexelem.boxplorer.db;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;
import com.nexelem.boxplorer.service.BoxService;

public class DBHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = String
			.valueOf(R.string.db_name);

	private static final int DATABASE_VERSION = 1;

	Logger log = Logger.getLogger(DBHelper.class.getName());

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
		try {
			this.log.log(Level.INFO, "Creating application database schema");
			TableUtils.createTable(this.connectionSource, Box.class);
			TableUtils.createTable(this.connectionSource, Item.class);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Can't create database", e);
			throw new RuntimeException(e);
		}
		// chwilowa akcja, aby byly jakies dane testowe
		this.insertInitValues();
	}

	@Override
	public void onUpgrade(SQLiteDatabase sd, ConnectionSource cs,
			int oldVersion, int newVersion) {
		try {
			this.log.log(Level.INFO, "Updating application database schema");
			TableUtils.dropTable(this.connectionSource, Box.class, true);
			TableUtils.dropTable(this.connectionSource, Item.class, true);
			// after we drop the old databases, we create the new ones
			this.onCreate(sd, this.connectionSource);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Can't drop database schema", e);
			throw new RuntimeException(e);
		}
	}

	private void insertInitValues() {
		Box box1 = new Box("Drobiazgi", "Pokój gościnny");
		Box box2 = new Box("Rower", "Garaż");
		Box box3 = new Box("Gwarancje i części", "Przedpokój");
		Box box4 = new Box("Erotyczne", "Schowek");

		try {
			BoxService bs = new BoxService(this);
			bs.create(box1);
			bs.create(box2);
			bs.create(box3);
			bs.create(box4);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void clearDatabase() {

		try {
			TableUtils.clearTable(this.getConnectionSource(), Item.class);
			TableUtils.clearTable(this.getConnectionSource(), Box.class);
		} catch (SQLException e) {
			this.log.log(Level.WARNING, "Unable to clear database", e);
		}
	}

	@Override
	public void close() {
		super.close();
	}

}