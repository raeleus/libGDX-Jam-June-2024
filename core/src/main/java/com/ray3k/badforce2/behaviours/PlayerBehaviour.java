package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour.BoundsData;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.screens.GameScreen.*;

public class PlayerBehaviour extends SlopeCharacterBehaviour {
    public PlayerBehaviour(GameObject gameObject) {
        super(0, .25f, .3f, 1.45f, gameObject);
        showDebug = true;
        setRenderOrder(DEBUG_RENDER_ORDER);
    }

    @Override
    public void handleControls() {
        if (Gdx.input.isKeyPressed(Keys.A)) moveLeft();
        else if (Gdx.input.isKeyPressed(Keys.D)) moveRight();

        if (Gdx.input.isKeyPressed(Keys.W)) moveJump();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        debugLabel.setText(debugText);
        gameCamera.position.set(getBody(this).getPosition().x, getBody(this).getPosition().y, 1f);
    }

    @Override
    public void eventCeilingClingMoving(float lateralSpeed, float ceilingAngle) {

    }

    private void updateFacingDirection(float lateralSpeed) {
        getSkeleton(this).getRootBone().setScale((lateralSpeed < 0 ? -1f : 1f), 1f);
    }

    @Override
    public void eventWalking(float lateralSpeed, float groundAngle) {
        updateFacingDirection(lateralSpeed);

        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("land")) return;
        setAnimation(0, "running", true, this);
    }

    @Override
    public void eventWalkStopping(float lateralSpeed, float groundAngle) {
        updateFacingDirection(lateralSpeed);
    }

    @Override
    public void eventWalkStop() {
        if (getAnimationState(this).getCurrent(0).getAnimation().getName().equals("land")) return;
        setAnimation(0, "standing", true, this);
    }

    @Override
    public void eventWalkReversing(float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventWalkingSlide(float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventWalkPushingWall(float wallAngle) {
        setAnimation(0, "walling", true, this);
    }

    @Override
    public void eventCliffEdge(boolean right) {

    }

    @Override
    public void eventTouchGroundFixture(Fixture fixture, float contactNormalAngle, BoundsBehaviour bounds,
                                        BoundsData boundsData) {

    }

    @Override
    public void eventSlideSlope(float lateralSpeed, float groundAngle, float slidingAngle) {

    }

    @Override
    public void eventSlidePushingWall(float wallAngle) {

    }

    @Override
    public void eventJump() {
        setAnimation(0, "jump", false, this);
        addAnimation(0, "jumping", true, 0, this);
    }

    @Override
    public void eventJumpReleased() {
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventJumpApex() {
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventFallMoving(float lateralSpeed) {
        updateFacingDirection(lateralSpeed);
    }

    @Override
    public void eventJumpFromSlide() {

    }

    @Override
    public void eventJumpMidair() {

    }

    @Override
    public void eventHitHead(float ceilingAngle) {

    }

    @Override
    public void eventFalling() {

    }

    @Override
    public void eventFallingTouchingWall(float wallAngle) {

    }

    @Override
    public void eventLand(float groundAngle) {
        setAnimation(0, "land", false, this);
        addAnimation(0, "standing", true, 0, this);
    }

    @Override
    public void eventWallCling(float wallAngle) {

    }

    @Override
    public void eventReleaseWallCling() {

    }

    @Override
    public void eventWallSliding(float wallAngle) {

    }

    @Override
    public void eventWallClimbing(float wallAngle) {

    }

    @Override
    public void eventWallClimbReachedTop() {

    }

    @Override
    public void eventWallJump(float wallAngle) {

    }

    @Override
    public void eventGrabLedge(float wallAngle) {

    }

    @Override
    public void eventReleaseGrabLedge() {

    }

    @Override
    public void eventLedgeJump(float wallAngle) {

    }

    @Override
    public void eventCeilingClingPushingWall(float wallContactAngle) {

    }

    @Override
    public void eventCeilingClingStop() {

    }

    @Override
    public void eventCeilingClingStopping(float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventCeilingClingMovingReversing(float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventCeilingClingReleased() {

    }

    @Override
    public void eventMagnetPushingWall(float wallContactAngle) {

    }

    @Override
    public void eventMagnetStop() {

    }

    @Override
    public void eventMagnetStopping(float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventMagnetMoving(float lateralSpeed, float ceilingAngle) {

    }

    @Override
    public void eventMagnetMovingReversing(float lateralSpeed, float groundAngle) {

    }

    @Override
    public void eventMagnetReleased() {

    }

    @Override
    public void eventPassedThroughPlatform(Fixture fixture, float fixtureAngle, BoundsBehaviour bounds,
                                           BoundsData boundsData) {

    }

    @Override
    public void eventSwing(float swingAngle, float lateralSpeed) {

    }

    @Override
    public void eventSwinging(float swingAngle, float lateralSpeed) {

    }

    @Override
    public void eventSwingReleased(float swingAngle, float lateralSpeed, boolean automaticRelease) {

    }

    @Override
    public void eventSwingCrashWall(float swingAngle, float lateralSpeed) {

    }

    @Override
    public void eventSwingCrashGround(float swingAngle, float lateralSpeed) {

    }
}
