package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.Utils;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviourAdapter;
import com.ray3k.badforce2.behaviours.slope.SlopeValues;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.Box2dBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Core.*;
import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.behaviours.PlayerBehaviour.*;

public class AlienBehaviour extends SlopeCharacterBehaviourAdapter {
    public float health = 50;
    private boolean goingLeft = true;
    public Sound deathSound;

    public AlienBehaviour(float footOffsetX, float footOffsetY, float footRadius, float torsoHeight,
                          GameObject gameObject) {
        super(footOffsetX, footOffsetY, footRadius, torsoHeight, gameObject);
        lateralMaxSpeed = 10f;
        lateralDeceleration = 10f;
        deathSound = sfx_alien_death;
    }

    @Override
    public void handleControls() {
        if (health <= 0) return;

        if (goingLeft) moveLeft();
        else moveRight();

        if (getBody(player) == null) return;
        float distanceToPlayer = pointDistance(this, player);
        if (distanceToPlayer < 7) goingLeft = getBody(player).getPosition().x < getBody(this).getPosition().x;
        updateFacingDirection();
    }

    private void updateFacingDirection() {
        getSkeleton(this).getRootBone().setScale((goingLeft ? -1f : 1f), 1f);
    }

    @Override
    public void eventWalkPushingWall(float delta, float wallAngle) {
        goingLeft = !goingLeft;
    }

    @Override
    public boolean onCollisionPreSolve(Behaviour other, Contact contact, Manifold oldManifold) {
        var otherAlien = other.getGameObject().getBehaviour(AlienBehaviour.class);
        if (otherAlien != null && otherAlien.health > 0) {
            contact.setEnabled(false);
            return true;
        }
        return super.onCollisionPreSolve(other, contact, oldManifold);
    }

    @Override
    public void onCollisionEnter(Behaviour other, Contact contact) {
        super.onCollisionEnter(other, contact);

        var otherAlien = other.getGameObject().getBehaviour(AlienBehaviour.class);
        if (otherAlien != null && otherAlien.health > 0) goingLeft = !goingLeft;
    }

    public void kill() {
        getAnimationState(this).setAnimation(0, "kill", false);
        var body = getBody(this);
        body.setLinearVelocity(0, 0);
        for (var fixture : body.getFixtureList()) {
            fixture.getFilterData().categoryBits = SlopeValues.CATEGORY_NO_CONTACT;
        }
        deltaX = 0;
        deltaY = 0;
        lateralSpeed = 0;
        stickToGround = false;
        deathSound.play();
    }
}
