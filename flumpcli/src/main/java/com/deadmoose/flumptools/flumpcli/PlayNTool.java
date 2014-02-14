package com.deadmoose.flumptools.flumpcli;

import com.deadmoose.flumptools.playn.FileAssets;

import playn.core.PlayN;
import playn.java.JavaAssets;
import playn.java.JavaPlatform;

public abstract class PlayNTool
   implements Runnable
{
    public final void run ()
    {
        JavaPlatform.Config config = new JavaPlatform.Config();
        config.headless = true;

        JavaPlatform.register(config);

        JavaPlatform platform = new JavaPlatform(config) {
            @Override
            public void invokeLater (Runnable runnable) {
                // Just do it NOW
                runnable.run();
            }

            @Override
            public JavaAssets assets () {
                return _assets;
            }
            protected JavaAssets _assets = new FileAssets(this);
        };
        PlayN.setPlatform(platform);

        try {
            execute();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    protected abstract void execute ();
}
