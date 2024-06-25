package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.ray3k.badforce2.Utils;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.GameObject;

import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.behaviours.PlayerBehaviour.*;

public class FlierBehaviour extends AlienBehaviour {
    private float moveTimer;
    public FlierBehaviour(float footOffsetX, float footOffsetY, float footRadius, float torsoHeight,
                          GameObject gameObject) {
        super(footOffsetX, footOffsetY, footRadius, torsoHeight, gameObject);

        health = 20f;
        gravity = 0;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        moveTimer -= delta;
    }

    @Override
    public void handleControls() {
        if (health <= 0 || player == null || getBody(player) == null) return;

        var distance = pointDistance(this, player);
        var direction = pointDirection(this, player);
        if (distance < 12 && moveTimer <= 0) {
            moveTimer = .7f;
            applyAirForce(MathUtils.random(4f, 7f), direction);
        }
        if (getSpeed() > 11f) setSpeed(11f);
    }

    @Override
    public boolean onCollisionPreSolve(Behaviour other, Contact contact, Manifold oldManifold) {
        var bounds = other.getGameObject().getBehaviour(BoundsBehaviour.class);
        if (health > 0 && bounds != null && bounds.canPassThroughBottom) {
            contact.setEnabled(false);
            return true;
        }
        return super.onCollisionPreSolve(other, contact, oldManifold);
    }

    private void updateFacingDirection() {
//        getSkeleton(this).getRootBone().setScale((goingLeft ? -1f : 1f), 1f);
    }

    @Override
    public void kill() {
        super.kill();
        gravity = -3f;
    }
}
