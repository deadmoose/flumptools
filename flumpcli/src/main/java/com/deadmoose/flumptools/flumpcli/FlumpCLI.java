package com.deadmoose.flumptools.flumpcli;

import java.io.PrintStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

public class FlumpCLI
{
    @Argument(required=true, handler=SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name=FlumpDump.NAME, impl=FlumpDump.class),
        @SubCommand(name=FlumpStrip.NAME, impl=FlumpStrip.class),
    })
    public Runnable cmd;

    @Option(name="--help", usage="Displays this help")
    public boolean help;

    public static void main (String[] args)
    {
        FlumpCLI cli = new FlumpCLI();
        CmdLineParser parser = new CmdLineParser(cli);

        try {
            parser.parseArgument(args);
        } catch (Exception e) {
            printUsage(parser, System.err);
            System.exit(-1);
        }

        if (cli.help) {
            if (cli.cmd != null) {
                parser = new CmdLineParser(cli.cmd);
            }
            printUsage(parser, System.out);
        } else {
            cli.cmd.run();
        }
    }

    public static void printUsage (CmdLineParser parser, PrintStream out)
    {
        parser.printSingleLineUsage(out);
        out.println();
        parser.printUsage(out);
    }
}
