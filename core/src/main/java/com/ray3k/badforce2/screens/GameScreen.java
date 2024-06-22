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

    @Override
    public void show() {
        gameCamera = new OrthographicCamera();
        gameViewport = new ExtendViewport(15, 10, gameCamera);
        uiViewport = new FillViewport(WINDOW_WIDTH, WINDOW_HEIGHT);
        stage = new Stage(uiViewport, batch);
        Gdx.input.setInputProcessor(stage);

        var root = new Table();
        root.setFillParent(true);
        root.setTouchable(Touchable.enabled);
        stage.addActor(root);

        debugLabel = new Label("", skin);
        root.add(debugLabel).expand().top().left();

        unBox = new UnBox(new World(new Vector2(), true));
        debugRenderer = new Box2DDebugRenderer();

        var player = new GameObject(unBox);
        var bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        new Box2dBehaviour(bodyDef, player);
        new PlayerBehaviour(player);

        var spine = new SpineBehaviour(player, "spine/player.json");
        spine.skeleton.setScale(1/PPM, 1/PPM);
        spine.skeleton.setSkin("assault");
        spine.animationState.setAnimation(0, "standing", true);

        var ground = new GameObject(unBox);
        bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(0, -5f);
        new Box2dBehaviour(bodyDef, ground);

        float[] points = new float[]{-5f, 1f, -5f, -1f, 5f, -1f, 5f, 1f, 4f, 3f, 3f, 1.5f, 2f, 1f};
        new BoundsBehaviour(points, ground);
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

        debugRenderer.render(unBox.getWorld(), gameCamera.combined);

        unBox.postRender();

        uiViewport.apply();
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
