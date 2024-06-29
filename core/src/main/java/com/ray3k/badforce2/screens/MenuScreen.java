package com.ray3k.badforce2.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.ray3k.badforce2.Utils;

import static com.ray3k.badforce2.Core.*;
import static com.ray3k.badforce2.screens.GameScreen.*;

public class MenuScreen extends ScreenAdapter {
    private Stage stage;
    private FillViewport viewport;

    @Override
    public void show() {
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
        var button = new ImageButton(skin);
        root.add(button);
        Utils.onChange(button, this::nextScreen);
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

    private void nextScreen() {
        core.setScreen(new GameScreen("level1.json"));
        foundCake = false;
        foundCat = false;
        foundRainbow = false;
        foundScythe = false;
    }
}
