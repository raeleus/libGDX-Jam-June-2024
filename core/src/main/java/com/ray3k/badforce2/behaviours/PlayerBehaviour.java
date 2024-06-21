package com.ray3k.badforce2.behaviours;

import com.ray3k.badforce2.Utils;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Utils.*;

public class PlayerBehaviour extends BehaviourAdapter {
    public PlayerBehaviour(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {

    }

    @Override
    public void fixedUpdate() {
//        getBody(this).applyLinearImpulse(20f, 20f, 0, 0, true);
//        getBody(this).applyAngularImpulse(10, true);
        getBody(this).setLinearVelocity(.25f, .25f);
    }
}
