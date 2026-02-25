package com.game.launch;

import java.io.InputStream;
import java.util.Scanner;

import com.game.tiles.*;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;

public class GameBoard extends Pane {

    private static final int GRID = 4;
    private static final int DRAG_THRESHOLD = 50;

    private final Tile[][] tiles = new Tile[GRID][GRID];
    private final int size = 120;
    private int move = 0;

    private final AudioClip slideSound;
    private final AudioClip wrongSlideSound;
    private final AudioClip completeSound;

    private Path correctPath;
    private boolean isFalse;

    public GameBoard(String level) {
        slideSound = new AudioClip("file:src/resources/sounds/slideSound.wav");
        wrongSlideSound = new AudioClip("file:src/resources/sounds/ErrorSound.wav");
        completeSound = new AudioClip("file:src/resources/sounds/victorySound.wav");

        initialize();

        InputStream inputStr = getClass().getClassLoader().getResourceAsStream("levels/" + level);
        if (inputStr == null)
            throw new RuntimeException("Level folder is not found!");

        Scanner input = new Scanner(inputStr);
        while (input.hasNext()) {
            String[] text = input.next().split(",");
            filler(text[1], text[2], Integer.parseInt(text[0]));
        }
        input.close();
    }

    public void initialize() {
        for (int row = 0; row < GRID; row++) {
            for (int column = 0; column < GRID; column++) {
                EmptyTile empty = new EmptyTile("Free");
                empty.getImage().relocate(size * column, size * row);
                this.getChildren().add(empty.getImage());
            }
        }
    }

    public void filler(String type, String property, int id) {
        int row = id / GRID;
        int column = id % GRID - 1;

        if (id % GRID == 0) {
            row = id / GRID - 1;
            column = GRID - 1;
        }

        Tile tile;
        switch (type) {
            case "Starter":
                if (property.equals("Horizontal")) property = "HorizontalR";
                else if (property.equals("Vertical")) property = "VerticalB";
                tile = new StarterTile(property);
                break;
            case "Pipe":
                tile = new PipeTile(property);
                break;
            case "PipeStatic":
                tile = new PipeStaticTile(property);
                break;
            case "End":
                if (property.equals("Horizontal")) property = "HorizontalL";
                else if (property.equals("Vertical")) property = "VerticalB";
                tile = new EndTile(property);
                break;
            case "Empty":
                tile = new EmptyTile(property);
                break;
            default:
                return;
        }

        tile.getImage().relocate(column * size, row * size);
        this.getChildren().add(tile.getImage());
        this.tiles[row][column] = tile;
    }

