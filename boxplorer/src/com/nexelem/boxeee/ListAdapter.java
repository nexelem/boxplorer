package com.nexelem.boxeee;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.nexelem.boxeee.animation.ToggleAnimation;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.model.Item;
import com.nexelem.boxeee.model.ItemState;
import com.nexelem.boxplorer.R;

public class ListAdapter extends BaseExpandableListAdapter implements
OnChildClickListener {

	private List<Box> boxes;
	private Context context;

	public ListAdapter(Context context, List<Box> boxes) {
		this.context = context;
		this.boxes = boxes;
	}

	/**
	 * Called when group is collapsed
	 */
	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
		for (Item item : boxes.get(groupPosition).getItems()) {
			item.setState(ItemState.NEW);
		}
	}

	/**
	 * Called when item is clicked - opening toolbar
	 */
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Item item = boxes.get(groupPosition).getItemsList().get(childPosition);
		boolean expanded = false;

		if (item.getState() != ItemState.DEFAULT_EXPANDED) {
			item.setState(ItemState.DEFAULT_EXPANDED);
			LinearLayout toolbar = (LinearLayout) v
					.findViewById(R.id.item_toolbar);
			ToggleAnimation animation = new ToggleAnimation(toolbar, 500);
			toolbar.startAnimation(animation);
			expanded = true;
		}

		for (Box box : boxes) {
			for (Item i : box.getItems()) {
				if (i.getState() == ItemState.DEFAULT_EXPANDED
						&& (!expanded || i != item)) {
					i.setState(ItemState.DEFAULT_TO_HIDE);
				}
			}
		}
		notifyDataSetChanged();
		return true;
	}

	/**
	 * Creating child record view - depending on item status
	 */
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View view, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ViewHolder holder;

		if (view == null) {
			view = inflater.inflate(R.layout.child_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.item_name);
			holder.add = (ImageView) view
					.findViewById(R.id.item_toolbar_remove);
			holder.edit = (ImageView) view.findViewById(R.id.item_toolbar_edit);
			holder.move = (ImageView) view.findViewById(R.id.item_toolbar_move);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		Item item = boxes.get(groupPosition).getItemsList().get(childPosition);

		if (item.getState() == ItemState.DEFAULT
				|| item.getState() == ItemState.NEW) {
			View toolbar = view.findViewById(R.id.item_toolbar);
			((LayoutParams) toolbar.getLayoutParams()).bottomMargin = -50;
			toolbar.setVisibility(View.GONE);
		}

		if (item.getState().equals(ItemState.NEW)) {
			item.setState(ItemState.DEFAULT);
			AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
			alpha.setDuration(800);
			holder.name.startAnimation(alpha);
		} else if (item.getState().equals(ItemState.REMOVE)) {
			AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
			alpha.setDuration(800);
			alpha.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation arg0) {
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
					boxes.get(groupPosition).getItems().remove(childPosition);
					ListAdapter.this.notifyDataSetChanged();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
			view.startAnimation(alpha);
		} else if (item.getState() == ItemState.DEFAULT_TO_HIDE) {
			item.setState(ItemState.DEFAULT);
			LinearLayout toolbar = (LinearLayout) view
					.findViewById(R.id.item_toolbar);
			ToggleAnimation animation = new ToggleAnimation(toolbar, 500);
			toolbar.startAnimation(animation);
		}

		holder.name.setText(boxes.get(groupPosition).getItemsList()
				.get(childPosition).getName());
		holder.add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boxes.get(groupPosition).getItemsList().get(childPosition).setState(ItemState.REMOVE);
				ListAdapter.this.notifyDataSetChanged();
			}
		});
		holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "TODO: edit item", Toast.LENGTH_SHORT)
				.show();
			}
		});
		holder.move.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "TODO: move item", Toast.LENGTH_SHORT)
				.show();

			}
		});

		return view;
	}

	/**
	 * Creating group view
	 */
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View view, final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewHolder holder;

		if (view == null) {
			view = inflater.inflate(R.layout.group_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.group_name);
			holder.add = (ImageView) view.findViewById(R.id.group_add_item);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: dialog with wizard
				boxes.get(groupPosition).getItems().add(new Item("Test "+ (boxes.get(groupPosition).getItems().size() + 1)));
				ListAdapter.this.notifyDataSetChanged();
			}
		});

		holder.name.setText(boxes.get(groupPosition).getName());
		return view;
	}

	public Object getGroup(int groupPosition) {
		return boxes.get(groupPosition);
	}

	public int getGroupCount() {
		return boxes.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

	public Item getChild(int groupPosition, int childPosition) {
		return boxes.get(groupPosition).getItemsList().get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		return boxes.get(groupPosition).getItems().size();
	}

	/**
	 * Helper class for better performance
	 */
	class ViewHolder {
		public TextView name;
		public ImageView add;
		public ImageView edit;
		public ImageView remove;
		public ImageView move;
	}

}