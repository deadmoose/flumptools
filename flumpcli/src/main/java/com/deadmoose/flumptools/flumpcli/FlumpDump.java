package com.deadmoose.flumptools.flumpcli;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.kohsuke.args4j.Option;

import tripleplay.flump.Library;
import tripleplay.flump.Movie;
import tripleplay.flump.Symbol;
import tripleplay.flump.Texture;

/**
 * Prints the names of symbols in a Flump library.
 */
public class FlumpDump extends LibraryTool
{
    public static final String NAME = "dump";

    @Option(name="--show-hidden", usage="Show hidden symbols")
    public boolean showHidden = false;

    @Option(name="--movies", usage="Show only movies")
    public boolean showOnlyMovies = false;

    @Option(name="--textures", usage="Show only textures")
    public boolean showOnlyTextures = false;

    @Override
    protected void checkArguments ()
    {
        super.checkArguments();
        Preconditions.checkArgument(!(showOnlyMovies && showOnlyTextures),
            "Can only use one of --movies and --textures");
    }

    @Override
    protected void execute (Library lib)
    {
        Map<String, Symbol> symbols = lib.symbols;
        if (!showHidden) {
            symbols = Maps.filterKeys(symbols, new Predicate<String>() {
                @Override public boolean apply (String input) {
                    return !input.startsWith("~");
                }
            });
        }

        PrintStream out = System.out;

        if (!showOnlyTextures && !showOnlyMovies) {
            // Showing both, so put headers and indent
            out = new PrintStream(out) {
                @Override public void println (String str) {
                    print("    ");
                    super.println(str);
                }
            };
        }

        if (!showOnlyTextures) {
            if (!showOnlyMovies) {
                System.out.println("Movies:");
            }
            dumpSymbols(symbols, Predicates.instanceOf(Movie.Symbol.class), out);
        }

        if (!showOnlyMovies) {
            if (!showOnlyTextures) {
                System.out.println("Textures:");
            }
            dumpSymbols(symbols, Predicates.instanceOf(Texture.Symbol.class), out);
        }
    }

    protected void dumpSymbols (
        Map<String, Symbol> symbols, Predicate<Object> predicate, PrintStream out)
    {
        List<String> names = Lists.newArrayList(Maps.filterValues(symbols, predicate).keySet());

        Collections.sort(names);
        for (String name : names) {
            out.println(name);
        }
    }
}
