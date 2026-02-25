package com.game.tiles;

import com.game.utils.Motion;
import javafx.scene.image.ImageView;

public abstract class Tile {

    protected ImageView image;
    protected String property;
    protected boolean canMove;
    protected boolean isTopFree;
    protected boolean isBottomFree;
    protected boolean isRightFree;
    protected boolean isLeftFree;
    private final Motion motion;

    public Tile() {
        this.motion = new Motion();
    }

    protected void configurePipe(String property, String straightPrefix, String curvedPrefix) {
        this.property = property;
        switch (property) {
            case "Horizontal":
                this.image = new ImageView("resources/images/" + straightPrefix + " Horizontal.jpeg");
                this.isLeftFree = true;
                this.isRightFree = true;
                break;
            case "Vertical":
                this.image = new ImageView("resources/images/" + straightPrefix + " Vertical.jpeg");
                this.isTopFree = true;
                this.isBottomFree = true;
                break;
            case "00":
                this.image = new ImageView("resources/images/" + curvedPrefix + " 00.jpeg");
                this.isLeftFree = true;
                this.isTopFree = true;
                break;
            case "01":
                this.image = new ImageView("resources/images/" + curvedPrefix + " 01.jpeg");
                this.isTopFree = true;
                this.isRightFree = true;
                break;
            case "10":
                this.image = new ImageView("resources/images/" + curvedPrefix + " 10.jpeg");
                this.isLeftFree = true;
                this.isBottomFree = true;
                break;
            case "11":
                this.image = new ImageView("resources/images/" + curvedPrefix + " 11.jpeg");
                this.isRightFree = true;
                this.isBottomFree = true;
                break;
        }
    }

    protected void configureDirectional(String property, String prefix) {
        this.property = property;
        this.image = new ImageView("resources/images/" + prefix + " " + property + ".jpeg");
        switch (property) {
            case "HorizontalR": this.isRightFree = true; break;
            case "HorizontalL": this.isLeftFree = true; break;
            case "VerticalB":   this.isBottomFree = true; break;
            case "VerticalT":   this.isTopFree = true; break;
        }
    }

    public boolean isMove() { return canMove; }
    public ImageView getImage() { return image; }
    public String getProperty() { return property; }
    public boolean isTopFree() { return isTopFree; }
    public boolean isBottomFree() { return isBottomFree; }
    public boolean isRightFree() { return isRightFree; }
    public boolean isLeftFree() { return isLeftFree; }
    public Motion getMotion() { return motion; }
}
