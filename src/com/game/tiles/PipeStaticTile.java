package com.game.tiles;

public class PipeStaticTile extends Tile {

	public PipeStaticTile(String property) {
		configurePipe(property, "PipeStatic", "CurvedStatic");
		this.canMove = false;
	}
}
