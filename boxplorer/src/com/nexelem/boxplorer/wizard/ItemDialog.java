package com.nexelem.boxplorer.wizard;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
import com.nexelem.boxplorer.utils.ObjectKeeper;

public class ItemDialog extends DialogFragment {

	private Item item = null;
	private int box = 0;

	public static ItemDialog newInstance(Item item, ListAdapter listAdapter, int box) {
		ItemDialog f = new ItemDialog();
		f.item = item;
		f.box = box;
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.item_dialog, container, false);

		final TextView itemName = (TextView) v.findViewById(R.id.item_name);
		final Spinner boxes = (Spinner) v.findViewById(R.id.item_boxlist);
		final boolean updateItem = this.item == null ? false : true;
		boxes.setAdapter(new BoxSpinnerAdapter(ObjectKeeper.getInstance().getBoxList()));
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
				String dialogItemName = itemName.getText().toString();
				if (dialogItemName.length() == 0) {
					AlertDialog alertDialog = new AlertDialog.Builder(ObjectKeeper.getInstance().getListAdapter().getContext()).create();
					alertDialog.setTitle("Błąd dodawania");
					alertDialog.setMessage("Przedmiot musi posiadać swoją nazwę");
					alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					alertDialog.show();
					return;
				}
				Item itemToSave = new Item(dialogItemName, (Box) boxes.getSelectedItem());

				try {
					if (updateItem) {
						itemToSave.setId(ItemDialog.this.item.getId());
						ObjectKeeper.getInstance().getItemService().update(itemToSave);
					} else {
						ObjectKeeper.getInstance().getItemService().create(itemToSave);
					}
				} catch (BusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ObjectKeeper.getInstance().getListAdapter().updateListAdapterData();
				ObjectKeeper.getInstance().getListAdapter().notifyDataSetChanged();
				ItemDialog.this.dismiss();
			}
		});

		return v;
	}

	/**
	 * Obsluguje zapisanie stanu formatki podczas przechodzenia w tryb
	 * landscape/portrait
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("box", this.box);
		outState.putParcelable("item", this.item);
	}

	/**
	 * Odczytuje zachowany stan formatki
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			int box = savedInstanceState.getInt("box", -1);
			Item item = savedInstanceState.getParcelable("item");
			if (box > -1) {
				this.box = box;
			}
			if (item != null) {
				this.item = item;
			}
		}
	}

}