    // Unified move method: isVertical=true for up/down, positive=true for down/right
    public void move(Tile tile, MouseEvent e, int row, int column,
                     double firstCoord, double layCoord, Label label,
                     boolean isVertical, boolean positive) {

        EmptyTile empty = new EmptyTile("Free");

        int targetRow = row + (isVertical ? (positive ? 1 : -1) : 0);
        int targetCol = column + (!isVertical ? (positive ? 1 : -1) : 0);

        // Boundary check
        if (targetRow < 0 || targetRow >= GRID || targetCol < 0 || targetCol >= GRID) {
            isFalse = true;
            return;
        }

        if (!tile.isMove()) {
            isFalse = true;
            return;
        }

        if (!this.tiles[targetRow][targetCol].getProperty().equals("Free")) {
            isFalse = true;
            return;
        }

        // Update motion
        tile.getMotion().setMoving(true);
        setDirectionMoving(tile, isVertical, positive, true);
        this.getTiles()[row][column].getImage().toFront();

        // Drag the tile
        double delta = isVertical ? (e.getY() - firstCoord) : (e.getX() - firstCoord);
        double newCoord = layCoord + delta;

        // Clamp to one tile distance
        if (positive) {
            newCoord = Math.min(newCoord, layCoord + size);
        } else {
            newCoord = Math.max(newCoord, layCoord - size);
        }

        if (isVertical) {
            tile.getImage().setLayoutY(newCoord);
        } else {
            tile.getImage().setLayoutX(newCoord);
        }

        tile.getImage().setOnMouseReleased(release -> {
            if (this.tiles[targetRow][targetCol].getProperty().equals("Free")) {
                double releaseCoord = isVertical ? release.getSceneY() : release.getSceneX();
                boolean draggedEnough = positive ? (releaseCoord > layCoord + DRAG_THRESHOLD)
                                                 : (releaseCoord < layCoord - DRAG_THRESHOLD);

                if (draggedEnough) {
                    double finalCoord = positive ? layCoord + size : layCoord - size;
                    if (isVertical) {
                        tile.getImage().setLayoutY(finalCoord);
                    } else {
                        tile.getImage().setLayoutX(finalCoord);
                    }
                    this.tiles[targetRow][targetCol] = tile;
                    this.tiles[row][column] = empty;
                    slideSound.play();
                    move++;
                    label.setText("Moves : " + move);
                } else {
                    if (isVertical) {
                        tile.getImage().setLayoutY(layCoord);
                    } else {
                        tile.getImage().setLayoutX(layCoord);
                    }
                }
            }
            tile.getMotion().setMoving(false);
            setDirectionMoving(tile, isVertical, positive, false);
        });
    }

    private void setDirectionMoving(Tile tile, boolean isVertical, boolean positive, boolean value) {
        if (isVertical) {
            if (positive) tile.getMotion().setMovingDown(value);
            else tile.getMotion().setMovingUp(value);
        } else {
            if (positive) tile.getMotion().setMovingRight(value);
            else tile.getMotion().setMovingLeft(value);
        }
    }

    public boolean isCompleted() {
        Tile tile = getStarter();

        Path path = new Path();
        path.getElements().add(new MoveTo(
                tile.getImage().getLayoutX() + size / 2.0,
                tile.getImage().getLayoutY() + size / 2.0));

        int row = (int) tile.getImage().getLayoutY() / size;
        int column = (int) tile.getImage().getLayoutX() / size;

        // Directions: 0=down, 1=left, 2=up, 3=right
        int direction;
        switch (tile.getProperty()) {
            case "VerticalB":    direction = 0; break;
            case "VerticalT":    direction = 2; break;
            case "HorizontalR":  direction = 3; break;
            default:             direction = 1; break;
        }

        while (true) {
            // End tile reached
            if (tile instanceof EndTile) {
                if (tile.getProperty().equals("HorizontalR") || tile.getProperty().equals("HorizontalL"))
                    path.getElements().add(new HLineTo(tile.getImage().getLayoutX() + size / 2.0));
                else
                    path.getElements().add(new VLineTo(tile.getImage().getLayoutY() + size / 2.0));
                break;
            }

            // Try to find the next segment
            int nextDir = -1;
            boolean isStarter = tile instanceof StarterTile;

            // Straight segments: direction matches the free side
            if (tile.isRightFree() && direction == 3) {
                double x = tile.getImage().getLayoutX() + (isStarter ? size / 2.0 : size);
                path.getElements().add(new HLineTo(x));
                nextDir = 3;
                if (++column >= GRID) return false;
                tile = this.tiles[row][column];
                if (!tile.isLeftFree()) return false;

            } else if (tile.isLeftFree() && direction == 1) {
                path.getElements().add(new HLineTo(tile.getImage().getLayoutX()));
                nextDir = 1;
                if (--column < 0) return false;
                tile = this.tiles[row][column];
                if (!tile.isRightFree()) return false;

            } else if (tile.isBottomFree() && direction == 0) {
                double y = tile.getImage().getLayoutY() + (isStarter ? size / 2.0 : size);
                path.getElements().add(new VLineTo(y));
                nextDir = 0;
                if (++row >= GRID) return false;
                tile = this.tiles[row][column];
                if (!tile.isTopFree()) return false;

            } else if (tile.isTopFree() && direction == 2) {
                path.getElements().add(new VLineTo(tile.getImage().getLayoutY()));
                nextDir = 2;
                if (--row < 0) return false;
                tile = this.tiles[row][column];
                if (!tile.isBottomFree()) return false;

            // Curved segments: direction differs from free side (pipe bends)
            } else {
                int[] curveResult = handleCurve(tile, direction, row, column, path);
                if (curveResult == null) return false;
                nextDir = curveResult[0];
                row = curveResult[1];
                column = curveResult[2];
                tile = this.tiles[row][column];
            }

            direction = nextDir;
        }

        slideSound.stop();
        completeSound.play();
        this.correctPath = path;
        return true;
    }

