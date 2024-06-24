package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.screens.GameScreen;
import dev.lyze.gdxUnBox2d.Box2dBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Core.textureAtlas;
import static com.ray3k.badforce2.Utils.getPosition;
import static com.ray3k.badforce2.screens.GameScreen.*;

public class ParticleBehaviour extends BehaviourAdapter {
    private ParticleEffect particleEffect;
    public ParticleBehaviour(String path, float x, float y, GameObject gameObject) {
        super(gameObject);
        particleEffect = new ParticleEffect();
        particleEffect.load(Gdx.files.internal(path), textureAtlas, "game/");
        particleEffect.scaleEffect(p2m(1));
        particleEffect.setPosition(x, y);
        var bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.position.set(x, y);
        setRenderOrder(PARTICLES_RENDER_ORDER);
        var box2d = new Box2dBehaviour(bodyDef, getGameObject());
        box2d.setExecutionOrder(getExecutionOrder() - 1);
    }

    @Override
    public void awake() {
        particleEffect.start();
    }

    @Override
    public void update(float delta) {
        var pos = getPosition(this);
        particleEffect.update(delta);
        particleEffect.setPosition(pos.x, pos.y);
    }

    @Override
    public void render(Batch batch) {
        particleEffect.draw(batch);
    }
}
