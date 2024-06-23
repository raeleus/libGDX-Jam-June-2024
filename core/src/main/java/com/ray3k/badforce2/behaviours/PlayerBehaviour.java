package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour.BoundsData;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.screens.GameScreen.*;

public class PlayerBehaviour extends SlopeCharacterBehaviour {
    private float queueRoll;

    public PlayerBehaviour(GameObject gameObject) {
        super(0, .25f, .3f, 1.45f, gameObject);
        showDebug = true;
        setRenderOrder(DEBUG_RENDER_ORDER);
        allowClingToWalls = true;
        allowWallJump = true;
        allowGrabLedges = true;
        allowLedgeJump = true;
        grabLedgeThreshold = .3f;
        ledgeGrabMaxDistance = .25f;
        midairJumps = 1;
    }

    @Override
    public void handleControls() {
        var animationIsRoll = getAnimationState(this).getCurrent(0).getAnimation().getName().equals("roll");
        if (!animationIsRoll || movementMode == MovementMode.FALLING) {
            if (Gdx.input.isKeyPressed(Keys.A)) moveLeft();
            else if (Gdx.input.isKeyPressed(Keys.D)) moveRight();
        }

        if (Gdx.input.isKeyPressed(Keys.A)) moveWallClingLeft();
        else if (Gdx.input.isKeyPressed(Keys.D)) moveWallClingRight();

        if (Gdx.input.isKeyPressed(Keys.S)) moveClimbDown();
        if (Gdx.input.isKeyPressed(Keys.S)) movePassThroughFloor();

        if (Gdx.input.isKeyPressed(Keys.W)) moveJump();

        if (queueRoll > 0 && !animationIsRoll || movementMode == MovementMode.WALKING && Gdx.input.isKeyJustPressed(Keys.SPACE) && !animationIsRoll) {
            boolean aimRight = Gdx.input.isKeyPressed(Keys.D) || !Gdx.input.isKeyPressed(Keys.A) && getSkeleton(this).getRootBone().getScaleX() > 0;
            if (aimRight && lateralSpeed < 0 || !aimRight && lateralSpeed > 0) lateralSpeed = 0;
            applyGroundForce(15f, aimRight ? 0 : 180);
            if (Math.abs(lateralSpeed) > 16f) lateralSpeed = Math.signum(lateralSpeed) * 16f;
            setAnimation(0, "roll", false, this);
            addAnimation(0, "running", true, 0, this);
            queueRoll = 0f;
        } else if (movementMode == MovementMode.WALKING && Gdx.input.isKeyJustPressed(Keys.SPACE)) queueRoll = .5f;

        if (movementMode == MovementMode.FALLING && Gdx.input.isKeyJustPressed(Keys.SPACE) && !animationIsRoll) {
            boolean aimRight = Gdx.input.isKeyPressed(Keys.D) || !Gdx.input.isKeyPressed(Keys.A) && getSkeleton(this).getRootBone().getScaleX() > 0;
            if (aimRight && lateralSpeed < 0 || !aimRight && lateralSpeed > 0) lateralSpeed = 0;
            applyAirForce(5f, aimRight ? 45 : 135);
            setAnimation(0, "roll", false, this);
            addAnimation(0, "falling", true, 0, this);
            midairJumpCounter = 1;
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        debugLabel.setText(debugText);
        gameCamera.position.set(getBody(this).getPosition().x, getBody(this).getPosition().y, 1f);
        queueRoll -= delta;
    }

    private void updateFacingDirection(float lateralSpeed) {
        getSkeleton(this).getRootBone().setScale((lateralSpeed < 0 ? -1f : 1f), 1f);
    }

    private void rotateRootBone(float angle) {
        getSkeleton(this).getRootBone().setRotation(angle);
    }

    private void approachRotationRootBone(float angle, float increment) {
        float rotation = getSkeleton(this).getRootBone().getRotation();
        rotateRootBone(approach360(rotation, angle, increment));
    }

    @Override
    public void eventCeilingClingMoving(float delta, float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventWalking(float delta, float lateralSpeed, float groundAngle) {
        updateFacingDirection(lateralSpeed);
        approachRotationRootBone(0, 360f * delta);

        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("land")) return;
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("roll")) return;
        setAnimation(0, "running", true, this);
    }

    @Override
    public void eventWalkStopping(float delta, float lateralSpeed, float groundAngle) {
        updateFacingDirection(lateralSpeed);
        approachRotationRootBone(0, 360f * delta);
    }

    @Override
    public void eventWalkStop(float delta) {
        approachRotationRootBone(0, 360f * delta);
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("land")) return;
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("roll")) return;
        setAnimation(0, "standing", true, this);
    }

    @Override
    public void eventWalkReversing(float delta, float lateralSpeed, float groundAngle) {
        approachRotationRootBone(0, 360f * delta);
    }

    @Override
    public void eventWalkingSlide(float delta, float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventWalkPushingWall(float delta, float wallAngle) {
        setAnimation(0, "walling", true, this);
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
        updateFacingDirection(lateralSpeed);
        approachRotationRootBone(slidingAngle, 180f * delta);

        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("roll")) return;
        setAnimation(0, "sliding", true, this);
    }

    @Override
    public void eventSlidePushingWall(float delta, float wallAngle) {

    }

    @Override
    public void eventJump(float delta) {
        rotateRootBone(0);
        setAnimation(0, "jump", false, this);
        addAnimation(0, "jumping", true, 0, this);
    }

    @Override
    public void eventJumpReleased(float delta) {
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("midair-jump")) return;
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventJumpApex(float delta) {
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("midair-jump")) return;
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventFallMoving(float delta, float lateralSpeed) {
        updateFacingDirection(lateralSpeed);
    }

    @Override
    public void eventJumpFromSlide(float delta) {

    }

    @Override
    public void eventJumpMidair(float delta) {
        setAnimation(0, "midair-jump", false, this);
        addAnimation(0, "falling", true, 0, this);
    }

    @Override
    public void eventHitHead(float delta, float ceilingAngle) {
        setAnimation(0, "jump-hit-head", false, this);
        addAnimation(0, "falling", true, 0, this);
    }

    @Override
    public void eventFalling(float delta) {
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("midair-jump")) return;
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("jump")) return;
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("jumping")) return;
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("jump-hit-head")) return;
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("roll")) return;
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventFallingTouchingWall(float delta, float wallAngle) {

    }

    @Override
    public void eventLand(float delta, float groundAngle) {
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("roll")) return;
        setAnimation(0, "land", false, this);
        addAnimation(0, "standing", true, 0, this);
    }

    @Override
    public void eventWallCling(float delta, float wallAngle) {
        setAnimation(0, "clinging-to-wall", true, this);
    }

    @Override
    public void eventReleaseWallCling(float delta) {
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("midair-jump")) return;
        setAnimation(0, "falling", true, this);
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
        System.out.println("wall jump");
        setAnimation(0, "midair-jump", false, this);
        addAnimation(0, "falling", true, 0, this);
    }

    @Override
    public void eventGrabLedge(float delta, float wallAngle) {
        setAnimation(0, "grabbing-ledge", true, this);
    }

    @Override
    public void eventReleaseGrabLedge(float delta) {
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("midair-jump")) return;
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventLedgeJump(float delta, float wallAngle) {
        System.out.println("ledge jump");
        setAnimation(0, "midair-jump", false, this);
        addAnimation(0, "falling", true, 0, this);
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