    private int[] handleCurve(Tile tile, int direction, int row, int column, Path path) {
        double tileX = tile.getImage().getLayoutX();
        double tileY = tile.getImage().getLayoutY();

        // Determine arc parameters based on which side is free and incoming direction
        double arcX, arcY;
        boolean sweep;
        int newDir;
        int newRow = row, newCol = column;

        if (tile.isRightFree() && direction == 0) {
            arcX = tileX + size; arcY = tileY + size / 2.0; sweep = false; newDir = 3; newCol++;
        } else if (tile.isRightFree() && direction == 2) {
            arcX = tileX + size; arcY = tileY + size / 2.0; sweep = true; newDir = 3; newCol++;
        } else if (tile.isLeftFree() && direction == 0) {
            arcX = tileX; arcY = tileY + size / 2.0; sweep = true; newDir = 1; newCol--;
        } else if (tile.isLeftFree() && direction == 2) {
            arcX = tileX; arcY = tileY + size / 2.0; sweep = false; newDir = 1; newCol--;
        } else if (tile.isBottomFree() && direction == 3) {
            arcX = tileX + size / 2.0; arcY = tileY + size; sweep = true; newDir = 0; newRow++;
        } else if (tile.isBottomFree() && direction == 1) {
            arcX = tileX + size / 2.0; arcY = tileY + size; sweep = false; newDir = 0; newRow++;
        } else if (tile.isTopFree() && direction == 3) {
            arcX = tileX + size / 2.0; arcY = tileY; sweep = false; newDir = 2; newRow--;
        } else if (tile.isTopFree() && direction == 1) {
            arcX = tileX + size / 2.0; arcY = tileY; sweep = true; newDir = 2; newRow--;
        } else {
            return null;
        }

        // Boundary check
        if (newRow < 0 || newRow >= GRID || newCol < 0 || newCol >= GRID) return null;

        // Validate connecting tile
        Tile next = this.tiles[newRow][newCol];
        if (newDir == 3 && !next.isLeftFree()) return null;
        if (newDir == 1 && !next.isRightFree()) return null;
        if (newDir == 0 && !next.isTopFree()) return null;
        if (newDir == 2 && !next.isBottomFree()) return null;

        ArcTo arc = new ArcTo();
        arc.setRadiusX(size / 2.0);
        arc.setRadiusY(size / 2.0);
        arc.setSweepFlag(sweep);
        arc.setX(arcX);
        arc.setY(arcY);
        path.getElements().add(arc);

        return new int[]{newDir, newRow, newCol};
    }

    public Tile getStarter() {
        for (int i = 0; i < GRID; i++)
            for (int j = 0; j < GRID; j++)
                if (this.tiles[i][j] instanceof StarterTile)
                    return this.tiles[i][j];
        return null;
    }

    public Tile[][] getTiles() { return tiles; }
    public int getSize() { return size; }
    public void setMove(int move) { this.move = move; }
    public AudioClip getWrongSlideSound() { return wrongSlideSound; }
    public Path getCorrectPath() { return correctPath; }
    public boolean isFalse() { return isFalse; }
    public void setFalse(boolean aFalse) { isFalse = aFalse; }
}
