package com.ray3k.badforce2.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader.PreloaderCallback;
import com.google.gwt.core.client.GWT;
import com.ray3k.badforce2.Core;

public class GwtLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
        cfg.padVertical = 0;
        cfg.padHorizontal = 0;
        return cfg;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new Core();
    }

    @Override
    public PreloaderCallback getPreloaderCallback() {
        return createPreloaderPanel(GWT.getHostPageBaseURL() + "preloadlogo.gif");
    }
}
