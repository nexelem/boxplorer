package com.nexelem.boxeee;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.nexelem.boxeee.db.BusinessException;
import com.nexelem.boxeee.db.DBHelper;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.service.BoxService;
import com.nexelem.boxeee.service.ItemService;
import com.nexelem.boxeee.wizard.WizardBox;

public class MainActivity extends FragmentActivity implements OnQueryTextListener {

	private DBHelper helper = null;
	private BoxService boxService = null;
	private ItemService itemService = null;
	private WizardBox wizard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.helper = new DBHelper(this.getApplicationContext());
		try {
			this.boxService = new BoxService(this.helper);
			this.itemService = new ItemService(this.helper);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Creating adapter with data
		ListAdapter adapter = new ListAdapter(this, getData());

		// Creating expandable list view
		ExpandableListView list = (ExpandableListView) findViewById(R.id.listView);
		list.setAdapter(adapter);
		list.setOnChildClickListener(adapter);
		list.requestFocus();

		// Customizing action bar
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(false);
		bar.setDisplayShowTitleEnabled(false);
		bar.setDisplayShowHomeEnabled(false);
		bar.setDisplayShowCustomEnabled(true);
		bar.setCustomView(R.layout.actionbar);

		// Customizing search view
		SearchView searcher = (SearchView) findViewById(R.id.searcher);
		searcher.setOnQueryTextListener(this);
		searcher.setIconified(false);
		
		// Creating add box wizard dialog
		ImageView addBox = (ImageView) findViewById(R.id.add_box);
		addBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new WizardBox().show(getSupportFragmentManager(), "dialog");
			}
		});
		
	}

	/**
	 * TODO: Handling search methods
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.nfc:
			Toast.makeText(this, "TODO: " + item.getTitle(), Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.qr_code:
			Toast.makeText(this, "TODO: " + item.getTitle(), Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.voice:
			Toast.makeText(this, "TODO: " + item.getTitle(), Toast.LENGTH_SHORT)
					.show();
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_bar, menu);
		return true;
	}

	/**
	 * TODO: Loading data from database
	 * 
	 * @return
	 */
	public List<Box> getData() {
		List<Box> boxes = new ArrayList<Box>();
		if (this.boxService != null) {
			try {
				boxes = this.boxService.list();
			} catch (BusinessException e) {
				Toast.makeText(this,
						"Application error: unable to get boxes list",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}

		return boxes;

		/**
		 * List<Box> boxes = new ArrayList<Box>(); Box box1 = new Box("Gry",
		 * "Pokój gościnny"); box1.getItems().add(new Item("GTA 3"));
		 * box1.getItems().add(new Item("FEZ!")); box1.getItems().add(new
		 * Item("Red Dead Redemption"));
		 * 
		 * Box box2 = new Box("Narzędzia", "Przedpokój");
		 * box2.getItems().add(new Item("Śrubokręt")); box2.getItems().add(new
		 * Item("Młotek")); box2.getItems().add(new Item("Piła"));
		 * box2.getItems().add(new Item("Latarka"));
		 * 
		 * Box box3 = new Box("Muzyka", "Gabinet"); box3.getItems().add(new
		 * Item("Lana del Ray")); box3.getItems().add(new Item("String"));
		 * box3.getItems().add(new Item("James Blunt"));
		 * 
		 * boxes.add(box1); boxes.add(box2); boxes.add(box3);
		 * 
		 * return boxes;
		 **/
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO: Filtering data
		Toast.makeText(this, "Szukam: " + newText, Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}
}
