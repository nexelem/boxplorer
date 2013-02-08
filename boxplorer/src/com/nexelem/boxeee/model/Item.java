package com.nexelem.boxeee.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "item")
public class Item {

	@DatabaseField(allowGeneratedIdInsert = true, generatedId = true)
	private UUID id = UUID.randomUUID();

	@DatabaseField(canBeNull = false)
	private String name;

	private ItemState state;

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
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ItemState getState() {
		return state;
	}

	public void setState(ItemState state) {
		this.state = state;
	}

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

}
