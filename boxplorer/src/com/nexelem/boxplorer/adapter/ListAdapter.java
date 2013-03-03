package com.nexelem.boxplorer.adapter;

import java.util.ArrayList;
import java.util.List;

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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.nexelem.boxplorer.Fonts;
import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.animation.HeightAnimation;
import com.nexelem.boxplorer.animation.ToggleAnimation;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.enums.BoxState;
import com.nexelem.boxplorer.enums.ItemState;
import com.nexelem.boxplorer.enums.SearchType;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;
import com.nexelem.boxplorer.utils.ObjectKeeper;
import com.nexelem.boxplorer.wizard.BoxDialog;
import com.nexelem.boxplorer.wizard.ItemDialog;
import com.nexelem.boxplorer.wizard.RemoveDialog;

/**
 * Klasa obslugujaca wyswietlanie oraz akcje na elementach listy
 * 
 * @author bartek wilczynski, darek zon
 */
public class ListAdapter extends BaseExpandableListAdapter implements OnChildClickListener {

	/**
	 * Lista przefiltrowanych pudelek (kiedy filtr nie jest nalozony
	 * boxes==fullList)
	 */
	private List<Box> boxes = new ArrayList<Box>();

	/**
	 * Kontekst aplikacji
	 */
	private final Context context;

	/**
	 * Szukany ciag znakow
	 */
	private String searchText = "";

	private int bgHeight = 0;

	public ListAdapter(Context context) {
		this.context = context;
		this.updateListAdapterData();
		ObjectKeeper.getInstance().setListAdapter(this);
	}

	public void updateListAdapterData() {
		try {
			this.boxes = ObjectKeeper.getInstance().getBoxService().list();
			ObjectKeeper.getInstance().setBoxList(this.boxes);
		} catch (BusinessException e) {
			Log.e("APP", "Unable to update ListAdapter data", e);
		}
	}

	/**
	 * Ustawia liste przefiltrowanych pudelek
	 * 
	 * @param boxes
	 *            lista pudelek
	 */
	public void setBoxes(List<Box> boxes) {
		this.boxes = boxes;
	}

	public List<Box> getFullList() {
		return ObjectKeeper.getInstance().getBoxList();
	}

	public Context getContext() {
		return this.context;
	}

