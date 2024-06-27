package com.ray3k.badforce2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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

    public static Sound sfx_alien_death;
    public static Sound sfx_alien_hit;
    public static Sound sfx_beam;
    public static Sound sfx_complete;
    public static Sound sfx_crawler_death;
    public static Sound sfx_dodge;
    public static Sound sfx_door;
    public static Sound sfx_double_jump;
    public static Sound sfx_fart;
    public static Sound sfx_flier_death;
    public static Sound sfx_grabbed_ledge;
    public static Sound sfx_head_hit;
    public static Sound sfx_jump;
    public static Sound sfx_land;
    public static Sound sfx_pit;
    public static Sound sfx_player_death;
    public static Sound sfx_player_hurt;
    public static Sound sfx_wall_push;
    public static Sound sfx_ricochet;
    public static Sound sfx_roll;
    public static Sound sfx_wall_slide;
    public static Sound sfx_spike;
    public static Sound sfx_spike_death;
    public static Sound sfx_wall_jump;
    public static Sound sfx_water;

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
        bgm.setVolume(.6f);

        sfx_alien_death = Gdx.audio.newSound(Gdx.files.internal("sfx/alien-death.mp3"));
        sfx_alien_hit = Gdx.audio.newSound(Gdx.files.internal("sfx/alien-hit.mp3"));
        sfx_beam = Gdx.audio.newSound(Gdx.files.internal("sfx/beam.mp3"));
        sfx_complete = Gdx.audio.newSound(Gdx.files.internal("sfx/complete.mp3"));
        sfx_crawler_death = Gdx.audio.newSound(Gdx.files.internal("sfx/crawler-death.mp3"));
        sfx_dodge = Gdx.audio.newSound(Gdx.files.internal("sfx/dodge.mp3"));
        sfx_door = Gdx.audio.newSound(Gdx.files.internal("sfx/door.mp3"));
        sfx_double_jump = Gdx.audio.newSound(Gdx.files.internal("sfx/double-jump.mp3"));
        sfx_fart = Gdx.audio.newSound(Gdx.files.internal("sfx/fart.mp3"));
        sfx_flier_death = Gdx.audio.newSound(Gdx.files.internal("sfx/flier-death.mp3"));
        sfx_grabbed_ledge = Gdx.audio.newSound(Gdx.files.internal("sfx/grabbed-ledge.mp3"));
        sfx_head_hit = Gdx.audio.newSound(Gdx.files.internal("sfx/head-hit.mp3"));
        sfx_jump = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.mp3"));
        sfx_land = Gdx.audio.newSound(Gdx.files.internal("sfx/land.mp3"));
        sfx_pit = Gdx.audio.newSound(Gdx.files.internal("sfx/pit.mp3"));
        sfx_player_death = Gdx.audio.newSound(Gdx.files.internal("sfx/player-death.mp3"));
        sfx_player_hurt = Gdx.audio.newSound(Gdx.files.internal("sfx/player-hurt.mp3"));
        sfx_wall_push = Gdx.audio.newSound(Gdx.files.internal("sfx/wall-push.mp3"));
        sfx_ricochet = Gdx.audio.newSound(Gdx.files.internal("sfx/ricochet.mp3"));
        sfx_roll = Gdx.audio.newSound(Gdx.files.internal("sfx/roll.mp3"));
        sfx_wall_slide = Gdx.audio.newSound(Gdx.files.internal("sfx/wall-slide.mp3"));
        sfx_spike = Gdx.audio.newSound(Gdx.files.internal("sfx/spike.mp3"));
        sfx_spike_death = Gdx.audio.newSound(Gdx.files.internal("sfx/spike-death.mp3"));
        sfx_wall_jump = Gdx.audio.newSound(Gdx.files.internal("sfx/wall-jump.mp3"));
        sfx_water = Gdx.audio.newSound(Gdx.files.internal("sfx/water.mp3"));
        setScreen(new SplashScreen());
    }

}
