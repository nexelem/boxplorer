package com.nexelem.boxeee.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "box")
public class Box {

	@DatabaseField(allowGeneratedIdInsert = true, generatedId = true)
	private UUID id = UUID.randomUUID();

	@DatabaseField(canBeNull = false)
	private String name;

	@DatabaseField(canBeNull = false)
	private String location;

	@ForeignCollectionField(eager = true)
	Collection<Item> items;

	public Box() {
	}

	public Box(String name, String location) {
		this.name = name;
		this.location = location;
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Collection<Item> getItems() {
		return this.items;
	}

	public List<Item> getItemsList() {
		return new ArrayList<Item>(this.items);
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

}
