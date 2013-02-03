package com.nexelem.boxeee.model;

import java.util.ArrayList;
import java.util.List;

public class Box {
	public List<Item> items;
	public String name;
	
	public Box(String name){
		this.name = name;
		items = new ArrayList<Item>();
	}
	
	public void add(Item item){
		items.add(0,item);
	}
	
	public Item get(int position){
		return items.get(position);
	}
	
	public int size(){
		return items.size();
	}
	
	public void remove(int position){
		items.remove(position);
	}
	
}
