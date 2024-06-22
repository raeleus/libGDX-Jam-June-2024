package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour.BoundsData;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

public class PlayerBehaviour extends SlopeCharacterBehaviour {
    public PlayerBehaviour(GameObject gameObject) {
        super(.1f, .45f, .5f, 1.45f, gameObject);
    }

    @Override
    public void handleControls() {
        if (Gdx.input.isKeyPressed(Keys.A)) moveLeft();
        else if (Gdx.input.isKeyPressed(Keys.D)) moveRight();

        if (Gdx.input.isKeyPressed(Keys.W)) moveJump();
    }

    @Override
    public void eventCeilingClingMoving(float delta, float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventWalking(float delta, float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventWalkStopping(float delta, float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventWalkStop(float delta) {

    }

    @Override
    public void eventWalkReversing(float delta, float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventWalkingSlide(float delta, float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventWalkPushingWall(float delta, float wallAngle) {

    }

    @Override
    public void eventCliffEdge(float delta, boolean right) {

    }

    @Override
    public void eventTouchGroundFixture(Fixture fixture, float contactNormalAngle, BoundsBehaviour bounds,
                                        BoundsData boundsData) {

    }

    @Override
    public void eventSlideSlope(float delta, float lateralSpeed, float groundAngle, float slidingAngle) {

    }

    @Override
    public void eventSlidePushingWall(float delta, float wallAngle) {

    }

    @Override
    public void eventJump(float delta) {

    }

    @Override
    public void eventJumpReleased(float delta) {

    }

    @Override
    public void eventJumpApex(float delta) {

    }

    @Override
    public void eventJumpFromSlide(float delta) {

    }

    @Override
    public void eventJumpMidair(float delta) {

    }

    @Override
    public void eventHitHead(float delta, float ceilingAngle) {

    }

    @Override
    public void eventFalling(float delta) {

    }

    @Override
    public void eventFallingTouchingWall(float delta, float wallAngle) {

    }

    @Override
    public void eventLand(float delta, float groundAngle) {

    }

    @Override
    public void eventWallCling(float delta, float wallAngle) {

    }

    @Override
    public void eventReleaseWallCling(float delta) {

    }

    @Override
    public void eventWallSliding(float delta, float wallAngle) {

    }

    @Override
    public void eventWallClimbing(float delta, float wallAngle) {

    }

    @Override
    public void eventWallClimbReachedTop(float delta) {

    }

    @Override
    public void eventWallJump(float delta, float wallAngle) {

    }

    @Override
    public void eventGrabLedge(float delta, float wallAngle) {

    }

    @Override
    public void eventReleaseGrabLedge(float delta) {

    }

    @Override
    public void eventLedgeJump(float delta, float wallAngle) {

    }

    @Override
    public void eventCeilingClingPushingWall(float delta, float wallContactAngle) {

    }

    @Override
    public void eventCeilingClingStop(float delta) {

    }

    @Override
    public void eventCeilingClingStopping(float previousSwingDelta, float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventCeilingClingMovingReversing(float delta, float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventCeilingClingReleased(float delta) {

    }

    @Override
    public void eventMagnetPushingWall(float delta, float wallContactAngle) {

    }

    @Override
    public void eventMagnetStop(float delta) {

    }

    @Override
    public void eventMagnetStopping(float previousSwingDelta, float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventMagnetMoving(float delta, float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventMagnetMovingReversing(float delta, float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventMagnetReleased(float delta) {

    }

    @Override
    public void eventPassedThroughPlatform(Fixture fixture, float fixtureAngle, BoundsBehaviour bounds,
                                           BoundsData boundsData) {

    }

    @Override
    public void eventSwing(float delta, float swingAngle, float lateralSpeed) {

    }

    @Override
    public void eventSwinging(float delta, float swingAngle, float lateralSpeed) {

    }

    @Override
    public void eventSwingReleased(float delta, float swingAngle, float lateralSpeed, boolean automaticRelease) {

    }

    @Override
    public void eventSwingCrashWall(float delta, float swingAngle, float lateralSpeed) {

    }

    @Override
    public void eventSwingCrashGround(float delta, float swingAngle, float lateralSpeed) {

    }
}
