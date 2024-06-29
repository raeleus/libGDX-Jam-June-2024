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
import com.ray3k.badforce2.Utils;

import java.text.DecimalFormat;
import java.util.logging.Level;

import static com.ray3k.badforce2.Core.*;
import static com.ray3k.badforce2.screens.GameScreen.*;

public class WinScreen extends ScreenAdapter {
    private Stage stage;
    private FillViewport viewport;

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
        var image = new Image(skin, "title");
        image.setScaling(Scaling.fit);
        root.add(image);

        root.row();
        var label = new Label("Congrats! You survived to the end.\nYou may travel back in time to improve your times\nor submit your score", skin);
        root.add(label);

        root.row();
        var verticalGroup = new VerticalGroup();
        verticalGroup.wrap();
        root.add(verticalGroup).grow();

        for (int i = 0; i < 9; i++) {
            var table = new Table();
            verticalGroup.addActor(table);

            int level = i + 1;
            label = new Label("Level " + level + ":   " + df.format(times[i] / 1000f) + " seconds", skin);
            table.add(label);

            var button = new ImageButton(skin);
            table.add(button);
            Utils.onChange(button, () -> nextScreen(level));
        }

        root.row();
        label = new Label("Or type your name: ", skin);
        root.add(label);
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
