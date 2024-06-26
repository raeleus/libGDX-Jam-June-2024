package com.ray3k.badforce2;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.spine.*;
import com.ray3k.badforce2.behaviours.SpineBehaviour;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.GameObject;

public class Utils {
    private static final Vector2 temp1 = new Vector2();
    private static final Vector2 temp2 = new Vector2();

    public static void onChange(Actor actor, Runnable runnable) {
        actor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                runnable.run();
            }
        });
    }

    public static void onClick(Actor actor, Runnable runnable) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                runnable.run();
            }
        });
    }

    public static void onRightClick(Actor actor, Runnable runnable) {
        var clickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                runnable.run();
            }
        };
        clickListener.setButton(Buttons.RIGHT);
        actor.addListener(clickListener);
    }

    public static Body getBody(Behaviour behaviour) {
        return getBody(behaviour.getGameObject());
    }

    public static Body getBody(GameObject gameObject) {
        var behaviour = gameObject.getBox2dBehaviour();
        if (behaviour == null) return null;
        return behaviour.getBody();
    }

    public static Vector2 getPosition(Behaviour behaviour) {
        return getPosition(behaviour.getGameObject());
    }

    public static Vector2 getPosition(GameObject gameObject) {
        return getBody(gameObject).getPosition();
    }

    public static Vector2 getLinearVelocity(Behaviour behaviour) {
        return getLinearVelocity(behaviour.getGameObject());
    }

    public static Vector2 getLinearVelocity(GameObject gameObject) {
        return getBody(gameObject).getLinearVelocity();
    }

    public static Skeleton getSkeleton(Behaviour behaviour) {
        return getSkeleton(behaviour.getGameObject());
    }

    public static Skeleton getSkeleton(GameObject gameObject) {
        var behaviour = gameObject.getBehaviour(SpineBehaviour.class);
        if (behaviour == null) return null;
        return behaviour.skeleton;
    }

    public static AnimationState getAnimationState(Behaviour behaviour) {
        return getAnimationState(behaviour.getGameObject());
    }

    public static AnimationState getAnimationState(GameObject gameObject) {
        var behaviour = gameObject.getBehaviour(SpineBehaviour.class);
        if (behaviour == null) return null;
        return behaviour.animationState;
    }

    public static void setAnimation(int track, String name, boolean looping, Behaviour behaviour) {
        setAnimation(track, name, looping, behaviour.getGameObject());
    }

    public static void setAnimation(int track, String name, boolean looping, GameObject gameObject) {
        var skeleton = getSkeleton(gameObject);
        var animationState = getAnimationState(gameObject);

        var animation = skeleton.getData().findAnimation(name);
        if (animationState.getCurrent(track) == null || animationState.getCurrent(track).getAnimation() != animation) animationState.setAnimation(track, animation, looping);
    }

    public static void addAnimation(int track, String name, boolean looping, float delay, Behaviour behaviour) {
        addAnimation(track, name, looping, delay, behaviour.getGameObject());
    }

    public static void addAnimation(int track, String name, boolean looping, float delay, GameObject gameObject) {
        var skeleton = getSkeleton(gameObject);
        var animationState = getAnimationState(gameObject);

        var animation = skeleton.getData().findAnimation(name);
        if (animationState.getCurrent(track).getAnimation() != animation) animationState.addAnimation(track, animation, looping, delay);
    }

    public static boolean animationNameEquals(int track, String name, Behaviour behaviour) {
        return animationNameEquals(track, name, behaviour.getGameObject());
    }

    public static boolean animationNameEquals(int track, String name, GameObject gameObject) {
        return getAnimationState(gameObject).getCurrent(track) != null && getAnimationState(gameObject).getCurrent(track).getAnimation().getName().equals(name);
    }

    public static Bone findBone(String name, Behaviour behaviour) {
        return findBone(name, behaviour.getGameObject());
    }

    public static Bone findBone(String name, GameObject gameObject) {
        return getSkeleton(gameObject).findBone(name);
    }

    public static Slot findSlot(String name, Behaviour behaviour) {
        return findSlot(name, behaviour.getGameObject());
    }

    public static Slot findSlot(String name, GameObject gameObject) {
        return getSkeleton(gameObject).findSlot(name);
    }

    public static SkeletonBounds getSkeletonBounds(GameObject gameObject) {
        var behaviour = gameObject.getBehaviour(SpineBehaviour.class);
        if (behaviour == null) return null;
        return behaviour.skeletonBounds;
    }

    public static float degDifference(float sourceAngle, float targetAngle) {
        var angle = targetAngle - sourceAngle;
        angle = mod((angle + 180), 360) - 180;
        return angle;
    }

    private static float mod(float a, float n) {
        return (a % n + n) % n;
    }

    public static float approach(float start, float target, float increment) {
        increment = Math.abs(increment);
        if (start < target) {
            start += increment;

            if (start > target) {
                start = target;
            }
        } else {
            start -= increment;

            if (start < target) {
                start = target;
            }
        }
        return start;
    }

    public static float approach360(float start, float target, float increment) {
        float delta = ((target - start + 360 + 180) % 360) - 180;
        return (start + Math.signum(delta) * MathUtils.clamp(increment, 0.0f, Math.abs(delta)) + 360) % 360;
    }

    /**
     * Determine if a polygon is wound clockwise. It may be concave or convex.
     * @param points
     * @return
     */
    public static boolean isClockwise(float[] points) {
        var sum = 0;
        for (int i = 0; i + 3 < points.length; i += 2) {
            sum += (points[i + 2] - points[i]) * (points[i + 3] + points[i + 1]);
        }
        sum += (points[0] - points[points.length - 2]) * (points[1] + points[points.length - 1]);
        return sum > 0;
    }

    public static float[] reverseVertecies(float[] points) {
        var newPoints = new float[points.length];
        for (int i = 0; i < points.length; i += 2) {
            var x = points[i];
            var y = points[i + 1];

            newPoints[points.length - i - 2] = x;
            newPoints[points.length - i - 1] = y;
        }
        return newPoints;
    }

    public static float pointDistance(float x1, float y1, float x2, float y2) {
        temp1.set(x1, y1);
        return temp1.dst(x2, y2);
    }

    public static float pointDistance(Body body1, Body body2) {
         temp1.set(body1.getPosition());
         temp2.set(body2.getPosition());
        return pointDistance(temp1.x, temp1.y, temp2.x, temp2.y);
    }

    public static float pointDistance(Behaviour behaviour1, Behaviour behaviour2) {
        return pointDistance(getBody(behaviour1), getBody(behaviour2));
    }

    public static float pointDirection(float x1, float y1, float x2, float y2) {
        temp1.set(x2, y2);
        temp1.sub(x1, y1);
        return temp1.angleDeg();
    }

    public static float pointDirection(Body body1, Body body2) {
        temp1.set(body1.getPosition());
        temp2.set(body2.getPosition());
        return pointDirection(temp1.x, temp1.y, temp2.x, temp2.y);
    }

    public static float pointDirection(Behaviour behaviour1, Behaviour behaviour2) {
        return pointDirection(getBody(behaviour1), getBody(behaviour2));
    }

    public static boolean isEqual360(float a, float b, float tolerance) {
        return MathUtils.isZero((a - b + 180 + 360) % 360 - 180, tolerance);
    }

    public static boolean isEqual360(float a, float b) {
        return isEqual360(a, b, MathUtils.FLOAT_ROUNDING_ERROR);
    }

    public static float throttledAcceleration(float speed, float maxSpeed, float acceleration, boolean maintainExtraMomentum) {
        acceleration *= (1 - speed / maxSpeed);
        if (maintainExtraMomentum && Math.signum(acceleration) != Math.signum(maxSpeed)) {
            acceleration = 0;
        }
        return speed + acceleration;
    }

    public static float throttledDeceleration(float speed, float maxSpeed, float minDeceleration, float deceleration) {
        deceleration *= (1 - Math.abs(speed) / maxSpeed);
        if (deceleration < minDeceleration) deceleration = minDeceleration;
        return Utils.approach(speed, 0, deceleration);
    }
}
