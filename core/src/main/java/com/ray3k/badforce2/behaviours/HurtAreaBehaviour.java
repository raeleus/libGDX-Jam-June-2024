package com.ray3k.badforce2.behaviours;

import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

public class HurtAreaBehaviour extends BoundsBehaviour {
    public HurtAreaBehaviour(float[] points, GameObject gameObject) {
        super(points, gameObject);
    }
}
