package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.esotericsoftware.spine.Skeleton.Physics;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour.BoundsData;
import com.ray3k.badforce2.behaviours.slope.SlopeCharacterBehaviour;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.GameObject;

import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.screens.GameScreen.*;

public class PlayerBehaviour extends SlopeCharacterBehaviour {
    private float queueRoll;
    private static Vector2 temp1 = new Vector2();

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
        if (animationNameEquals(0, "disappear", this)) return;
        var animationIsRoll = animationNameEquals(0, "roll", this);
        if (!animationIsRoll || movementMode == MovementMode.FALLING) {
            if (Gdx.input.isKeyPressed(Keys.A)) moveLeft();
            else if (Gdx.input.isKeyPressed(Keys.D)) moveRight();
        }

        if (Gdx.input.isKeyPressed(Keys.A)) moveWallClingLeft();
        else if (Gdx.input.isKeyPressed(Keys.D)) moveWallClingRight();

        if (Gdx.input.isKeyPressed(Keys.S)) moveClimbDown();
        if (Gdx.input.isKeyPressed(Keys.S)) movePassThroughFloor();

        if (!animationIsRoll && Gdx.input.isKeyPressed(Keys.W)) moveJump();

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
            applyAirForce(8f, aimRight ? 45 : 135);
            setAnimation(0, "air-roll", false, this);
            midairJumpCounter = 1;
        }

        if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
            setAnimation(1, "shooting", true, this);
            setAnimation(2, "aiming", true, this);
        } else {
            setAnimation(1, "not-shooting", true, this);
            setAnimation(2, "not-aiming", true, this);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        debugLabel.setText(debugText);
        updateCamera();

        queueRoll -= delta;
    }

    private void updateCamera() {
        gameCamera.position.set(getBody(this).getPosition().x, getBody(this).getPosition().y, 1f);

        if (gameCamera.viewportWidth > levelWidth) {
            gameCamera.position.x = levelWidth / 2f;
        } else {
            if (gameCamera.position.x - gameCamera.viewportWidth / 2f < 0)
                gameCamera.position.x = gameCamera.viewportWidth / 2f;
            if (gameCamera.position.x + gameCamera.viewportWidth / 2f > levelWidth)
                gameCamera.position.x = levelWidth - gameCamera.viewportWidth / 2f;
        }

        if (gameCamera.viewportHeight > levelHeight) {
            gameCamera.position.y = levelHeight / 2f;
        } else {
            if (gameCamera.position.y - gameCamera.viewportHeight / 2f < 0)
                gameCamera.position.y = gameCamera.viewportHeight / 2f;
            if (gameCamera.position.y + gameCamera.viewportHeight / 2f > levelHeight)
                gameCamera.position.y = levelHeight - gameCamera.viewportHeight / 2f;
        }
    }

    @Override
    public void lateUpdate(float delta) {
        super.lateUpdate(delta);
        updateTargetBone();
    }

    private void updateTargetBone() {
        getSkeleton(this).updateWorldTransform(Physics.pose);
        var bone = findBone("target", this);
        temp1.set(Gdx.input.getX(), Gdx.input.getY());
        gameViewport.unproject(temp1);
        bone.getParent().worldToLocal(temp1);
        bone.setPosition(temp1.x, temp1.y);
    }

    private void updateFacingDirection(float lateralSpeed) {
        getSkeleton(this).getRootBone().setScale((lateralSpeed < 0 ? -1f : 1f), 1f);
        updateTargetBone();
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

        if (animationNameEquals(0, "land", this)) return;
        if (animationNameEquals(0, "roll", this)) return;
        if (animationNameEquals(0, "land-roll", this)) return;
        if (animationNameEquals(0, "disappear", this)) return;
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
        if (animationNameEquals(0, "land", this)) return;
        if (animationNameEquals(0, "roll", this)) return;
        if (animationNameEquals(0, "land-roll", this)) return;
        if (animationNameEquals(0, "disappear", this)) return;
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
        if (animationNameEquals(0, "disappear", this)) return;
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

        if (animationNameEquals(0, "roll", this)) return;
        if (animationNameEquals(0, "land-roll", this)) return;
        if (animationNameEquals(0, "disappear", this)) return;
        setAnimation(0, "sliding", true, this);
    }

    @Override
    public void eventSlidePushingWall(float delta, float wallAngle) {

    }

    @Override
    public void eventJump(float delta) {
        if (animationNameEquals(0, "disappear", this)) return;
        rotateRootBone(0);
        setAnimation(0, "jump", false, this);
        addAnimation(0, "jumping", true, 0, this);
    }

    @Override
    public void eventJumpReleased(float delta) {
        if (animationNameEquals(0, "midair-jump", this)) return;
        if (animationNameEquals(0, "disappear", this)) return;
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventJumpApex(float delta) {
        if (animationNameEquals(0, "midair-jump", this)) return;
        if (animationNameEquals(0, "disappear", this)) return;
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
        if (animationNameEquals(0, "disappear", this)) return;
        setAnimation(0, "midair-jump", false, this);
        addAnimation(0, "falling", true, 0, this);
    }

    @Override
    public void eventHitHead(float delta, float ceilingAngle) {
        if (animationNameEquals(0, "air-roll", this)) return;
        if (animationNameEquals(0, "disappear", this)) return;
        setAnimation(0, "jump-hit-head", false, this);
        addAnimation(0, "falling", true, 0, this);
    }

    @Override
    public void eventFalling(float delta) {
        if (animationNameEquals(0, "midair-jump", this)) return;
        if (animationNameEquals(0, "jump", this)) return;
        if (animationNameEquals(0, "jumping", this)) return;
        if (animationNameEquals(0, "jump-hit-head", this)) return;
        if (animationNameEquals(0, "roll", this)) return;
        if (animationNameEquals(0, "land-roll", this)) return;
        if (animationNameEquals(0, "air-roll", this)) return;
        if (animationNameEquals(0, "disappear", this)) return;
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventFallingTouchingWall(float delta, float wallAngle) {

    }

    @Override
    public void eventLand(float delta, float groundAngle) {
        if (animationNameEquals(0, "disappear", this)) return;
        if (animationNameEquals(0, "air-roll", this)) {
            setAnimation(0, "land-roll", false, this);
            addAnimation(0, "standing", true, 0, this);
            return;
        }
        setAnimation(0, "land", false, this);
        addAnimation(0, "standing", true, 0, this);
    }

    @Override
    public void eventWallCling(float delta, float wallAngle) {
        setAnimation(0, "clinging-to-wall", true, this);
    }

    @Override
    public void eventReleaseWallCling(float delta) {
        if (animationNameEquals(0, "disappear", this)) return;
        if (animationNameEquals(0, "midair-jump", this)) return;
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
        setAnimation(0, "midair-jump", false, this);
        addAnimation(0, "falling", true, 0, this);
    }

    @Override
    public void eventGrabLedge(float delta, float wallAngle) {
        setAnimation(0, "grabbing-ledge", true, this);
    }

    @Override
    public void eventReleaseGrabLedge(float delta) {
        if (animationNameEquals(0, "disappear", this)) return;
        if (animationNameEquals(0, "midair-jump", this)) return;
        setAnimation(0, "falling", true, this);
    }

    @Override
    public void eventLedgeJump(float delta, float wallAngle) {
        if (animationNameEquals(0, "disappear", this)) return;
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

    @Override
    public boolean onCollisionPreSolve(Behaviour other, Contact contact, Manifold oldManifold) {
        var returnValue = super.onCollisionPreSolve(other, contact, oldManifold);

        if (other.getGameObject().hasBehaviour(DoorBehaviour.class)) contact.setEnabled(false);

        return returnValue;
    }

    @Override
    public void onCollisionEnter(Behaviour other, Contact contact) {
        super.onCollisionEnter(other, contact);
        if (other.getGameObject().hasBehaviour(DoorBehaviour.class)) {
            setAnimation(0, "disappear", false, this);
            lateralSpeed = 0;
            getBody(this).setLinearVelocity(0, 0);
        }
    }
}
