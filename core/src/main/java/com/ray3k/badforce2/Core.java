package com.ray3k.badforce2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.ray3k.badforce2.screens.SplashScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Core extends Game {
    public static final float GAME_WIDTH = 1024;
    public static final float GAME_HEIGHT = 576;
    public static TextureAtlas textureAtlas;
    public static Skin skin;
    public static SkeletonRenderer skeletonRenderer;

    @Override
    public void create() {
        textureAtlas = new TextureAtlas(Gdx.files.internal("textures/textures.atlas"));
        skin = new Skin(Gdx.files.internal("ui/skin.json"), textureAtlas);
        setScreen(new SplashScreen());
    }

    public static void onChange(Actor actor, Runnable runnable) {
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                runnable.run();
            }
        });
    }

    public static void onClick(Actor actor, Runnable runnable) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                runnable.run();
            }
        });
    }
}
