package com.nexelem.boxeee.test;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import com.nexelem.boxeee.db.BusinessException;
import com.nexelem.boxeee.db.DBHelper;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.model.Item;
import com.nexelem.boxeee.service.BoxService;
import com.nexelem.boxeee.service.ItemService;
import com.nexelem.boxeee.test.populate.BoxPopulator;
import com.nexelem.boxeee.test.populate.ItemPopulator;
import com.nexelem.boxplorer.test.R;

public class BoxServiceTest extends android.test.AndroidTestCase {

	private DBHelper helper = null;
	private BoxService boxService = null;
	private ItemService itemService = null;
	private BoxPopulator boxPopulator = null;
	private ItemPopulator itemPopulator = null;

	@Override
	protected void setUp() {
		this.helper = new DBHelper(this.mContext);
		try {
			this.boxService = new BoxService(this.helper);
			this.itemService = new ItemService(this.helper);
		} catch (BusinessException e) {
			throw new RuntimeException();
		}
		this.boxPopulator = new BoxPopulator(this.boxService);
		this.itemPopulator = new ItemPopulator(this.itemService);
	}

	@Override
	protected void tearDown() {
		this.helper.clearDatabase();
		this.helper.close();
	}

	public void testCreateBox() throws BusinessException {
		Box box = this.boxPopulator.createBox();

		Box box2 = this.boxService.get(box.getId());
		assertEquals(box.getName(), box2.getName());
		assertEquals(box.getId(), box2.getId());
	}

	public void testEditBox() throws BusinessException {
		Box box = this.boxPopulator.createBox();

		Box savedBox = this.boxService.get(box.getId());
		assertEquals(box.getName(), savedBox.getName());
		String boxName = "_box" + RandomUtils.nextInt();
		savedBox.setName(boxName);
		this.boxService.update(savedBox);

		Box updatedBox = this.boxService.get(box.getId());
		assertEquals(updatedBox.getName(), boxName);
		assertEquals(box.getId(), updatedBox.getId());
	}

	public void testRemoveEmptyBox() throws BusinessException {
		Box box = this.boxPopulator.createBox();

		Box savedBox = this.boxService.get(box.getId());
		assertEquals(box.getName(), savedBox.getName());

		this.boxService.delete(box.getId());

		Box removedBox = this.boxService.get(box.getId());
		assertNull(removedBox);
	}

	public void testListBox() throws BusinessException {
		int boxNum = 10;
		this.boxPopulator.createBoxes(boxNum);
		List<Box> boxes = this.boxService.list();
		assertEquals(boxNum, boxes.size());
	}

	public void testDeleteBox() throws BusinessException {
		Box box1 = this.boxPopulator.createBox();
		Box box2 = this.boxPopulator.createBox();
		this.itemPopulator.populateItems(15, box1);
		this.itemPopulator.populateItems(15, box2);
		Item item1 = new Item("Wiertarka", box1);
		Item item2 = new Item("Tarka", box1);
		Item item3 = new Item("Deska", box1);
		Item item4 = new Item("Maszynka", box2);
		Item item5 = new Item("Kokos", box2);
		Item item6 = new Item("Tatar", box2);
		this.itemService.create(item1);
		this.itemService.create(item2);
		this.itemService.create(item3);
		this.itemService.create(item4);
		this.itemService.create(item5);
		this.itemService.create(item6);

		List<Box> boxes = this.boxService.list();
		assertEquals(2, boxes.size());

		this.boxService.delete(box1.getId());

		List<Box> afterDeleteboxes = this.boxService.list();
		assertEquals(1, afterDeleteboxes.size());

		List<Item> itemsList = this.itemService.list();
		assertEquals(18, itemsList.size());
	}
}
