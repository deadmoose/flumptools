package com.deadmoose.flumptools.flumpcli;

import java.io.File;

import com.google.common.base.Preconditions;

import org.kohsuke.args4j.Argument;

import playn.core.util.Callback;

import tripleplay.flump.JsonLoader;
import tripleplay.flump.Library;

public abstract class LibraryTool extends PlayNTool
{
    @Argument(required=true)
    public String libraryFile;

    @Override
    protected void execute ()
    {
        checkArguments();

        JsonLoader.loadLibrary(_lib.getParent(), new Callback<Library>() {
            @Override public void onSuccess (Library library) {
                execute(library);
                System.exit(0);
            }

            @Override public void onFailure (Throwable cause) {
                System.err.println("Could not load library: " + cause.getMessage());
                System.exit(-1);
            }
        });
    }

    protected abstract void execute (Library lib);

    protected void checkArguments ()
    {
        Preconditions.checkArgument(libraryFile.endsWith("library.json"),
            "Invalid library");

        _lib = new File(libraryFile);
        Preconditions.checkArgument(_lib.exists(), "Library does not exist");
        Preconditions.checkArgument(_lib.isFile(), "Library is not a file");

    }

    protected File _lib;
}
