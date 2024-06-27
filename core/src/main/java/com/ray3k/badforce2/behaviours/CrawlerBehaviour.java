package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.GameObject;

import static com.ray3k.badforce2.Core.*;
import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.behaviours.PlayerBehaviour.player;
import static com.ray3k.badforce2.screens.GameScreen.unBox;

public class CrawlerBehaviour extends AlienBehaviour {
    public boolean passive;
    private final static Vector2 temp1 = new Vector2();
    private final static Vector2 temp2 = new Vector2();
    private static float tempFraction;
    private Behaviour targetedBehaviour;
    public CrawlerBehaviour(float footOffsetX, float footOffsetY, float footRadius, float torsoHeight,
                            GameObject gameObject) {
        super(footOffsetX, footOffsetY, footRadius, torsoHeight, gameObject);
        lateralMaxSpeed = 20f;
        lateralDeceleration = 20f;
        health = 10f;
        deathSound = sfx_crawler_death;
    }

    @Override
    public void awake() {
        super.awake();
        if (passive) {
            gravity = 0;
            deltaX = 0;
            deltaY = 0;
            getBody(this).setLinearVelocity(0, 0);
            getAnimationState(this).setEmptyAnimation(0, 0);
        }
    }

    @Override
    public void handleControls() {
        if (health <= 0) return;
        if (getBody(player) == null) return;

        if (passive) {
            float distanceToPlayer = pointDistance(this, player);

            var playerBody = getBody(player);
            var bounds = unBox.findBehaviours(BoundsBehaviour.class);
            temp1.set(getBody(this).getWorldCenter());
            temp2.set(playerBody.getWorldCenter());
            tempFraction = Float.MAX_VALUE;
            unBox.getWorld().rayCast((fixture, point1, normal, fraction) -> {
                if (playerBody.getFixtureList().contains(fixture, true)) {
                    if (fraction < tempFraction) {
                        tempFraction = fraction;
                        targetedBehaviour = player;
                        return fraction;
                    }
                    return 1;
                }

                for (var bound : bounds) {
                    var body = getBody(bound);
                    if (body.getFixtureList().contains(fixture, true)) {
                        if (fraction < tempFraction) {
                            tempFraction = fraction;
                            targetedBehaviour = bound;
                            return fraction;
                        }
                        return 1;
                    }
                }
                return -1;
            }, temp1, temp2);

            if (distanceToPlayer < 7f && targetedBehaviour == player) {
                passive = false;
                gravity = -3f;
                setAnimation(0, "walk", true, this);
                applyAirForce(25f, playerBody.getPosition().x > getPosition(this).x ? 45f : 135f);
            }
        } else {
            super.handleControls();
        }
    }
}
