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
	Collection<Item> items = new ArrayList<Item>();

	public Box() {
	}

	public Box(String name, String location) {
		this.name = name;
		this.location = location;
	}

	public Box(UUID id, String name, String location, Collection<Item> items) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.items = items;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Box other = (Box) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
