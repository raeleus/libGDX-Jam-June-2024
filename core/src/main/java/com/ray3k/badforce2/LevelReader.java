package com.ray3k.badforce2;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.AnimationState.AnimationStateAdapter;
import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.ray3k.badforce2.OgmoReader.EntityNode;
import com.ray3k.badforce2.OgmoReader.OgmoValue;
import com.ray3k.badforce2.behaviours.DoorBehaviour;
import com.ray3k.badforce2.behaviours.ImageBehaviour;
import com.ray3k.badforce2.behaviours.PlayerBehaviour;
import com.ray3k.badforce2.behaviours.SpineBehaviour;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import com.ray3k.badforce2.screens.GameScreen;
import dev.lyze.gdxUnBox2d.Box2dBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

import static com.ray3k.badforce2.screens.GameScreen.*;

public class LevelReader extends OgmoReader.OgmoAdapter {
    private String layerName;

    @Override
    public void level(String ogmoVersion, int width, int height, int offsetX, int offsetY,
                      ObjectMap<String, OgmoValue> valuesMap) {
        if (valuesMap.containsKey("next-level")) GameScreen.nextLevelName = valuesMap.get("next-level").asString();
    }

    @Override
    public void layer(String name, int gridCellWidth, int gridCellHeight, int offsetX, int offsetY) {
        layerName = name;
    }

    @Override
    public void decal(int x, int y, float originX, float originY, float scaleX, float scaleY, int rotation,
                      String texture, String folder, ObjectMap<String, OgmoValue> valuesMap) {
        var image = new GameObject(unBox);
        new ImageBehaviour(texture, p2m(x), p2m(y), image, layerName.equals("foreground") ? FOREGROUND_RENDER_ORDER : BACKGROUND_RENDER_ORDER);
    }

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
                spine.animationState.getData().setMix("air-roll", "grabbing-ledge", .1f);
                spine.animationState.getData().setMix("air-roll", "clinging-to-wall", .1f);
                spine.skeleton.setScale(p2m(1), p2m(1));
                spine.skeleton.setSkin("assault");
                spine.animationState.setAnimation(0, "standing", true);
                spine.useBodyRotation = false;
                spine.animationState.addListener(new AnimationStateAdapter() {
                    @Override
                    public void complete(TrackEntry entry) {
                        if (entry.getAnimation().getName().equals("disappear")) Core.core.setScreen(new GameScreen(nextLevelName));
                    }
                });
                break;

            case "platform":
                var ground = new GameObject(unBox);
                bodyDef = new BodyDef();
                bodyDef.type = BodyType.StaticBody;
                bodyDef.position.set(0, 0);
                new Box2dBehaviour(bodyDef, ground);

                var points = new FloatArray();
                points.add(p2m(x), p2m(y));
                for (var node : nodes) {
                    points.add(p2m(node.x), p2m(node.y));
                }
                new BoundsBehaviour(points.toArray(), ground);
                break;
            case "platform-pass-through":
                ground = new GameObject(unBox);
                bodyDef = new BodyDef();
                bodyDef.type = BodyType.StaticBody;
                bodyDef.position.set(0, 0);
                new Box2dBehaviour(bodyDef, ground);

                points = new FloatArray();
                points.add(p2m(x), p2m(y));
                for (var node : nodes) {
                    points.add(p2m(node.x), p2m(node.y));
                }
                var bounds = new BoundsBehaviour(points.toArray(), ground);
                bounds.canPassThroughBottom = true;
                break;
            case "door":
                ground = new GameObject(unBox);
                bodyDef = new BodyDef();
                bodyDef.type = BodyType.StaticBody;
                bodyDef.position.set(0, 0);
                new Box2dBehaviour(bodyDef, ground);

                points = new FloatArray();
                points.add(p2m(x), p2m(y));
                for (var node : nodes) {
                    points.add(p2m(node.x), p2m(node.y));
                }
                new DoorBehaviour(points.toArray(), ground);
                break;
        }
    }
}
