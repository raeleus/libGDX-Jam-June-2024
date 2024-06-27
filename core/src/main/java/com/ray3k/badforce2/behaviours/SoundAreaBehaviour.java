package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.audio.Sound;
import com.ray3k.badforce2.behaviours.slope.BoundsBehaviour;
import dev.lyze.gdxUnBox2d.GameObject;

public class SoundAreaBehaviour extends BoundsBehaviour {
    public Sound sound;
    boolean destroy;
    public SoundAreaBehaviour(float[] points, Sound sound, boolean destroy, GameObject gameObject) {
        super(points, gameObject);
        this.sound = sound;
        this.destroy = destroy;
    }
}
