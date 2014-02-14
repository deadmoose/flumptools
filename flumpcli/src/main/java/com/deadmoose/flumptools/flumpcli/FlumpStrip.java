package com.deadmoose.flumptools.flumpcli;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import org.kohsuke.args4j.Argument;

import tripleplay.flump.KeyframeData;
import tripleplay.flump.LayerData;
import tripleplay.flump.Library;
import tripleplay.flump.Movie;
import tripleplay.flump.Symbol;
import tripleplay.flump.Texture;

/**
 * Strips unused symbols from a Flump library.
 *
 * E.g. flump strip <library> <symbol> [symbols...]
 *
 * Loads the library, and throws out any symbols that are not either provided as arguments or
 * referenced by those symbols.
 */
public class FlumpStrip extends LibraryTool
{
    public static final String NAME = "strip";

    @Argument(index=1, required=true)
    public List<String> entryPoints;

    @Override
    protected void execute (Library lib)
    {
        Set<String> toKeep = Sets.newHashSet();

        Queue<String> queue = Queues.newConcurrentLinkedQueue(entryPoints);

        for (String entryPoint = queue.poll(); entryPoint != null; entryPoint = queue.poll()) {
            if (!toKeep.add(entryPoint)) {
                // We already know about it, so don't crawl down it again
                continue;
            }
            Symbol symbol = lib.symbols.get(entryPoint);
            Preconditions.checkNotNull(symbol, "Symbol '" + entryPoint + "' not in library.");

            if (symbol instanceof Texture.Symbol) {
                // Simple. Textures have no references, so we just keep it
            } else if (symbol instanceof Movie.Symbol) {
                // Need to crawl through and find any referenced Symbols
                for (LayerData layer : ((Movie.Symbol)symbol).layers) {
                    for (KeyframeData kf : layer.keyframes) {
                        Symbol ref = kf.symbol();
                        if (ref != null) {
                            queue.add(ref.name());
                        }
                    }
                }
            }
        }

        // TODO: Actually do the work instead of just printing the refs

        System.out.println("To keep:");
        for (String name : toKeep) {
            System.out.println("    " + name);
        }

        System.out.println("To strip:");
        for (String name :
            Iterables.filter(lib.symbols.keySet(), Predicates.not(Predicates.in(toKeep)))) {
            System.out.println("    " + name);
        }
    }
}
