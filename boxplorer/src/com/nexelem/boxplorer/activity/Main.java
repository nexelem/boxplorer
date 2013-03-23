package com.nexelem.boxplorer.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nexelem.boxplorer.Fonts;
import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.adapter.ListAdapter;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.db.DBHelper;
import com.nexelem.boxplorer.enums.SearchType;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.service.BoxService;
import com.nexelem.boxplorer.service.ItemService;
import com.nexelem.boxplorer.utils.NfcUtils;
import com.nexelem.boxplorer.utils.ObjectKeeper;
import com.nexelem.boxplorer.wizard.BoxDialog;

/**
 * Klasa wejsciowa do aplikacji
 * 
 * @author bartek wilczynski, darek zon
 */
public class Main extends Activity implements TextWatcher {

	private static final String TAG = Main.class.getName();

	private ListAdapter adapter;
	private ExpandableListView list;
	private ImageView clear;
	private NfcAdapter nfcAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		if (NfcUtils.isNfcAvailable(this.getApplicationContext())) {
			this.nfcAdapter = NfcAdapter.getDefaultAdapter(this.getApplicationContext());
		}

		// Loading fonts
		Fonts.loadFonts(this);

		DBHelper helper = new DBHelper(this.getApplicationContext());
		try {
			ObjectKeeper.getInstance().setBoxService(new BoxService(helper));
			ObjectKeeper.getInstance().setItemService(new ItemService(helper));
		} catch (BusinessException e) {
			Log.e(TAG, "Unable to get required DB Services", e);
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
				FragmentManager fm = Main.this.getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				Fragment prev = Main.this.getFragmentManager().findFragmentByTag("wizard");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);
				// Create and show the dialog.
				DialogFragment newFragment = new BoxDialog();
				newFragment.show(ft, "wizard");
				// ft.commit();
			}
		});

		this.handleTagReceive(this.getIntent());

		// Customizing search view
		final TextView searcher = (TextView) this.findViewById(R.id.searcher);
		searcher.addTextChangedListener(this);
		searcher.setTypeface(Fonts.LIGHT_FONT);
		this.clear = (ImageView) this.findViewById(R.id.searcher_clear);
		this.clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searcher.setText("");
				searcher.clearFocus();
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
			this.readNfcTag();
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

	private void readNfcTag() {
		DialogFragment df = (new DialogFragment() {
			@Override
			public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
			}

			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				this.getDialog().setTitle(R.string.nfc_logo);
				View v = inflater.inflate(R.layout.wizard_box_3, container, false);
				return v;
			}
		});

		df.show(this.getFragmentManager(), "findByNfc");

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
		if (!NfcUtils.isNfcAvailable(this.getApplicationContext())) {
			menu.findItem(R.id.nfc).setVisible(false);
		}

		// sprawdzamy czy jest dostepna kamera
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
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
				Log.e(TAG, "Unable to get boxes", e);
			}
		}

		return boxes;
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
		if (data == null) { // anulowalismy wyszukiwanie glosowe
			return;
		}
		List<String> contents = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		TextView searcher = (TextView) this.findViewById(R.id.searcher);
		if ((searcher != null) && (contents != null)) {
			searcher.setText(contents.get(0));
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
				TextView searcher = (TextView) this.findViewById(R.id.searcher);
				if (searcher != null) {
					searcher.setText(":qr");
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
		this.handleTagReceive(intent);
	}

	private void handleTagReceive(Intent intent) {
		if (intent == null) {
			return;
		}
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tagFromIntent == null) {
			return;
		}
		String id;
		try {
			id = NfcUtils.getContent(tagFromIntent);
		} catch (BusinessException e1) {
			Toast.makeText(this.getApplicationContext(), R.string.nfc_unable_to_read, Toast.LENGTH_SHORT);
			return;
		}
		try {
			this.adapter.searchForBox(id);
			TextView searcher = (TextView) this.findViewById(R.id.searcher);
			if (searcher != null) {
				searcher.setText(":nfc");
			}
			this.expandAll();
		} catch (BusinessException e) {
			Toast.makeText(this.getApplicationContext(), R.string.box_not_found, Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (NfcUtils.isNfcAvailable(this.getApplicationContext())) {
			this.nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (NfcUtils.isNfcAvailable(this.getApplicationContext())) {
			this.nfcAdapter.enableForegroundDispatch(this, NfcUtils.getPendingIntent(this), NfcUtils.getIntentFilters(), NfcUtils.getTechList());
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String newText = s.toString();
		this.clear.setVisibility(newText.length() > 0 ? View.VISIBLE : View.GONE);
		if (newText.equals(":nfc") || newText.equals(":qr")) {
			return;
		}

		if (newText.length() < 3) {
			this.adapter.setBoxes(this.adapter.getFullList());
			this.adapter.notifyDataSetChanged();
			this.collapseAll();
		} else if (newText.length() >= 3) {
			try {
				this.adapter.searchFor(newText);
				this.expandAll();
				Toast.makeText(this, "Szukam: " + newText, Toast.LENGTH_SHORT).show();
			} catch (BusinessException e) {
				Toast.makeText(this, "ERROR: " + newText, Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Wystapil blad podczas poszukiwania przedmiotu", e);
			}

		}
	}

}
