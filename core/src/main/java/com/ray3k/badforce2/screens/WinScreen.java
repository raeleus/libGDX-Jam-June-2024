package com.ray3k.badforce2.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.github.raeleus.gamejoltapi.GameJoltApi;
import com.github.raeleus.gamejoltapi.GameJoltScores;
import com.github.raeleus.gamejoltapi.GameJoltScores.ScoresAddListener;
import com.github.raeleus.gamejoltapi.GameJoltScores.ScoresAddValue;
import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.Utils;

import java.text.DecimalFormat;
import java.util.logging.Level;

import static com.ray3k.badforce2.Core.*;
import static com.ray3k.badforce2.screens.GameScreen.*;

public class WinScreen extends ScreenAdapter {
    private Stage stage;
    private FillViewport viewport;
    private long totalTime;

    @Override
    public void show() {
        var df = new DecimalFormat("#.00");
        viewport = new FillViewport(WINDOW_WIDTH, WINDOW_HEIGHT);
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        var root = new Table();
        root.setFillParent(true);
        root.setTouchable(Touchable.enabled);
        stage.addActor(root);

        root.defaults().space(30);
        var label = new Label("Congrats! You survived to the end.\nYou may travel back in time to improve your times\nor submit your score." , skin);
        root.add(label);

        root.row();
        var verticalGroup = new VerticalGroup();
        verticalGroup.wrap();
        root.add(verticalGroup).grow();

        totalTime = 0;
        for (int i = 0; i < 8; i++) {
            var table = new Table();
            verticalGroup.addActor(table);

            int level = i + 1;
            var seconds = df.format(times[i] / 1000f);
            totalTime += times[i];
            label = new Label("Level " + level + ":   " + seconds + " seconds", skin);
            table.add(label);

            var button = new ImageButton(skin);
            table.add(button);
            Utils.onChange(button, () -> nextScreen(level));
        }

        var totalString = df.format(totalTime / 1000);
        root.row();
        label = new Label("Or type your name: " + totalString + " seconds", skin);
        root.add(label);

        root.row();
        var textField = new TextField("", skin);
        root.add(textField);
        stage.setKeyboardFocus(textField);

        root.row();
        var button = new ImageButton(skin, "submit");
        root.add(button);
        Utils.onChange(button, () -> {
            if (textField.getText().isBlank()) return;
            var gj = new GameJoltApi();
            var request = GameJoltScores.ScoresAddRequest.builder()
                .gameID("907422")
                .tableID(918899)
                .score(totalString + " seconds")
                .sort(totalTime)
                .guest(textField.getText())
                .build();
            gj.sendRequest(request, "8cde9c99919c19143c9cb5664065a382", new ScoresAddListener() {
                @Override
                public void scoresAdd(ScoresAddValue scoresAddValue) {
                    System.out.println("scoresAddValue.message = " + scoresAddValue.message);
                    System.out.println("scoresAddValue.success = " + scoresAddValue.success);
                }

                @Override
                public void failed(Throwable t) {
                    System.out.println("failed " + t.getLocalizedMessage());
                }

                @Override
                public void cancelled() {
                    System.out.println("cancelled");
                }
            });
            if (foundCake && foundCat && foundRainbow && foundScythe) {
                request = GameJoltScores.ScoresAddRequest.builder()
                    .gameID("907422")
                    .tableID(918903)
                    .score(totalString + " seconds")
                    .sort(totalTime)
                    .guest(textField.getText())
                    .build();
                gj.sendRequest(request, "8cde9c99919c19143c9cb5664065a382", new ScoresAddListener() {
                    @Override
                    public void scoresAdd(ScoresAddValue scoresAddValue) {
                        System.out.println("scoresAddValue.message = " + scoresAddValue.message);
                        System.out.println("scoresAddValue.success = " + scoresAddValue.success);
                    }

                    @Override
                    public void failed(Throwable t) {
                        System.out.println("failed " + t.getLocalizedMessage());
                    }

                    @Override
                    public void cancelled() {
                        System.out.println("cancelled");
                    }
                });
            }
            core.setScreen(new MenuScreen());
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private void nextScreen(int level) {
        core.setScreen(new GameScreen("level" + level + ".json"));
    }
}
