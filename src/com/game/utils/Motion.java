package com.game.utils;

public class Motion {

	private boolean isMoving;
	private boolean isMovingUp;
	private boolean isMovingDown;
	private boolean isMovingLeft;
	private boolean isMovingRight;

	public boolean canMoveDown() { return !isMoving || isMovingDown; }
	public boolean canMoveUp() { return !isMoving || isMovingUp; }
	public boolean canMoveLeft() { return !isMoving || isMovingLeft; }
	public boolean canMoveRight() { return !isMoving || isMovingRight; }

	public void setMoving(boolean moving) { isMoving = moving; }
	public void setMovingUp(boolean v) { isMovingUp = v; }
	public void setMovingDown(boolean v) { isMovingDown = v; }
	public void setMovingLeft(boolean v) { isMovingLeft = v; }
	public void setMovingRight(boolean v) { isMovingRight = v; }
}
