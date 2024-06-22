package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.Skeleton.Physics;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.ray3k.badforce2.Utils;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Core.skeletonJson;
import static com.ray3k.badforce2.Core.skeletonRenderer;
import static com.ray3k.badforce2.Utils.getBody;
import static com.ray3k.badforce2.Utils.getPosition;

public class SpineBehaviour extends BehaviourAdapter {
    public Skeleton skeleton;
    public AnimationState animationState;
    public SkeletonBounds skeletonBounds;
    public boolean useBodyRotation = true;

    public SpineBehaviour(GameObject gameObject, String skeletonDataPath) {
        super(gameObject);
        var skeletonData = skeletonJson.readSkeletonData(Gdx.files.internal(skeletonDataPath));
        var animationData = new AnimationStateData(skeletonData);

        skeleton = new Skeleton(skeletonData);
        animationState = new AnimationState(animationData);
        skeletonBounds = new SkeletonBounds();
    }

    public SpineBehaviour(GameObject gameObject, SkeletonData skeletonData, AnimationStateData animationStateData) {
        super(gameObject);

        skeleton = new Skeleton(skeletonData);
        animationState = new AnimationState(animationStateData);
        skeletonBounds = new SkeletonBounds();
    }

    @Override
    public void awake() {
        Vector2 position = getPosition(this);
        skeleton.setPosition(position.x, position.y);
        animationState.update(0);
        animationState.apply(skeleton);
        skeleton.updateWorldTransform(Physics.update);
        skeletonBounds.update(skeleton, true);
    }

    @Override
    public void start() {

    }

    @Override
    public void fixedUpdate() {

    }

    @Override
    public void update(float delta) {
        skeleton.setPosition(getPosition(this).x, getPosition(this).y);
        if (useBodyRotation) skeleton.getRootBone().setRotation(getBody(getGameObject()).getAngle() * MathUtils.radDeg);
        animationState.update(delta);
        animationState.apply(skeleton);
        skeleton.update(delta);
        skeleton.updateWorldTransform(Physics.update);
        skeletonBounds.update(skeleton, true);
    }

    @Override
    public void render(Batch batch) {
        skeletonRenderer.draw(batch, skeleton);
    }

    public void createChainShapeFixture(String slotName, FixtureDef fixtureDef) {
        createChainShapeFixture(skeleton.findSlot(slotName), fixtureDef);
    }

    public void createChainShapeFixture(Slot slot, FixtureDef fixtureDef) {
        var bbox = (BoundingBoxAttachment) slot.getAttachment();
        var worldVertices = new float[bbox.getWorldVerticesLength()];
        bbox.computeWorldVertices(slot, 0, bbox.getWorldVerticesLength(),  worldVertices, 0, 2);

        if (Utils.isClockwise(worldVertices)) worldVertices = Utils.reverseVertecies(worldVertices);

        new CreateChainShapeFixtureBehaviour(worldVertices, null, fixtureDef, getGameObject());
    }

    public void createEdgeShapeFixture(String slotName, FixtureDef fixtureDef, boolean oneSided) {
        createEdgeShapeFixture(skeleton.findSlot(slotName), fixtureDef, oneSided);
    }

    public void createEdgeShapeFixture(Slot slot, FixtureDef fixtureDef, boolean oneSided) {
        var bbox = (BoundingBoxAttachment) slot.getAttachment();
        var worldVertices = new float[bbox.getWorldVerticesLength()];
        bbox.computeWorldVertices(slot, 0, bbox.getWorldVerticesLength(),  worldVertices, 0, 2);

        if (Utils.isClockwise(worldVertices)) worldVertices = Utils.reverseVertecies(worldVertices);

        new CreateChainShapeFixtureBehaviour(worldVertices, null, fixtureDef, getGameObject());
    }

    public void createPolygonFixture(String slotName, FixtureDef fixtureDef) {
        createPolygonFixture(skeleton.findSlot(slotName), fixtureDef);
    }

    public void createPolygonFixture(Slot slot, FixtureDef fixtureDef) {
        var bbox = (BoundingBoxAttachment) slot.getAttachment();
        var worldVertices = new float[bbox.getWorldVerticesLength()];
        bbox.computeWorldVertices(slot, 0, bbox.getWorldVerticesLength(),  worldVertices, 0, 2);

        if (Utils.isClockwise(worldVertices)) worldVertices = Utils.reverseVertecies(worldVertices);

        new CreatePolygonShapeFixtureBehaviour(worldVertices, null, fixtureDef, getGameObject());
    }
}
