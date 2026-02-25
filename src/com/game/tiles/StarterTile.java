package com.game.tiles;

public class StarterTile extends Tile {

	public StarterTile(String property) {
		configureDirectional(property, "Starter");
		this.canMove = false;
	}
}
