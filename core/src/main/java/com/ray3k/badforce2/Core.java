package com.ray3k.badforce2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.badforce2.screens.SplashScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Core extends Game {
    public static final float WINDOW_WIDTH = 1024;
    public static final float WINDOW_HEIGHT = 576;
    public static Core core;
    public static TextureAtlas textureAtlas;
    public static Skin skin;
    public static TwoColorPolygonBatch batch;
    public static SkeletonRenderer skeletonRenderer;
    public static SkeletonJson skeletonJson;

    @Override
    public void create() {
        core = this;
        textureAtlas = new TextureAtlas(Gdx.files.internal("textures/textures.atlas"));
        skin = new Skin(Gdx.files.internal("ui/skin.json"), textureAtlas);
        batch = new TwoColorPolygonBatch(32767);
        skeletonRenderer = new SkeletonRenderer();
        skeletonJson = new SkeletonJson(textureAtlas);

        setScreen(new SplashScreen());
    }

}
