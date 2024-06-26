package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.audio.Sound;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

public class PitBehaviour extends BoundsBehaviour {
    public Sound sound;
    public PitBehaviour(float[] points, Sound sound, GameObject gameObject) {
        super(points, gameObject);
        this.sound = sound;
    }
}
