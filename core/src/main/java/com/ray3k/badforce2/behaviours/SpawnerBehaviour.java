package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.math.Vector2;
import com.ray3k.badforce2.LevelReader;
import com.ray3k.badforce2.Utils;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import java.util.concurrent.Delayed;

public class SpawnerBehaviour extends BehaviourAdapter {
    public enum Type {
        ALIEN, CRAWLER, FLIER
    }
    public Type type;
    public float angle;
    public float speed;
    public float delay;
    public float initialDelay;
    public float x;
    public float y;
    private float timer;
    private static final Vector2 temp1 = new Vector2();

    public SpawnerBehaviour(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void awake() {
        timer = initialDelay;
    }

    @Override
    public void update(float delta) {
        timer -= delta;
        if (timer <= 0) {
            timer = delay;
            spawnEnemy();
        }
    }

    private void spawnEnemy() {
        AlienBehaviour behaviour;
        temp1.set(speed, 0);
        temp1.rotateDeg(angle);
        switch (type) {
            case FLIER:
                behaviour = LevelReader.spawnFlier(x, y, temp1.x, temp1.y);
                behaviour.gravity = -1f;
                break;
            case CRAWLER:
                behaviour = LevelReader.spawnCrawler(x, y, temp1.x, temp1.y, false);
                behaviour.lateralMaxSpeed = 12f;
                break;
            case ALIEN:
            default:
                behaviour = LevelReader.spawnAlien(x, y, temp1.x, temp1.y);
                break;
        }
        behaviour.lateralSpeed = temp1.x;
    }
}
