package com.ray3k.badforce2.behaviours;

import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.screens.MenuScreen;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

public class TimerBehaviour extends BehaviourAdapter {
    private float time = 60f;

    public TimerBehaviour(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void update(float delta) {
        time -= delta;
        if (time <= 0) {
            getGameObject().destroy();
            Core.core.setScreen(new MenuScreen());
        }
    }
}
