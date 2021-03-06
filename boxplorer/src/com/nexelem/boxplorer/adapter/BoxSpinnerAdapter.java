package com.nexelem.boxplorer.adapter;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.nexelem.boxplorer.Fonts;
import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.model.Box;

/**
 * Klasa obslugujaca liste wyboru pudelek
 * 
 * @author darek zon
 * 
 */
public class BoxSpinnerAdapter implements SpinnerAdapter {

	private final List<Box> boxList;

	public BoxSpinnerAdapter(List<Box> boxList) {
		this.boxList = boxList;
	}

	@Override
	public int getCount() {
		if (this.boxList != null) {
			return this.boxList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (this.getCount() >= position) {
			return this.boxList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		Box box = (Box) this.getItem(position);
		if (box != null) {
			return box.getId().getMostSignificantBits();
		}
		return 0l;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Box box = (Box) this.getItem(position);
		if (box != null) {
			View view = inflater.inflate(R.layout.box_spinner, null);
			TextView groupName = (TextView) view.findViewById(R.id.box_name);
			TextView groupLocation = (TextView) view.findViewById(R.id.box_location);
			groupName.setTypeface(Fonts.REGULAR_FONT);
			groupLocation.setTypeface(Fonts.REGULAR_FONT);
			groupName.setText(box.getName());
			groupLocation.setText(box.getLocation());
			return view;
		}
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		if ((this.boxList == null) || (this.boxList.size() == 0)) {
			return true;
		}
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// nie trzeba implementowac
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// nie trzeba implementowac
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return this.getView(position, convertView, parent);
	}

}