	/**
	 * Metoda wywolywana kiedy zamykamy pudelko (grupe obiektow na liscie)
	 */
	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
		for (Item item : this.boxes.get(groupPosition).getItems()) {
			item.setState(ItemState.NEW);
		}
		this.boxes.get(groupPosition).setState(BoxState.COLLAPSE);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
		this.boxes.get(groupPosition).setState(BoxState.EXPAND);
	}

	/**
	 * Metoda wywolywana kiedy klikamy na kokretny przedmiot)
	 */
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Item item = this.boxes.get(groupPosition).getItemsList().get(childPosition);
		boolean expanded = false;

		if (item.getState() != ItemState.DEFAULT_EXPANDED) {
			item.setState(ItemState.DEFAULT_EXPANDED);
			LinearLayout toolbar = (LinearLayout) v.findViewById(R.id.item_toolbar);
			ToggleAnimation animation = new ToggleAnimation(toolbar, 500);
			toolbar.startAnimation(animation);

			ImageView edit = (ImageView) v.findViewById(R.id.item_toolbar_edit);
			ImageView remove = (ImageView) v.findViewById(R.id.item_toolbar_remove);
			edit.setVisibility(View.VISIBLE);
			remove.setVisibility(View.VISIBLE);
			AlphaAnimation alpha = new AlphaAnimation(0, 1);
			alpha.setDuration(500);
			edit.startAnimation(alpha);
			remove.startAnimation(alpha);
			expanded = true;
		}

		for (Box box : this.boxes) {
			for (Item i : box.getItems()) {
				if ((i.getState() == ItemState.DEFAULT_EXPANDED) && (!expanded || (i != item))) {
					i.setState(ItemState.DEFAULT_TO_HIDE);
				}
			}
		}
		this.notifyDataSetChanged();
		return true;
	}

	/**
	 * Metoda tworzaca widok przedmiotu na liscie
	 */
	@Override
	public View getChildView(final int boxPosition, final int itemPosition, boolean isLastChild, View view, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ViewHolder holder;

		if (view == null) {
			view = inflater.inflate(R.layout.child_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.item_name);
			holder.add = (ImageView) view.findViewById(R.id.item_toolbar_remove);
			holder.edit = (ImageView) view.findViewById(R.id.item_toolbar_edit);
			holder.location = (TextView) view.findViewById(R.id.group_location);
			holder.name.setTypeface(Fonts.LIGHT_FONT);
			holder.location.setTypeface(Fonts.LIGHT_FONT);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final Item item = this.boxes.get(boxPosition).getItemsList().get(itemPosition);

		if (item == null) {
			return view;
		}

		if ((item.getState() == ItemState.DEFAULT) || (item.getState() == ItemState.NEW)) {
			View toolbar = view.findViewById(R.id.item_toolbar);
			((LayoutParams) toolbar.getLayoutParams()).bottomMargin = -50;
			toolbar.setVisibility(View.GONE);
		}

		final AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
		switch (item.getState()) {
		case NEW:
			item.setState(ItemState.DEFAULT);
			alpha.setDuration(800);
			holder.name.startAnimation(alpha);
			break;
		case REMOVE:
			this.removeItem(view, alpha, boxPosition, itemPosition);
			break;
		case DEFAULT_TO_HIDE:
			item.setState(ItemState.DEFAULT);
			LinearLayout toolbar = (LinearLayout) view.findViewById(R.id.item_toolbar);
			ToggleAnimation animation = new ToggleAnimation(toolbar, 500);
			toolbar.startAnimation(animation);

			AlphaAnimation a = new AlphaAnimation(1, 0);
			a.setDuration(500);
			a.setFillAfter(true);
			a.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					holder.edit.setVisibility(View.GONE);
					holder.add.setVisibility(View.GONE);
				}
			});
			holder.edit.startAnimation(a);
			holder.add.startAnimation(a);
			break;
		}

		holder.name.setText(this.boxes.get(boxPosition).getItemsList().get(itemPosition).getName());
		holder.location.setText(this.boxes.get(boxPosition).getLocation() + ", " + this.boxes.get(boxPosition).getName());
		holder.add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ListAdapter.this.boxes.get(boxPosition).getItemsList().get(itemPosition).setState(ItemState.REMOVE);
				ListAdapter.this.notifyDataSetChanged();
			}

		});
		// edycja przedmiotu
		holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fm = null;
				try {
					fm = ((Activity) ListAdapter.this.context).getFragmentManager();
				} catch (ClassCastException e) {
					Log.d("Fragment", "Can't get the fragment manager with this");
				}

				FragmentTransaction ft = fm.beginTransaction();
				Fragment prev = fm.findFragmentByTag("item-dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);
				DialogFragment newFragment = ItemDialog.newInstance(item, ListAdapter.this, boxPosition);
				newFragment.show(fm, "item-dialog");
				ft.commit();
			}
		});
		return view;
	}

	/**
	 * Metoda wywolywana podczas usuwania przedmiotow Wyswietla okno dialogowe z
	 * zapytaniem czy usunac po czym usuwa przedmiot z bazy i aktualizuje
	 * ListAdapter
	 */
	private void removeItem(final View view, final AlphaAnimation alpha, final int boxPosition, final int itemPosition) {

		final RemoveDialog d = new RemoveDialog();
		d.setMessage(R.string.item_remove_question);
		d.setTitle(R.string.item_remove);
		d.setOnAcceptListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alpha.setDuration(800);
				alpha.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation arg) {
					}

					@Override
					public void onAnimationEnd(Animation arg) {
						Item it = ListAdapter.this.getChild(boxPosition, itemPosition);
						if (it != null) {
							try {
								ObjectKeeper.getInstance().getItemService().delete(it.getId());
								ListAdapter.this.updateListAdapterData();
								ListAdapter.this.notifyDataSetChanged();
							} catch (BusinessException e) {
								Log.e("APP", "There where an issue during item deletion", e);
								Toast.makeText(ListAdapter.this.context, "WystÄ…piÅ‚ bÅ‚Ä…d podczas usuwania przedmiotu", Toast.LENGTH_SHORT).show();
							}
						}
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}
				});
				view.startAnimation(alpha);
				d.dismiss();
			}
		});
		d.setOnRejectListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		FragmentManager fm = ((Activity) ListAdapter.this.context).getFragmentManager();
		d.show(fm, "remove-dialog");
	}

	private void removeBox(final View view, final int boxPosition) {
		final RemoveDialog d = new RemoveDialog();
		d.setMessage(R.string.box_remove_question);
		d.setTitle(R.string.box_remove);
		d.setOnAcceptListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Box box = (Box) ListAdapter.this.getGroup(boxPosition);
				if (box != null) {
					try {
						ObjectKeeper.getInstance().getBoxService().delete(box.getId());
						ListAdapter.this.updateListAdapterData();
						ListAdapter.this.notifyDataSetChanged();
					} catch (BusinessException e) {
						Log.e("APP", "There where an issue during box deletion", e);
						Toast.makeText(ListAdapter.this.context, "Wystąpił błąd podczas usuwania pudełka", Toast.LENGTH_SHORT).show();
					}
				}
				d.dismiss();
			}
		});
		d.setOnRejectListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		FragmentManager fm = ((Activity) ListAdapter.this.context).getFragmentManager();
		d.show(fm, "remove-dialog");
	}

	/**
	 * Metoda tworzy widok pudelek
	 */
	@Override
	public View getGroupView(final int boxPosition, boolean isExpanded, View view, final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ViewHolder holder;

		if (view == null) {
			view = inflater.inflate(R.layout.group_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.group_name);
			holder.location = (TextView) view.findViewById(R.id.group_location);
			holder.add = (ImageView) view.findViewById(R.id.group_add_item);
			holder.remove = (ImageView) view.findViewById(R.id.group_remove_item);
			holder.edit = (ImageView) view.findViewById(R.id.group_edit_item);
			holder.indicator = (ImageView) view.findViewById(R.id.expand_indicator);

			holder.bg = view.findViewById(R.id.expand_bg);
			holder.height = view.findViewById(R.id.expand_height);

			holder.name.setTypeface(Fonts.REGULAR_FONT);
			holder.location.setTypeface(Fonts.REGULAR_FONT);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		Box box = this.boxes.get(boxPosition);
		System.out.println(box.getId().toString());
		if (box.getState() == BoxState.EXPAND) {
			box.setState(BoxState.NORMAL);
			RotateAnimation rotation = new RotateAnimation(0f, 90f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotation.setDuration(100);
			rotation.setInterpolator(new AccelerateInterpolator());
			rotation.setFillAfter(true);

			HeightAnimation height = new HeightAnimation(holder.bg, holder.height.getHeight());
			height.setDuration(100);
			height.setInterpolator(new AccelerateInterpolator());
			height.setFillAfter(true);
			this.bgHeight = holder.bg.getHeight();

			holder.indicator.startAnimation(rotation);
			holder.bg.startAnimation(height);

			AlphaAnimation alpha = new AlphaAnimation(0, 1);
			alpha.setFillAfter(true);
			alpha.setDuration(200);
			alpha.setInterpolator(new LinearInterpolator());
			alpha.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation arg0) {
					holder.edit.setVisibility(View.VISIBLE);
					holder.remove.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
				}
			});
			holder.edit.startAnimation(alpha);
			holder.remove.startAnimation(alpha);

		} else if (box.getState() == BoxState.COLLAPSE) {
			box.setState(BoxState.NORMAL);
			RotateAnimation rotation = new RotateAnimation(90f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotation.setDuration(200);
			rotation.setInterpolator(new LinearInterpolator());
			rotation.setFillAfter(true);

			HeightAnimation height = new HeightAnimation(holder.bg, this.bgHeight);
			height.setDuration(200);
			height.setInterpolator(new AccelerateInterpolator());
			height.setFillAfter(true);

			holder.indicator.startAnimation(rotation);
			holder.bg.startAnimation(height);

			AlphaAnimation alpha = new AlphaAnimation(1, 0);
			alpha.setFillAfter(true);
			alpha.setDuration(200);
			alpha.setInterpolator(new LinearInterpolator());
			alpha.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation arg0) {
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
					holder.edit.setVisibility(View.GONE);
					holder.remove.setVisibility(View.GONE);

				}
			});
			holder.edit.startAnimation(alpha);
			holder.remove.startAnimation(alpha);
		}

		// obsluga guzika dodawania przedmiotu do pudelka
		holder.add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				FragmentManager fm = null;
				try {
					fm = ((Activity) ListAdapter.this.context).getFragmentManager();
				} catch (ClassCastException e) {
					Log.d("Fragment", "Can't get the fragment manager with this", e);
					return;
				}

				FragmentTransaction ft = fm.beginTransaction();
				Fragment prev = fm.findFragmentByTag("item-dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);
				DialogFragment newFragment = ItemDialog.newInstance(null, ListAdapter.this, boxPosition);
				newFragment.show(fm, "item-dialog");
				ft.commit();
			}
		});

		holder.remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ListAdapter.this.removeBox(view, boxPosition);
			}
		});

		holder.edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				FragmentTransaction ft = ((Activity) ListAdapter.this.context).getFragmentManager().beginTransaction();
				Fragment prev = ((Activity) ListAdapter.this.context).getFragmentManager().findFragmentByTag("wizard");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);

				// Create and show the dialog.
				DialogFragment newFragment = new BoxDialog((Box) ListAdapter.this.getGroup(boxPosition));
				newFragment.show(ft, "wizard");
			}
		});

		holder.name.setText(this.boxes.get(boxPosition).getName());
		holder.location.setText(this.boxes.get(boxPosition).getLocation());
		return view;
	}

	/**
	 * Zwraca pudelko na podstawie jego pozycji na liscie
	 */
	@Override
	public Object getGroup(int boxPosition) {
		if (this.getGroupCount() >= boxPosition) {
			return this.boxes.get(boxPosition);
		}
		return null;
	}

	/**
	 * Zwraca aktualna liczbe pudelek
	 */
	@Override
	public int getGroupCount() {
		if (this.boxes != null) {
			return this.boxes.size();
		}
		return 0;
	}

	/**
	 * Zwraca id pudelka z podanej pozycji
	 */
	@Override
	public long getGroupId(int boxPosition) {
		Box box = ((Box) this.getGroup(boxPosition));
		if (box != null) {
			box.getId().getMostSignificantBits();
		}
		return 0l;
	}

	@Override
	public boolean isChildSelectable(int boxPosition, int itemPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	/**
	 * Zwraca przedmiot z podanego pudelka
	 */
	@Override
	public Item getChild(int groupPosition, int itemPosition) {
		return this.boxes.get(groupPosition).getItemsList().get(itemPosition);
	}

	/**
	 * Metoda pobiera identyfikator przedmiotu (identyfikator w obrebie
	 * wyswietlanej listy)
	 */
	@Override
	public long getChildId(int boxPosition, int itemPosition) {
		Item item = this.getChild(boxPosition, itemPosition);
		if (item != null) {
			return item.getId().getMostSignificantBits();
		}
		return 0l;
	}

	/**
	 * Zwraca liczbe przedmiotow w pudelku
	 */
	@Override
	public int getChildrenCount(int boxPosition) {
		return this.boxes.get(boxPosition).getItems().size();
	}

	/**
	 * Metoda wyszukujaca przedmiot na podstawie ciagu znakow
	 * 
	 * @throws BusinessException
	 */
	public void searchFor(String searchName) throws BusinessException {
		if (SearchType.NFC.getSearchTag().equals(searchName)) {
			return;
		}

		if ((searchName != null) && (this.searchText.length() < searchName.length()) && searchName.startsWith(this.searchText)) {
			this.boxes = ObjectKeeper.getInstance().getItemService().getByLikelyItemName(this.boxes, searchName);
		} else {
			this.boxes = ObjectKeeper.getInstance().getItemService().getByLikelyItemName(ObjectKeeper.getInstance().getBoxList(), searchName);
		}

		this.searchText = searchName;
		this.notifyDataSetChanged();
	}

	/**
	 * Metoda wyswietlajaca pudelko na podstawie jego ID
	 * 
	 * @throws BusinessException
	 */
	public boolean searchForBox(String boxId) throws BusinessException {
		Log.i("APP", "Searching for Box id: " + boxId);
		Box box;
		box = ObjectKeeper.getInstance().getBoxService().get(boxId);
		if (box != null) {
			List<Box> boxesList = new ArrayList<Box>();
			boxesList.add(box);
			this.boxes = boxesList;
			this.notifyDataSetChanged();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Klasa pomocnicza zapewniajaca lepsza wydajnosc
	 */
	class ViewHolder {
		public TextView name;
		public TextView location;
		public ImageView add;
		public ImageView edit;
		public ImageView remove;
		public ImageView indicator;
		public View bg;
		public View height;
	}
}