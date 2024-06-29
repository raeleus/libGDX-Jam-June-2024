package com.ray3k.badforce2.behaviours;

import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.Utils;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.Core.*;
import static com.ray3k.badforce2.Utils.*;
import static com.ray3k.badforce2.behaviours.PlayerBehaviour.*;

public class BonusBehaviour extends BehaviourAdapter {
    private Runnable runnable;

    public BonusBehaviour(GameObject gameObject, Runnable runnable) {
        super(gameObject);
        this.runnable = runnable;
    }

    @Override
    public void update(float delta) {
        if (getBody(player) != null && pointDistance(this, player) < 3) {
            getGameObject().destroy();
            runnable.run();
            sfx_complete.play();
        }
    }
}
