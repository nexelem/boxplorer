package com.nexelem.boxeee;

import java.util.ArrayList;
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
import com.nexelem.boxeee.db.BusinessException;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.model.Item;
import com.nexelem.boxeee.model.ItemState;
import com.nexelem.boxeee.service.BoxService;
import com.nexelem.boxeee.service.ItemService;
import com.nexelem.boxplorer.R;

public class ListAdapter extends BaseExpandableListAdapter implements
		OnChildClickListener {

	private List<Box> boxes = new ArrayList<Box>();
	private List<Box> fullList = new ArrayList<Box>();
	private final Context context;
	private final ItemService itemService;
	private final BoxService boxService;
	private final String searchText = "";

	public ListAdapter(Context context, BoxService boxService,
			ItemService itemService) {
		this.context = context;
		this.itemService = itemService;
		this.boxService = boxService;
		this.updateListAdapterData();
	}

	private void updateListAdapterData() {
		try {
			this.boxes = this.boxService.list();
			this.fullList = this.boxes;
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setBoxes(List<Box> boxes) {
		this.boxes = boxes;
	}

	public List<Box> getFullList() {
		return this.fullList;
	}

	/**
	 * Called when group is collapsed
	 */
	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
		for (Item item : this.boxes.get(groupPosition).getItems()) {
			item.setState(ItemState.NEW);
		}
	}

	/**
	 * Called when item is clicked - opening toolbar
	 */
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Item item = this.boxes.get(groupPosition).getItemsList()
				.get(childPosition);
		boolean expanded = false;

		if (item.getState() != ItemState.DEFAULT_EXPANDED) {
			item.setState(ItemState.DEFAULT_EXPANDED);
			LinearLayout toolbar = (LinearLayout) v
					.findViewById(R.id.item_toolbar);
			ToggleAnimation animation = new ToggleAnimation(toolbar, 500);
			toolbar.startAnimation(animation);
			expanded = true;
		}

		for (Box box : this.boxes) {
			for (Item i : box.getItems()) {
				if ((i.getState() == ItemState.DEFAULT_EXPANDED)
						&& (!expanded || (i != item))) {
					i.setState(ItemState.DEFAULT_TO_HIDE);
				}
			}
		}
		this.notifyDataSetChanged();
		return true;
	}

	/**
	 * Creating child record view - depending on item status
	 */
	@Override
	public View getChildView(final int boxPosition, final int itemPosition,
			boolean isLastChild, View view, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.context
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

		Item item = this.boxes.get(boxPosition).getItemsList()
				.get(itemPosition);

		if (item == null) {
			return view;
		}

		if ((item.getState() == ItemState.DEFAULT)
				|| (item.getState() == ItemState.NEW)) {
			View toolbar = view.findViewById(R.id.item_toolbar);
			((LayoutParams) toolbar.getLayoutParams()).bottomMargin = -50;
			toolbar.setVisibility(View.GONE);
		}

		AlphaAnimation alpha;
		switch (item.getState()) {
		case NEW:
			item.setState(ItemState.DEFAULT);
			alpha = new AlphaAnimation(0.0f, 1.0f);
			alpha.setDuration(800);
			holder.name.startAnimation(alpha);
			break;
		case REMOVE:
			alpha = new AlphaAnimation(1.0f, 0.0f);
			alpha.setDuration(800);
			alpha.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation arg0) {
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
					ListAdapter.this.boxes.get(boxPosition).getItems()
							.remove(itemPosition);
					ListAdapter.this.notifyDataSetChanged();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
			view.startAnimation(alpha);
			break;
		case DEFAULT_TO_HIDE:
			item.setState(ItemState.DEFAULT);
			LinearLayout toolbar = (LinearLayout) view
					.findViewById(R.id.item_toolbar);
			ToggleAnimation animation = new ToggleAnimation(toolbar, 500);
			toolbar.startAnimation(animation);
			break;
		}

		holder.name.setText(this.boxes.get(boxPosition).getItemsList()
				.get(itemPosition).getName());
		holder.add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ListAdapter.this.boxes.get(boxPosition).getItemsList()
						.get(itemPosition).setState(ItemState.REMOVE);
				ListAdapter.this.notifyDataSetChanged();
			}

		});
		holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ListAdapter.this.context, "TODO: edit item",
						Toast.LENGTH_SHORT).show();
			}
		});
		holder.move.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(ListAdapter.this.context, "TODO: move item",
						Toast.LENGTH_SHORT).show();

			}
		});

		return view;
	}

	/**
	 * Creating group view
	 */
	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View view, final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) this.context
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
				Item itemToSave = new Item("Test "
						+ (ListAdapter.this.boxes.get(groupPosition).getItems()
								.size() + 1), ListAdapter.this.boxes
						.get(groupPosition));

				try {
					ListAdapter.this.itemService.create(itemToSave);
				} catch (BusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				ListAdapter.this.updateListAdapterData();
				ListAdapter.this.notifyDataSetChanged();
			}
		});

		holder.name.setText(this.boxes.get(groupPosition).getName());
		return view;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.boxes.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.boxes.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public Item getChild(int groupPosition, int childPosition) {
		return this.boxes.get(groupPosition).getItemsList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.boxes.get(groupPosition).getItems().size();
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

	public void searchFor(String newText) throws BusinessException {
		if (this.searchText.startsWith(newText)) {
			this.boxes = this.itemService.getByLikelyItemName(this.boxes,
					newText);
		} else {
			this.boxes = this.itemService.getByLikelyItemName(this.fullList,
					newText);
		}
		this.notifyDataSetChanged();
	}
}