package com.nexelem.boxplorer;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.db.DBHelper;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.service.BoxService;
import com.nexelem.boxplorer.service.ItemService;

public class MainActivity extends Activity implements OnQueryTextListener {

	private DBHelper helper = null;
	private BoxService boxService;
	private ItemService itemService;
	private ListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		this.helper = new DBHelper(this.getApplicationContext());
		try {
			this.boxService = new BoxService(this.helper);
			this.itemService = new ItemService(this.helper);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Creating adapter with data
		this.adapter = new ListAdapter(this, this.boxService, this.itemService);

		// Creating expandable list view
		ExpandableListView list = (ExpandableListView) this
				.findViewById(R.id.listView);
		list.setAdapter(this.adapter);
		list.setOnChildClickListener(this.adapter);
		list.requestFocus();

		// Customizing action bar
		ActionBar bar = this.getActionBar();
		bar.setDisplayHomeAsUpEnabled(false);
		bar.setDisplayShowTitleEnabled(false);
		bar.setDisplayShowHomeEnabled(false);
		bar.setDisplayShowCustomEnabled(true);
		bar.setCustomView(R.layout.actionbar);

		// Customizing search view
		SearchView searcher = (SearchView) this.findViewById(R.id.searcher);
		searcher.setOnQueryTextListener(this);
		searcher.setIconified(false);
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
			this.readQrCode();
			break;
		case R.id.voice:
			Toast.makeText(this, "TODO: " + item.getTitle(), Toast.LENGTH_SHORT)
					.show();
			break;
		}
		return true;
	}

	private void readQrCode() {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE",
				"QR_CODE_MODE");
		this.startActivityForResult(intent, 666);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = this.getMenuInflater();
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
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (newText.length() < 3) {
			this.adapter.setBoxes(this.adapter.getFullList());
		} else if (newText.length() >= 3) {
			try {
				this.adapter.searchFor(newText);
				Toast.makeText(this, "Szukam: " + newText, Toast.LENGTH_SHORT)
						.show();
			} catch (BusinessException e) {
				Toast.makeText(this, "ERROR: " + newText, Toast.LENGTH_SHORT)
						.show();
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 666) {
			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				String format = data.getStringExtra("SCAN_RESULT_FORMAT");
				System.out.println("xZing contents: " + contents + " format: "
						+ format);
				Log.i("QR", "QR READED AS: " + contents);
				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
				System.out.println("xZing Cancelled");
			}
		}
	}
}
