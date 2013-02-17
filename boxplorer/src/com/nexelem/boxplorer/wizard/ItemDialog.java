package com.nexelem.boxplorer.wizard;

import java.util.List;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.adapter.BoxSpinnerAdapter;
import com.nexelem.boxplorer.adapter.ListAdapter;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;
import com.nexelem.boxplorer.service.ItemService;

public class ItemDialog extends DialogFragment {

	private Item item = null;
	private List<Box> boxList = null;
	private ItemService itemService = null;
	private ListAdapter listAdapter = null;
	private int box = 0;

	public static ItemDialog newInstance(Item item, List<Box> boxList,
			ItemService itemService, ListAdapter listAdapter, int box) {
		ItemDialog f = new ItemDialog();
		f.setItem(item);
		f.setBoxList(boxList);
		f.setItemService(itemService);
		f.listAdapter = listAdapter;
		f.box = box;
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// mNum = this.getArguments().getInt("num");

		// dupa i takie tam
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.item_dialog, container, false);

		final TextView itemName = (TextView) v.findViewById(R.id.item_name);
		final Spinner boxes = (Spinner) v.findViewById(R.id.item_boxlist);
		final boolean updateItem = this.item == null ? false : true;
		boxes.setAdapter(new BoxSpinnerAdapter(this.boxList));
		boxes.setSelection(this.box);
		if (this.item != null) {
			itemName.setText(this.item.getName());
		}

		Button cancelButton = (Button) v.findViewById(R.id.item_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ItemDialog.this.dismiss();
			}
		});

		Button saveButton = (Button) v.findViewById(R.id.item_save);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Item itemToSave = new Item(itemName.getText().toString(),
						(Box) boxes.getSelectedItem());

				try {
					if (updateItem) {
						itemToSave.setId(ItemDialog.this.item.getId());
						ItemDialog.this.getItemService().update(itemToSave);
					} else {
						ItemDialog.this.getItemService().create(itemToSave);
					}
				} catch (BusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ItemDialog.this.listAdapter.updateListAdapterData();
				ItemDialog.this.listAdapter.notifyDataSetChanged();
				ItemDialog.this.dismiss();
			}
		});

		return v;
	}

	public Item getItem() {
		return this.item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public List<Box> getBoxList() {
		return this.boxList;
	}

	public void setBoxList(List<Box> list) {
		this.boxList = list;
	}

	public ItemService getItemService() {
		return this.itemService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
}
