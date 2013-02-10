package com.nexelem.boxeee.test;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;

import com.nexelem.boxeee.test.populate.BoxPopulator;
import com.nexelem.boxeee.test.populate.ItemPopulator;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.db.DBHelper;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.model.Item;
import com.nexelem.boxplorer.service.BoxService;
import com.nexelem.boxplorer.service.ItemService;
import com.nexelem.boxplorer.test.R;

public class ItemServiceTest extends android.test.AndroidTestCase {

	private DBHelper helper = null;
	private ItemService service = null;
	private BoxService boxService = null;
	private ItemPopulator populator = null;
	private BoxPopulator boxPopulator = null;

	protected void setUp() {
		this.helper = new DBHelper(this.mContext);
		try {
			this.service = new ItemService(this.helper);
			this.boxService = new BoxService(this.helper);
		} catch (BusinessException e) {
			throw new RuntimeException();
		}
		this.populator = new ItemPopulator(this.service);
		this.boxPopulator = new BoxPopulator(this.boxService);
	}

	protected void tearDown() {
		this.helper.clearDatabase();
		this.helper.close();
	}

	public void testCreateBoxWithItem() throws BusinessException {
		Box box = this.boxPopulator.createBox();

		Box savedBox = boxService.get(box.getId());
		assertEquals(box.getName(), savedBox.getName());
		assertEquals(box.getId(), savedBox.getId());

		Item item = populator.createItem();
		item.setBox(savedBox);
		service.create(item);

		Box boxWithItems = boxService.get(savedBox.getId());

		assertEquals(1, boxWithItems.getItemsList().size());
	}

	public void testEditItem() throws BusinessException {
		Item item = populator.createItem();
		service.create(item);

		Item savedItem = service.get(item.getId());
		assertEquals(item.getId(), savedItem.getId());
		assertEquals(item.getName(), savedItem.getName());

		String newName = "_" + RandomUtils.nextInt();
		savedItem.setName(newName);
		service.update(savedItem);

		Item updatedItem = service.get(item.getId());
		assertEquals(newName, updatedItem.getName());
	}

	public void testRemoveItem() throws BusinessException {
		Item item = populator.createItem();
		service.create(item);

		Item savedItem = service.get(item.getId());
		assertEquals(item.getId(), savedItem.getId());
		assertEquals(item.getName(), savedItem.getName());

		service.delete(item.getId());

		Item deletedItem = service.get(item.getId());
		assertNull(deletedItem);
	}

	public void testFindingByLikelyItemName() throws BusinessException {
		Box box = this.boxPopulator.createBox();

		populator.populateItems(10, box);
		Item item1 = new Item("Wiertarka", box);
		Item item2 = new Item("Tarka", box);
		Item item3 = new Item("Deska", box);
		this.service.create(item1);
		this.service.create(item2);
		this.service.create(item3);

		Box savedBox = this.boxService.get(box.getId());
		assertEquals(13, savedBox.getItems().size());

		List<Box> boxes = this.boxService.list();
		List<Box> findByName = this.service.getByLikelyItemName(boxes, "ar");
		assertEquals(1, findByName.size());
		assertEquals(2, findByName.get(0).getItems().size());

		List<Box> findByName2 = this.service.getByLikelyItemName(boxes, "ka");
		assertEquals(1, findByName2.size());
		assertEquals(3, findByName2.get(0).getItems().size());

		List<Box> findByName3 = this.service.getByLikelyItemName(boxes, "Ta");
		assertEquals(1, findByName3.size());
		assertEquals(2, findByName3.get(0).getItems().size());

	}

	public void testMultiBoxFindingByLikelyItemName() throws BusinessException {
		Box box1 = this.boxPopulator.createBox();
		Box box2 = this.boxPopulator.createBox();
		populator.populateItems(15, box1);
		populator.populateItems(15, box2);
		Item item1 = new Item("Wiertarka", box1);
		Item item2 = new Item("Tarka", box1);
		Item item3 = new Item("Deska", box1);
		Item item4 = new Item("Maszynka", box2);
		Item item5 = new Item("Kokos", box2);
		Item item6 = new Item("Tatar", box2);
		service.create(item1);
		service.create(item2);
		service.create(item3);
		service.create(item4);
		service.create(item5);
		service.create(item6);

		List<Box> boxes = this.boxService.list();

		List<Box> findByName = this.service.getByLikelyItemName(boxes, "ar");
		assertEquals(2, findByName.size());
		assertEquals(2, findByName.get(0).getItems().size());
		assertEquals(1, findByName.get(1).getItems().size());
	}
}
