package com.nexelem.boxeee.test.populate;

import org.apache.commons.lang.math.RandomUtils;

import com.nexelem.boxeee.db.BusinessException;
import com.nexelem.boxeee.model.Box;
import com.nexelem.boxeee.service.BoxService;

public class BoxPopulator {

	private static final String BOX_NAME = "box_";
	private static final String BOX_LOCATION = "location_";

	private BoxService boxService = null;

	public BoxPopulator(BoxService service) {
		this.boxService = service;
	}

	public Box createBox() throws BusinessException {
		Box box = new Box(BOX_NAME + RandomUtils.nextInt(), BOX_LOCATION
				+ RandomUtils.nextInt());
		boxService.create(box);
		return box;
	}

	public void createBoxes(int boxesNum) throws BusinessException {
		for (int i = 0; i < boxesNum; ++i) {
			this.createBox();
		}
	}
}
