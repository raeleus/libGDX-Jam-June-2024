package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.physics.box2d.Contact;
import com.ray3k.badforce2.Utils;
import dev.lyze.gdxUnBox2d.Behaviour;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Utils.*;

public class AlienBehaviour extends BehaviourAdapter {
    private float health = 100;

    public AlienBehaviour(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void awake() {
        getBody(this).setLinearVelocity(-5f, 0f);
    }

    @Override
    public void onCollisionEnter(Behaviour other, Contact contact) {

    }
}
