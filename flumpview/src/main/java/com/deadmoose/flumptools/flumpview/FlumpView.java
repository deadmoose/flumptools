package com.deadmoose.flumptools.flumpview;

import playn.core.Game;
import playn.core.PlayN;
import playn.core.util.Clock;

import playn.java.JavaPlatform;

public class FlumpView extends Game.Default
{
    public static final int UPDATE_RATE = 50;

    public static void main (String[] args)
    {
        JavaPlatform.Config config = new JavaPlatform.Config();
        JavaPlatform.register(config);
        PlayN.run(new FlumpView());
    }

    public FlumpView ()
    {
        super(UPDATE_RATE);
    }

    @Override public void init()
    {
    }

    @Override public void update (int delta)
    {
        _clock.update(delta);
    }

    @Override public void paint (float alpha)
    {
        _clock.paint(alpha);
    }

    protected final Clock.Source _clock = new Clock.Source(UPDATE_RATE);
}

