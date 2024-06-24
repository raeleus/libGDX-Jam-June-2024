package com.ray3k.badforce2.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.esotericsoftware.spine.AnimationStateData;
import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.LevelReader;
import com.ray3k.badforce2.OgmoReader;
import com.ray3k.badforce2.behaviours.PlayerBehaviour;
import com.ray3k.badforce2.behaviours.SpineBehaviour;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour;
import dev.lyze.gdxUnBox2d.Box2dBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.UnBox;
import dev.lyze.gdxUnBox2d.behaviours.fixtures.CreateBoxFixtureBehaviour;
import dev.lyze.gdxUnBox2d.behaviours.fixtures.CreateCircleFixtureBehaviour;

import static com.ray3k.badforce2.Core.*;

public class GameScreen extends ScreenAdapter {
    public static ExtendViewport gameViewport;
    public static OrthographicCamera gameCamera;
    public static Stage stage;
    public static FillViewport uiViewport;
    public static UnBox unBox;
    private Box2DDebugRenderer debugRenderer;
    public static Label debugLabel;
    public static final float BACKGROUND_RENDER_ORDER = -50f;
    public static final float PARTICLES_RENDER_ORDER = 100f;
    public static final float FOREGROUND_RENDER_ORDER = 200f;
    public static final float DEBUG_RENDER_ORDER = 300f;
    public static String levelName;
    public static String nextLevelName;
    public static float levelWidth;
    public static float levelHeight;

    public GameScreen(String levelName) {
        GameScreen.levelName = levelName;
    }

    @Override
    public void show() {
        gameCamera = new OrthographicCamera();
//        gameCamera.zoom = 100f;
        gameViewport = new ExtendViewport(15, 10, gameCamera);
        uiViewport = new FillViewport(WINDOW_WIDTH, WINDOW_HEIGHT);
        stage = new Stage(uiViewport, batch);
        Gdx.input.setInputProcessor(stage);

        var root = new Table();
        root.setFillParent(true);
        root.setTouchable(Touchable.enabled);
        stage.addActor(root);

        debugLabel = new Label("", skin);
//        root.add(debugLabel).expand().top().left();

        unBox = new UnBox(new World(new Vector2(), true));
        debugRenderer = new Box2DDebugRenderer();

        var ogmoReader = new OgmoReader();
        ogmoReader.addListener(new LevelReader());
        ogmoReader.readFile(Gdx.files.internal("levels/" + levelName));
        bgm.play();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        uiViewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        unBox.preRender(Gdx.graphics.getDeltaTime());

        gameViewport.apply();
        batch.setProjectionMatrix(gameCamera.combined);

        batch.begin();
        unBox.render(batch);
        batch.end();

//        debugRenderer.render(unBox.getWorld(), gameCamera.combined);

        unBox.postRender();

        uiViewport.apply();
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public static float p2m(float pixels) {
        return pixels / PPM;
    }

    public static float m2p(float meters) {
        return meters * PPM;
    }
}
