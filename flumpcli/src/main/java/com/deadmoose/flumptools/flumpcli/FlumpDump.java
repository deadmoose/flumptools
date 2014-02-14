package com.deadmoose.flumptools.flumpcli;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import playn.core.util.Callback;

import tripleplay.flump.JsonLoader;
import tripleplay.flump.Library;
import tripleplay.flump.Movie;
import tripleplay.flump.Symbol;
import tripleplay.flump.Texture;

public class FlumpDump extends PlayNTool
{
    public static final String NAME = "dump";

    @Argument(required=true)
    public String libraryFile;

    @Option(name="--show-hidden", usage="Show hidden symbols")
    public boolean showHidden = false;

    @Option(name="--movies", usage="Show only movies")
    public boolean showOnlyMovies = false;

    @Option(name="--textures", usage="Show only textures")
    public boolean showOnlyTextures = false;

    @Override
    protected void execute ()
    {
        File lib = new File(libraryFile);

        Preconditions.checkArgument(libraryFile.endsWith("library.json"),
            "Invalid library");
        Preconditions.checkArgument(lib.exists(), "Library does not exist");
        Preconditions.checkArgument(lib.isFile(), "Library is not a file");
        Preconditions.checkArgument(!(showOnlyMovies && showOnlyTextures),
            "Can only use one of --movies and --textures");

        JsonLoader.loadLibrary(lib.getParent(), new Callback<Library>() {
            @Override
            public void onSuccess (Library library) {
                dumpLibrary(library);
                System.exit(0);
            }

            @Override public void onFailure (Throwable cause) {
                System.err.println("Could not load library: " + cause.getMessage());
                System.exit(-1);
            }
        });
    }

    protected void dumpLibrary (Library lib)
    {
        Map<String, Symbol> symbols = lib.symbols;
        if (!showHidden) {
            symbols = Maps.filterKeys(symbols, new Predicate<String>() {
                @Override public boolean apply (String input) {
                    return !input.startsWith("~");
                }
            });
        }

        if (!showOnlyTextures) {
            dumpSymbols(symbols, Predicates.instanceOf(Movie.Symbol.class));
        }

        if (!showOnlyMovies) {
            dumpSymbols(symbols, Predicates.instanceOf(Texture.Symbol.class));
        }

    }

    protected void dumpSymbols (Map<String, Symbol> symbols, Predicate<Object> predicate)
    {
        List<String> names = Lists.newArrayList(Maps.filterValues(symbols, predicate).keySet());

        Collections.sort(names);
        for (String name : names) {
            System.out.println(name);
        }
    }
}
