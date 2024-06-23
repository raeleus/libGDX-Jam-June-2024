package com.ray3k.badforce2;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.ray3k.badforce2.OgmoReader.EntityNode;
import com.ray3k.badforce2.OgmoReader.OgmoValue;
import com.ray3k.badforce2.behaviours.PlayerBehaviour;
import com.ray3k.badforce2.behaviours.SpineBehaviour;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import dev.lyze.gdxUnBox2d.Box2dBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

import static com.ray3k.badforce2.screens.GameScreen.p2m;
import static com.ray3k.badforce2.screens.GameScreen.unBox;

public class LevelReader extends OgmoReader.OgmoAdapter {
    @Override
    public void entity(String name, int id, int x, int y, int width, int height, boolean flippedX, boolean flippedY,
                       int originX, int originY, int rotation, Array<EntityNode> nodes,
                       ObjectMap<String, OgmoValue> valuesMap) {
        switch (name) {
            case "player":
                var player = new GameObject(unBox);
                var bodyDef = new BodyDef();
                bodyDef.type = BodyType.DynamicBody;
                bodyDef.fixedRotation = true;
                bodyDef.position.set(p2m(x), p2m(y));
                bodyDef.allowSleep = false;
                new Box2dBehaviour(bodyDef, player);
                new PlayerBehaviour(player);

                var spine = new SpineBehaviour(player, "spine/player.json");
                spine.animationState.getData().setMix("jump", "falling", .25f);
                spine.animationState.getData().setMix("jumping", "falling", .25f);
                spine.animationState.getData().setMix("running", "standing", .1f);
                spine.animationState.getData().setMix("land", "running", .25f);
                spine.animationState.getData().setMix("land", "walling", .25f);
                spine.animationState.getData().setMix("standing", "running", .25f);
                spine.animationState.getData().setMix("running", "walling", .25f);
                spine.animationState.getData().setMix("walling", "running", .25f);
                spine.animationState.getData().setMix("standing", "walling", .25f);
                spine.animationState.getData().setMix("walling", "standing", .25f);
                spine.animationState.getData().setMix("sliding", "standing", .25f);
                spine.animationState.getData().setMix("sliding", "running", .25f);
                spine.animationState.getData().setMix("falling", "sliding", .25f);
                spine.animationState.getData().setMix("running", "sliding", .25f);
                spine.animationState.getData().setMix("jumping", "jump-hit-head", .1f);
                spine.animationState.getData().setMix("jump", "jump-hit-head", .1f);
                spine.animationState.getData().setMix("grabbing-ledge", "falling", .1f);
                spine.animationState.getData().setMix("falling", "grabbing-ledge", .1f);
                spine.animationState.getData().setMix("jumping", "grabbing-ledge", .1f);
                spine.animationState.getData().setMix("clinging-to-wall", "grabbing-ledge", .25f);
                spine.animationState.getData().setMix("running", "falling", .25f);
                spine.animationState.getData().setMix("land", "falling", .25f);
                spine.skeleton.setScale(p2m(1), p2m(1));
                spine.skeleton.setSkin("assault");
                spine.animationState.setAnimation(0, "standing", true);
                spine.useBodyRotation = false;
                break;

            case "platform":
                var ground = new GameObject(unBox);
                bodyDef = new BodyDef();
                bodyDef.type = BodyType.StaticBody;
                bodyDef.position.set(0, 0);
                new Box2dBehaviour(bodyDef, ground);

                FloatArray points = new FloatArray();
                points.add(p2m(x), p2m(y));
                for (var node : nodes) {
                    points.add(p2m(node.x), p2m(node.y));
                }
                new BoundsBehaviour(points.toArray(), ground);
                break;
        }
    }
}
