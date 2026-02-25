package com.game.tiles;

import javafx.scene.image.ImageView;

public class EmptyTile extends Tile {

	public EmptyTile(String property) {
		this.property = property;
		if (property.equals("none")) {
			this.image = new ImageView("resources/images/Empty.jpg");
			this.canMove = true;
		} else if (property.equals("Free")) {
			this.image = new ImageView("resources/images/EmptyFree.jpeg");
			this.canMove = false;
		}
	}
}
