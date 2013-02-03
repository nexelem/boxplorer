package com.nexelem.boxeee;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

import com.example.boxeee.R;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.model.Item;

public class MainActivity extends Activity implements OnQueryTextListener {


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Creating adapter with data
        ListAdapter adapter = new ListAdapter(this, getData());
        
        // Creating expandable list view
        ExpandableListView list = (ExpandableListView)findViewById(R.id.listView);
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
    }
	
	
	/**
	 * TODO: Handling search methods
	 */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    	case R.id.nfc:
    		Toast.makeText(this, "TODO: "+item.getTitle(), Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.qr_code:
    		Toast.makeText(this, "TODO: "+item.getTitle(), Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.voice:
    		Toast.makeText(this, "TODO: "+item.getTitle(), Toast.LENGTH_SHORT).show();
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
	 * @return
	 */
    public List<Box> getData(){
    	List<Box> boxes = new ArrayList<Box>();
    	Box box1 = new Box("Gry");
    	box1.add(new Item("GTA 3"));
    	box1.add(new Item("FEZ!"));
    	box1.add(new Item("Red Dead Redemption"));
    	
    	Box box2 = new Box("Narzędzia");
    	box2.add(new Item("Śrubokręt"));
    	box2.add(new Item("Młotek"));
    	box2.add(new Item("Piła"));
    	box2.add(new Item("Latarka"));

    	Box box3 = new Box("Muzyka");
    	box3.add(new Item("Lana del Ray"));
    	box3.add(new Item("String"));
    	box3.add(new Item("James Blunt"));
    	
    	boxes.add(box1);
    	boxes.add(box2);
    	boxes.add(box3);
    	
    	return boxes;
    }

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO: Filtering data
		Toast.makeText(this, "Szukam: "+newText, Toast.LENGTH_SHORT).show();
		return true;
	}



	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}
}
