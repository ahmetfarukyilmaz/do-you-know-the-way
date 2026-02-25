package com.game.tiles;

public class PipeTile extends Tile {

	public PipeTile(String property) {
		configurePipe(property, "Pipe", "CurvedPipe");
		this.canMove = true;
	}
}
