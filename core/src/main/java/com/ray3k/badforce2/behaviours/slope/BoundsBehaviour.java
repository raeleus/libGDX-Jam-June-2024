package com.ray3k.badforce2.behaviours.slope;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Utils.getBody;
import static com.ray3k.badforce2.Utils.isClockwise;
import static com.ray3k.badforce2.behaviours.slope.SlopeValues.CATEGORY_BOUNDS;

public class BoundsBehaviour extends BehaviourAdapter {
    public float[] points;
    public int edgeCount;
    public boolean canPassThroughBottom;
    public boolean ceilingClingable;
    public short category = CATEGORY_BOUNDS;

    public float deltaX;
    public float deltaY;

    public BoundsBehaviour(float[] points, GameObject gameObject) {
        super(gameObject);
        this.points = points;
    }

    public static Vector2 temp1 = new Vector2();
    public static Vector2 temp2 = new Vector2();

    @Override
    public void awake() {
        var body = getBody(getGameObject());
        body.setUserData(this);

        boolean clockwise = isClockwise(points);
        Fixture previousFixture = null;
        Fixture firstFixture = null;
        for (int i = 0; i + 1 < points.length; i += 2) {
            EdgeShape edgeShape = new EdgeShape();
            edgeShape.setHasVertex0(true);
            edgeShape.setHasVertex3(true);

            float nextX, nextY;
            if (i + 3 < points.length) {
                nextX = points[i + 2];
                nextY = points[i + 3];
            } else {
                nextX = points[0];
                nextY = points[1];
            }
            edgeShape.set(points[i], points[i + 1], nextX, nextY);

            if (i - 2 >= 0) {
                edgeShape.setVertex0(points[i - 2], points[i - 1]);
            } else {
                edgeShape.setVertex0(points[points.length - 2], points[points.length - 1]);
            }

            if (i + 5 < points.length)
                edgeShape.setVertex3(points[i + 4], points[i + 5]);
            else
                edgeShape.setVertex3(points[i + 4 - points.length], points[i + 5 - points.length]);

            var fixture = body.createFixture(edgeShape, .5f);
            edgeCount ++;
            fixture.setFriction(0);
            fixture.getFilterData().categoryBits = category;

            var data = new BoundsData();
            data.fixture = fixture;
            temp1.set(nextX, nextY);
            temp2.set(points[i], points[i + 1]);
            if (!clockwise) {
                temp2.sub(temp1);
                data.angle = (temp2.angleDeg() + 90) % 360;
            } else {
                temp1.sub(temp2);
                data.angle = (temp1.angleDeg() + 90) % 360;
            }
            data.previousFixture = previousFixture;
            if (previousFixture != null) ((BoundsData) previousFixture.getUserData()).nextFixture = fixture;
            if (i == points.length - 2) {
                data.nextFixture = firstFixture;
                ((BoundsData)firstFixture.getUserData()).previousFixture = fixture;
            }

            fixture.setUserData(data);
            previousFixture = fixture;
            if (i == 0) firstFixture = fixture;
            edgeShape.dispose();
        }

        if (!clockwise) {
            for (var fixture : body.getFixtureList()) {
                var boundsData = (BoundsData) fixture.getUserData();
                var temp = boundsData.previousFixture;
                boundsData.previousFixture = boundsData.nextFixture;
                boundsData.nextFixture = temp;
            }
        }
    }

    public static class BoundsData {
        public float angle;
        public Fixture previousFixture;
        public Fixture nextFixture;
        public Fixture fixture;

        public int distanceToFixture(Fixture otherFixture) {
            if (fixture == otherFixture) return 0;
            var bounds = (BoundsBehaviour) fixture.getBody().getUserData();
            if (otherFixture.getBody().getUserData() != bounds) return Integer.MAX_VALUE;
            var distance = 1;
            var fix = nextFixture;
            while (fix != otherFixture) {
                distance++;
                fix = ((BoundsData)fix.getUserData()).nextFixture;
            }

            return Math.min(bounds.edgeCount - distance, distance);
        }

        public boolean checkFixturesBetween(Fixture otherFixture, CompareBoundsFixtures compare) {
            var bounds = (BoundsBehaviour) fixture.getBody().getUserData();
            if (otherFixture.getBody().getUserData() != bounds) return false;

            if (fixture == otherFixture || nextFixture == otherFixture || previousFixture == otherFixture) return true;

            var accepted = true;
            var distanceNext = 1;
            var nextFix = nextFixture;
            while (nextFix != otherFixture) {
                if (!compare.accept(nextFix)) {
                    accepted = false;
                    break;
                }
                nextFix = ((BoundsData)nextFix.getUserData()).nextFixture;
                distanceNext++;
            }

            if (!accepted) {
                accepted = true;
                var distancePrevious = 1;
                var prevFix = previousFixture;
                while (prevFix != otherFixture) {
                    if (!compare.accept(prevFix) || distancePrevious > distanceNext) {
                        accepted = false;
                        break;
                    }
                    prevFix = ((BoundsData) prevFix.getUserData()).previousFixture;
                    distancePrevious++;
                }
            }
            return accepted;
        }
    }

    public static interface CompareBoundsFixtures {
        public boolean accept(Fixture fixture);
    }
}
