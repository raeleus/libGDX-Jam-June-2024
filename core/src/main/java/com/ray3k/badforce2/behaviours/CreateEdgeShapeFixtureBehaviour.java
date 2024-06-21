package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.fixtures.CreateFixtureBehaviour;

/**
 * Creates a box fixture in awake with the provided parameters.
 */
public class CreateEdgeShapeFixtureBehaviour extends CreateFixtureBehaviour {
    private final Array<EdgeShape> edges;
    private static final Vector2 v0 = new Vector2(), v1 = new Vector2(), v2 = new Vector2(), v3 = new Vector2();

    public CreateEdgeShapeFixtureBehaviour(float[] vertices, GameObject gameObject, boolean oneSided) {
        this(vertices, Vector2.Zero, gameObject, oneSided);
    }

    public CreateEdgeShapeFixtureBehaviour(float[] vertices, Vector2 position, GameObject gameObject, boolean oneSided) {
        this(vertices, position, new FixtureDef(), gameObject, oneSided);
    }

    public CreateEdgeShapeFixtureBehaviour(float[] vertices, Vector2 position, FixtureDef fixtureDef, GameObject gameObject, boolean oneSided) {
        super(fixtureDef, gameObject);
        edges = new Array<>();
        var edge = new EdgeShape();
        for (int i = 0; i <= vertices.length - 2; i += 2) {
            if (!oneSided) {
                var v2Index = i == vertices.length - 2 ? 0 : i + 2;
                v1.set(vertices[i], vertices[i + 1]);
                v2.set(vertices[v2Index], vertices[v2Index + 1]);

                if (vertices.length == 4) continue;

                var v0Index = i == 0 ? vertices.length - 2 : i - 2;
                v0.set(vertices[v0Index], vertices[v0Index + 1]);

                var v3Index = v2Index + 2;
                if (v3Index >= vertices.length) v3Index -= vertices.length;
                v3.set(vertices[v3Index], vertices[v3Index + 1]);

                edge.setOneSided(v0, v1, v2, v3);
            }
        }
    }

    @Override
    public void awake() {
        for (var edge : edges) {
            createAndAttachFixture(edge);
        }
    }
}
