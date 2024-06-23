package com.ray3k.badforce2.behaviours;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ray3k.badforce2.Core;
import com.ray3k.badforce2.Utils;
import com.ray3k.badforce2.screens.GameScreen;
import dev.lyze.gdxUnBox2d.GameObject;
import dev.lyze.gdxUnBox2d.behaviours.BehaviourAdapter;

import static com.ray3k.badforce2.screens.GameScreen.*;

public class ImageBehaviour extends BehaviourAdapter {
    private TextureRegion textureRegion;
    private float x, y;

    public ImageBehaviour(String path, float x, float y, GameObject gameObject, float renderOrder) {
        super(gameObject);
        textureRegion = new TextureRegion(new Texture(Gdx.files.internal("levels/" + path), false));
        this.x = x - p2m(textureRegion.getRegionWidth()) / 2f;
        this.y = y - p2m(textureRegion.getRegionHeight()) / 2f;
        setRenderOrder(renderOrder);
    }

    @Override
    public void render(Batch batch) {
        batch.draw(textureRegion, x, y, p2m(textureRegion.getRegionWidth()), p2m(textureRegion.getRegionHeight()));
    }

    @Override
    public void onDestroy() {
        textureRegion.getTexture().dispose();
    }
}
