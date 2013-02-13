package com.nexelem.boxplorer;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.Log;
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

import com.nexelem.boxplorer.animation.ToggleAnimation;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;
import com.nexelem.boxplorer.model.ItemState;
import com.nexelem.boxplorer.service.BoxService;
import com.nexelem.boxplorer.service.ItemService;
import com.nexelem.boxplorer.wizard.ItemDialog;

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

	public void updateListAdapterData() {
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

		final Item item = this.boxes.get(boxPosition).getItemsList()
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
		// edycja przedmiotu
		holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fm = null;
				try {
					fm = ((Activity) ListAdapter.this.context)
							.getFragmentManager();
				} catch (ClassCastException e) {
					Log.d("Fragment",
							"Can't get the fragment manager with this");
				}

				FragmentTransaction ft = fm.beginTransaction();
				Fragment prev = fm.findFragmentByTag("item-dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);
				DialogFragment newFragment = ItemDialog.newInstance(item,
						ListAdapter.this.getFullList(),
						ListAdapter.this.itemService, ListAdapter.this,
						boxPosition);
				newFragment.show(fm, "item-dialog");
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
			holder.location = (TextView) view.findViewById(R.id.group_location);
			holder.add = (ImageView) view.findViewById(R.id.group_add_item);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		// obsluga guzika dodawania przedmiotu do pudelka
		holder.add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				FragmentManager fm = null;
				try {
					fm = ((Activity) ListAdapter.this.context)
							.getFragmentManager();
				} catch (ClassCastException e) {
					Log.d("Fragment",
							"Can't get the fragment manager with this");
				}

				FragmentTransaction ft = fm.beginTransaction();
				Fragment prev = fm.findFragmentByTag("item-dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);
				DialogFragment newFragment = ItemDialog.newInstance(null,
						ListAdapter.this.getFullList(),
						ListAdapter.this.itemService, ListAdapter.this,
						groupPosition);
				newFragment.show(fm, "item-dialog");
			}
		});
		holder.name.setText(this.boxes.get(groupPosition).getName());
		holder.location.setText(this.boxes.get(groupPosition).getLocation());
		return view;
	}

	@Override
	public Object getGroup(int groupPosition) {
		if (this.getGroupCount() >= groupPosition) {
			Box b = this.boxes.get(groupPosition);
			Log.i("Box:", b.getId() + " " + b.getName());
			return this.boxes.get(groupPosition);
		}
		return null;
	}

	@Override
	public int getGroupCount() {
		if (this.boxes != null) {
			return this.boxes.size();
		}
		return 0;
	}

	@Override
	public long getGroupId(int groupPosition) {
		Box box = ((Box) this.getGroup(groupPosition));
		if (box != null) {
			box.getId().getMostSignificantBits();
		}
		return 0l;
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
		public TextView location;
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

	@SuppressLint("ShowToast")
	public void searchForBox(String id) {
		Log.i("QR", "Searching for Box id: " + id);
		List<Box> boxesList = new ArrayList<Box>();
		Box box;
		try {
			box = this.boxService.get(id);
			if (box != null) {
				boxesList.add(box);
				this.boxes = boxesList;
				this.notifyDataSetChanged();
			} else {
				Toast.makeText(this.context, "Box not found",
						Toast.LENGTH_SHORT);
			}
		} catch (BusinessException e) {
			Toast.makeText(this.context, "Unable to find Box",
					Toast.LENGTH_SHORT);
			e.printStackTrace();
		}
	}
}