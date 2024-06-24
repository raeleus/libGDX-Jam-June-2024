package com.ray3k.badforce2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.esotericsoftware.spine.Skeleton.Physics;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.badforce2.screens.SplashScreen;
import dev.lyze.gdxUnBox2d.UnBox;
import space.earlygrey.shapedrawer.ShapeDrawer;

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
    public static final float PPM = 100f;
    public static ShapeDrawer shapeDrawer;
    public static Music bgm;

    @Override
    public void create() {
        core = this;
        textureAtlas = new TextureAtlas(Gdx.files.internal("textures/textures.atlas"));
        skin = new Skin(Gdx.files.internal("ui/skin.json"), textureAtlas);
        batch = new TwoColorPolygonBatch(32767);
        skeletonRenderer = new SkeletonRenderer();
        skeletonJson = new SkeletonJson(textureAtlas);
        shapeDrawer = new ShapeDrawer(batch, textureAtlas.findRegion("white"));
        bgm = Gdx.audio.newMusic(Gdx.files.internal("bgm/game.ogg"));
        bgm.setLooping(true);

        setScreen(new SplashScreen());
    }

}
