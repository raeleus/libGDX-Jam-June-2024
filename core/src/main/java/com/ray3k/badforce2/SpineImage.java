package com.ray3k.badforce2;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.esotericsoftware.spine.*;

public class SpineImage extends Image {
    public SpineDrawable spineDrawable;
    public boolean clip = true;
    private static final Vector2 temp = new Vector2();

    public SpineImage(SkeletonRenderer skeletonRenderer, SkeletonData skeletonData, AnimationStateData animationStateData) {
        spineDrawable = new SpineDrawable(skeletonRenderer, skeletonData, animationStateData);
        setDrawable(spineDrawable);
    }

    public SpineImage(SkeletonRenderer skeletonRenderer, Skeleton skeleton, AnimationState animationState) {
        spineDrawable = new SpineDrawable(skeletonRenderer, skeleton, animationState);
        setDrawable(spineDrawable);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        spineDrawable.update(delta);
    }

    public void setCrop(float x, float y, float width, float height) {
        spineDrawable.setCrop(x, y, width, height);
    }

    public AnimationState getAnimationState() {
        return spineDrawable.getAnimationState();
    }

    public Skeleton getSkeleton() {
        return spineDrawable.getSkeleton();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!clip) {
            super.draw(batch, parentAlpha);
            return;
        }

        batch.flush();
        validate();
        localToStageCoordinates(temp.set(getImageX(), getImageY()));

        if (clipBegin(temp.x, temp.y, getImageWidth(), getImageHeight())) {
            super.draw(batch, parentAlpha);
            batch.flush();
            clipEnd();
        }
    }
}
