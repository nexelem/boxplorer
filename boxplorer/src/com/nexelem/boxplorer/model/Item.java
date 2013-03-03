package com.nexelem.boxplorer.model;

import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.nexelem.boxplorer.enums.ItemState;

@DatabaseTable(tableName = "item")
public class Item implements Parcelable {

	@DatabaseField(allowGeneratedIdInsert = true, generatedId = true)
	private UUID id = UUID.randomUUID();

	@DatabaseField(canBeNull = false)
	private String name;

	private ItemState state = ItemState.DEFAULT;

	@DatabaseField(foreign = true)
	private Box box;

	public Item() {
	}

	public Item(String name) {
		this.name = name;
		this.state = ItemState.NEW;
	}

	public Item(String name, Box box) {
		this(name);
		this.box = box;
	}

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ItemState getState() {
		return this.state;
	}

	public void setState(ItemState state) {
		this.state = state;
	}

	public Box getBox() {
		return this.box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(this.id.toString());
		parcel.writeString(this.name);
	}

}
