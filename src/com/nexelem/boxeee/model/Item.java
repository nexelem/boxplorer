package com.nexelem.boxeee.model;

public class Item {
	public String name;
	public ItemState state;
	
	public Item(String name){
		this.name = name;
		this.state = ItemState.NEW;
	}
}
