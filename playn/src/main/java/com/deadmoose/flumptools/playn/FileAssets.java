package com.deadmoose.flumptools.playn;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.common.collect.Lists;

import playn.core.PlayN;
import playn.core.gl.Scale;
import playn.java.JavaAssets;
import playn.java.JavaPlatform;

public class FileAssets extends JavaAssets
{
    public FileAssets (JavaPlatform platform)
    {
        super(platform);
        setPathPrefix("");
    }

    @Override protected URL requireResource (String path)
        throws FileNotFoundException
    {
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
    }

    @Override protected Scale assetScale ()
    {
        return _scale;
    }

    protected Scale _scale = new Scale(1) {
        @Override public List<Scale.ScaledResource> getScaledResources (String path) {
            return Lists.newArrayList(new Scale.ScaledResource(this, path));
        }
    };
}
