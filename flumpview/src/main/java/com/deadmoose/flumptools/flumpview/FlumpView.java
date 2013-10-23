package com.deadmoose.flumptools.flumpview;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.common.collect.Lists;

import playn.core.Game;
import playn.core.PlayN;
import playn.core.gl.Scale;
import playn.core.util.Clock;
import playn.java.JavaAssets;
import playn.java.JavaPlatform;

import tripleplay.game.ScreenStack;

public class FlumpView extends Game.Default
{
    public static final int UPDATE_RATE = 50;

    public static void main (String[] args)
    {
        JavaPlatform.Config config = new JavaPlatform.Config();
        config.width = 1280;
        config.height = 1024;
        JavaPlatform.register(config);

        JavaPlatform platform = new JavaPlatform(config) {
            @Override public JavaAssets assets () {
                return _assets;
            }
            protected final JavaAssets _assets = new JavaAssets(this) {
                @Override protected URL requireResource (String path) throws FileNotFoundException {
                    if (path.startsWith("/")) {
                        URL url = null;
                        try {
                            url = new File(path).toURI().toURL();
                        } catch (MalformedURLException mue) {
                            PlayN.log().error("Error loading file: " + path, mue);
                        }

                        if (url == null) {
                            throw new FileNotFoundException(path);
                        }

                        return url;
                    } else {
                        return super.requireResource(path);
                    }
                }

                @Override protected Scale assetScale () {
                    return _scale;
                }

                protected Scale _scale = new Scale(1) {
                    @Override public List<Scale.ScaledResource> getScaledResources(String path) {
                        return Lists.newArrayList(new Scale.ScaledResource(this, path));

                    }
                };

                {
                    setPathPrefix("");
                    setTitle("Flumpview");
                }
            };
        };
        PlayN.setPlatform(platform);
        PlayN.run(new FlumpView());
    }

    public FlumpView ()
    {
        super(UPDATE_RATE);
    }

    @Override public void init()
    {
        _screens.push(new FlumpViewScreen(_screens));
    }

    @Override public void update (int delta)
    {
        _clock.update(delta);
        _screens.update(delta);
    }

    @Override public void paint (float alpha)
    {
        _clock.paint(alpha);
        _screens.paint(_clock);
    }

    protected final Clock.Source _clock = new Clock.Source(UPDATE_RATE);
    protected final ScreenStack _screens = new ScreenStack() {
        @Override protected void handleError (RuntimeException error) {
            PlayN.log().warn("Screen failure", error);
        }
        @Override protected Transition defaultPushTransition () {
            return slide();
        }
        @Override protected Transition defaultPopTransition () {
            return slide().right();
        }
    };
}

