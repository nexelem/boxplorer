package com.nexelem.boxplorer.adapter;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.model.Box;

public class BoxSpinnerAdapter implements android.widget.SpinnerAdapter {

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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) parent.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Box box = (Box) this.getItem(position);
		if (box != null) {
			View view = inflater.inflate(R.layout.box_spinner, null);
			TextView groupName = (TextView) view.findViewById(R.id.box_name);
			TextView groupLocation = (TextView) view
					.findViewById(R.id.box_location);
			groupName.setText(box.getName());
			groupLocation.setText(box.getLocation());
			return view;
		}
		return null;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return this.getView(position, convertView, parent);
	}

}
