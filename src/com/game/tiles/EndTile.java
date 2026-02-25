package com.game.tiles;

public class EndTile extends Tile {

	public EndTile(String property) {
		configureDirectional(property, "End");
		this.canMove = false;
	}
}
