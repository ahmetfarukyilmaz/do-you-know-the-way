package com.game.launch;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private double firstX, lastX, firstY, lastY;
    private int levelCounter = 1;
    private int row, column;
    private double initialLayoutY, initialLayoutX;
    private String level = "level1.txt";
    private String userName;
    private Stage stage;
    private Scene scene;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        createMainMenu();
    }

    private Background createBackground(double width, double height) {
        BackgroundImage back = new BackgroundImage(
                new Image("resources/images/background.jpg", width, height, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        return new Background(back);
    }

    public void createMainMenu() {
        AudioClip mainMenuSound = new AudioClip("file:src/resources/sounds/sound.wav");
        mainMenuSound.setCycleCount(999);
        mainMenuSound.play();

        stage = new Stage();
        stage.setTitle("Do You Know The Way?");
        stage.setResizable(false);
        stage.getIcons().add(new Image("resources/images/ugandan_knuckles.png"));

        Pane pane = new Pane();
        pane.setBackground(createBackground(480, 600));

        ImageView gameName = new ImageView("resources/images/Game Name.gif");
        Button newGame = new Button("", new ImageView("resources/images/playButton.png"));
        Button exit = new Button("", new ImageView("resources/images/quitButton.png"));

        newGame.setMaxSize(300, 80);
        newGame.setMinSize(300, 80);
        exit.setMaxSize(300, 80);
        exit.setMinSize(300, 80);

        pane.getChildren().addAll(newGame, exit, gameName);
        gameName.relocate(0, 100);
        newGame.relocate(225, 250);
        exit.relocate(225, 400);

        Scene scene = new Scene(pane, 750, 600);
        stage.setScene(scene);
        stage.show();

        newGame.setOnAction(e -> { mainMenuSound.stop(); createNickNameBoard(); });
        exit.setOnAction(e -> System.exit(1));
    }

    public void createNickNameBoard() {
        stage.setResizable(false);

        Pane paneText = new Pane();
        paneText.setBackground(createBackground(295, 250));

        TextField textField = new TextField();
        textField.setMaxWidth(300);
        textField.setMinWidth(200);

        ImageView nickNameText = new ImageView("resources/images/Nick Name.png");
        Button startButton = new Button("Start");

        paneText.getChildren().addAll(nickNameText, startButton, textField);
        nickNameText.relocate(0, 30);
        textField.relocate(50, 90);
        startButton.relocate(130, 150);

        Scene sceneText = new Scene(paneText, 295, 250);
        stage.setScene(sceneText);
        stage.show();

        startButton.setOnAction(e -> {
            userName = textField.getText();
            stage.setScene(scene);
            createGameBoard();
        });
    }

    public void createGameBoard() {
        GameBoard gameboard = new GameBoard(level);

        Label labelNick = createLabel(userName, 170, 550);
        Label labelMove = createLabel("Moves : 0", 345, 500);
        Label labelLevel = createLabel("Level  " + levelCounter, 20, 500);

        ImageView ball = new ImageView("resources/images/ball.png");

        gameboard.setBackground(createBackground(480, 600));

        ball.relocate(
                (gameboard.getStarter().getImage().getLayoutX() + gameboard.getSize() / 2)
                        - (ball.getImage().getWidth() - gameboard.getSize() / 5.2),
                gameboard.getStarter().getImage().getLayoutY() + gameboard.getSize() / 2
                        - (ball.getImage().getHeight() - gameboard.getSize() / 4.5));

        gameboard.getChildren().addAll(labelMove, labelLevel, ball, labelNick);

        gameboard.setOnMousePressed(e -> {
            firstX = e.getX();
            firstY = e.getY();
            row = (int) firstY / gameboard.getSize();
            column = (int) firstX / gameboard.getSize();

            if (row >= 0 && row < 4 && column >= 0 && column < 4) {
                initialLayoutY = gameboard.getTiles()[row][column].getImage().getLayoutY();
                initialLayoutX = gameboard.getTiles()[row][column].getImage().getLayoutX();
            }
        });

        gameboard.setOnMouseDragged(e -> {
            lastX = e.getX();
            lastY = e.getY();

            if (row >= 0 && row < 4 && column >= 0 && column < 4) {
                var tile = gameboard.getTiles()[row][column];
                var motion = tile.getMotion();

                if (lastY - firstY > 30 && motion.canMoveDown()) {
                    gameboard.move(tile, e, row, column, firstY, initialLayoutY, labelMove, true, true);
                } else if (lastX - firstX > 30 && motion.canMoveRight()) {
                    gameboard.move(tile, e, row, column, firstX, initialLayoutX, labelMove, false, true);
                } else if (firstX - lastX > 30 && motion.canMoveLeft()) {
                    gameboard.move(tile, e, row, column, firstX, initialLayoutX, labelMove, false, false);
                } else if (firstY - lastY > 30 && motion.canMoveUp()) {
                    gameboard.move(tile, e, row, column, firstY, initialLayoutY, labelMove, true, false);
                }
            }
        });

        gameboard.setOnMouseReleased(finish -> {
            if (gameboard.isFalse()) {
                gameboard.getWrongSlideSound().play();
                gameboard.setFalse(false);
            }

            if (gameboard.isCompleted()) {
                ball.relocate(0, 0);
                ball.toFront();

                PathTransition pt = new PathTransition();
                pt.setPath(gameboard.getCorrectPath());
                pt.setNode(ball);
                pt.setDuration(Duration.seconds(3));
                pt.play();

                gameboard.setOnMouseDragged(null);
                gameboard.setOnMouseReleased(null);

                if (levelCounter == 15) {
                    Button exitButton = new Button("Exit");
                    gameboard.getChildren().add(exitButton);
                    exitButton.relocate(400, 550);
                    exitButton.setOnAction(exit -> System.exit(1));
                } else {
                    Button nextBut = new Button("Next Stage");
                    nextBut.relocate(400, 550);
                    gameboard.getChildren().add(nextBut);
                    nextBut.setOnAction(goNext -> {
                        gameboard.setMove(0);
                        stage.setScene(scene);
                        level = "level" + (++levelCounter) + ".txt";
                        createGameBoard();
                    });
                }
            }
        });

        scene = new Scene(gameboard, 480, 600);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private Label createLabel(String text, double x, double y) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        label.setTextFill(Color.rgb(35, 243, 255));
        label.relocate(x, y);
        return label;
    }
}
