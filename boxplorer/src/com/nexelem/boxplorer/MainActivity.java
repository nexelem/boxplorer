package com.nexelem.boxplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
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

import com.nexelem.boxplorer.adapter.ListAdapter;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.db.DBHelper;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.search.SearchType;
import com.nexelem.boxplorer.service.BoxService;
import com.nexelem.boxplorer.service.ItemService;
import com.nexelem.boxplorer.utils.NfcTagReader;
import com.nexelem.boxplorer.utils.ObjectKeeper;
import com.nexelem.boxplorer.wizard.BoxDialog;

/**
 * Klasa wejsciowa do aplikacji
 * 
 * @author bartek wilczynski, darek zon
 */
public class MainActivity extends Activity implements OnQueryTextListener {

	/**
	 * 
	 */
	private ListAdapter adapter;
	private ExpandableListView list;
	private NfcAdapter nfcAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		// Loading fonts
		Fonts.loadFonts(this);

		DBHelper helper = new DBHelper(this.getApplicationContext());
		try {
			ObjectKeeper.getInstance().setBoxService(new BoxService(helper));
			ObjectKeeper.getInstance().setItemService(new ItemService(helper));
		} catch (BusinessException e) {
			Log.e("APP", "Unable to get required DB Services", e);
			throw new RuntimeException();
		}

		// Creating adapter with data
		this.adapter = new ListAdapter(this);

		// Creating expandable list view
		this.list = (ExpandableListView) this.findViewById(R.id.listView);
		this.list.setAdapter(this.adapter);
		this.list.setOnChildClickListener(this.adapter);
		this.list.requestFocus();

		// Customizing action bar
		ActionBar bar = this.getActionBar();
		bar.setDisplayHomeAsUpEnabled(false);
		bar.setDisplayShowTitleEnabled(false);
		bar.setDisplayShowHomeEnabled(false);
		bar.setDisplayShowCustomEnabled(true);
		bar.setCustomView(R.layout.actionbar);

		// Setting add button action
		ImageView addBox = (ImageView) this.findViewById(R.id.add_box);
		addBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentTransaction ft = MainActivity.this.getFragmentManager().beginTransaction();
				Fragment prev = MainActivity.this.getFragmentManager().findFragmentByTag("wizard");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);

				// Create and show the dialog.
				DialogFragment newFragment = new BoxDialog();
				newFragment.show(ft, "wizard");
			}
		});

		// Customizing search view
		SearchView searcher = (SearchView) this.findViewById(R.id.searcher);
		searcher.setOnQueryTextListener(this);
		searcher.setIconified(false);

		this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
	}

	/**
	 * TODO: Handling search methods
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.nfc:
			Toast.makeText(this, "TODO: " + item.getTitle(), Toast.LENGTH_SHORT).show();
			break;
		case R.id.qr_code:
			this.readQrCode();
			break;
		case R.id.voice:
			this.readVoice();
			break;
		}
		return true;
	}

	private void readVoice() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition");
		this.startActivityForResult(intent, SearchType.VOICE.ordinal());
	}

	private void readQrCode() {
		Intent intent = new Intent("nex.com.google.zxing.client.android.SCAN");
		intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
		this.startActivityForResult(intent, SearchType.QR.ordinal());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.search_bar, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		PackageManager pm = this.getApplicationContext().getPackageManager();
		// sprawdzamy czy mozna rozpoznawac glos
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			menu.findItem(R.id.voice).setVisible(false);
		}

		// sprawdzamy czy jest dostepne NFC
		if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
			menu.findItem(R.id.nfc).setVisible(false);
		}

		// sprawdzamy czy jest dostepna kamera
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			menu.findItem(R.id.qr_code).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * TODO: Loading data from database
	 * 
	 * @return
	 */
	public List<Box> getData() {
		List<Box> boxes = new ArrayList<Box>();
		if (ObjectKeeper.getInstance().getBoxService() != null) {
			try {
				boxes = ObjectKeeper.getInstance().getBoxService().list();
			} catch (BusinessException e) {
				Toast.makeText(this, "Application error: unable to get boxes list", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}

		return boxes;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (newText.length() < 3) {
			this.adapter.setBoxes(this.adapter.getFullList());
			this.adapter.notifyDataSetChanged();
			this.collapseAll();
		} else if (Arrays.asList(SearchType.QR.getSearchTag(), SearchType.NFC.getSearchTag()).contains(newText)) {
			return true;
		} else if (newText.length() >= 3) {
			try {
				this.adapter.searchFor(newText);
				this.expandAll();
				Toast.makeText(this, "Szukam: " + newText, Toast.LENGTH_SHORT).show();
			} catch (BusinessException e) {
				Toast.makeText(this, "ERROR: " + newText, Toast.LENGTH_SHORT).show();
				Log.e("APP", "Wystapil blad podczas poszukiwania przedmiotu", e);
			}

		}
		return true;
	}

	private void collapseAll() {
		for (int i = 0; i < this.adapter.getGroupCount(); i++) {
			this.list.collapseGroup(i);
		}
	}

	private void expandAll() {
		for (int i = 0; i < this.adapter.getGroupCount(); i++) {
			this.list.expandGroup(i, true);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	/**
	 * Metoda obslugujaca wartosci zwracane przez uruchomienie aktywnosci
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SearchType.QR.ordinal()) {
			this.handleQrCodeResult(resultCode, data);

		} else if (requestCode == SearchType.VOICE.ordinal()) {
			this.handleVoiceResult(resultCode, data);
		}
	}

	/**
	 * Metoda obslugujaca wyszukiwanie glosowe Pobiera pierszy element z listy
	 * rozpoznanych slow i wyszukuje przedmiotu
	 */
	private void handleVoiceResult(int resultCode, Intent data) {
		List<String> contents = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		SearchView searcher = (SearchView) this.findViewById(R.id.searcher);
		if ((searcher != null) && (contents != null)) {
			searcher.setQuery(contents.get(0), true);
		}
		this.expandAll();
	}

	/**
	 * Metoda obslugujaca wyszukiwanie po kodzie QR. Pobiera rozpoznany element
	 * i probuje wyszukac na jego podstawie pudelko
	 */
	private void handleQrCodeResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String contents = data.getStringExtra("SCAN_RESULT");
			Log.i("QR", "QR READED AS: " + contents);
			try {
				this.adapter.searchForBox(contents);
				SearchView searcher = (SearchView) this.findViewById(R.id.searcher);
				if (searcher != null) {
					searcher.setQuery(":qr", false);
				}
				this.expandAll();
			} catch (BusinessException e) {
				Toast.makeText(this.getApplicationContext(), "Unable to find Box", Toast.LENGTH_SHORT).show();
				Log.w("QR", "Error while searching box " + contents, e);
			}
		} else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this.getApplicationContext(), "Unable to find Box", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		String id = NfcTagReader.getUUID(tagFromIntent);
		try {
			this.adapter.searchForBox(id);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SearchView searcher = (SearchView) this.findViewById(R.id.searcher);
		if (searcher != null) {
			searcher.setQuery(":nfc", false);
		}
		this.expandAll();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(this.nfcAdapter != null)
			this.nfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		String[][] techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("application/com.nex.boxplorer");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		IntentFilter[] intentFiltersArray = new IntentFilter[] { ndef, };
		if(this.nfcAdapter != null)
			this.nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
	}

}
