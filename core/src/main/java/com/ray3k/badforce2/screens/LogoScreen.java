package com.ray3k.badforce2.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.ray3k.badforce2.SpineImage;

import static com.ray3k.badforce2.Core.*;

public class LogoScreen extends ScreenAdapter {
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

        onClick(root, this::nextScreen);

        var skeletonData = skeletonJson.readSkeletonData(Gdx.files.internal("spine/libgdx.json"));
        var animationData = new AnimationStateData(skeletonData);

        var image = new SpineImage(skeletonRenderer, skeletonData, animationData);
        image.setCrop(0, 0, 1024, 576);
        root.add(image).grow();

        var skeleton = image.getSkeleton();
        var animation = image.getAnimationState();

        animation.setAnimation(0, "animation", false);
        animation.addListener(new AnimationStateAdapter() {
            @Override
            public void event(TrackEntry entry, Event event) {
                var path = event.getData().getAudioPath();
                if (path != null) {
                    var sound = Gdx.audio.newSound(Gdx.files.internal(path));
                    sound.play();
                }
            }

            @Override
            public void complete(TrackEntry entry) {
                stage.addAction(Actions.delay(2f, Actions.run(() -> nextScreen())));
            }
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

    private void nextScreen() {
        core.setScreen(new SplashScreen());
    }
}
