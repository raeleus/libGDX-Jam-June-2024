package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.physics.box2d.Contact;
import com.ray3k.badforce2.Utils;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviourAdapter;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.behaviours.PlayerBehaviour.*;

public class AlienBehaviour extends SlopeCharacterBehaviourAdapter {
    public float health = 50;
    private boolean goingLeft = true;

    public AlienBehaviour(float footOffsetX, float footOffsetY, float footRadius, float torsoHeight,
                          GameObject gameObject) {
        super(footOffsetX, footOffsetY, footRadius, torsoHeight, gameObject);
        lateralMaxSpeed = 10f;
        lateralDeceleration = 10f;
    }

    @Override
    public void handleControls() {
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
    public void onCollisionEnter(Behaviour other, Contact contact) {
        super.onCollisionEnter(other, contact);
        if (other.getGameObject().hasBehaviour(AlienBehaviour.class)) goingLeft = !goingLeft;
    }
}
