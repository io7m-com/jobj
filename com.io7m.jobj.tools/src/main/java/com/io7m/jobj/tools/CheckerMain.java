/*
 * Copyright © 2015 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jobj.tools;

import com.io7m.jlexing.core.LexicalPositionType;
import com.io7m.jobj.core.JOParser;
import com.io7m.jobj.core.JOParserErrorCode;
import com.io7m.jobj.core.JOParserEventListenerType;
import com.io7m.jobj.core.JOParserType;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Command line {@code obj} file checker.
 */

public final class CheckerMain
{
  @Option(
    name = "--file",
    usage = "The file that will be checked",
    required = true)
  private Path file;

  @Option(
    name = "--help",
    usage = "Show help",
    help = true)
  private boolean help;

  private CheckerMain()
  {

  }

  /**
   * Main entry point.
   *
   * @param args Command line arguments
   */

  public static void main(final String[] args)
  {
    new CheckerMain().go(args);
  }

  private static void showHelp(final CmdLineParser parser)
  {
    System.err.println("checker: usage: [options...] [arguments...]");
    System.err.println();
    parser.printUsage(System.err);
    System.err.println();
    System.err.println("Version: io7m-jobj " + getVersion());
    System.err.println();
  }

  private static String getVersion()
  {
    final Package p = CheckerMain.class.getPackage();
    final String v = p.getImplementationVersion();
    if (v == null) {
      return "0.0.0";
    }
    return v;
  }

  private void go(final String[] args)
  {
    final CmdLineParser parser = new CmdLineParser(this);

    try {
      parser.parseArgument(args);

      if (this.help) {
        showHelp(parser);
        System.err.flush();
        System.exit(0);
      }

      final Listener ls = new Listener();

      try (InputStream s = Files.newInputStream(this.file)) {
        final JOParserType p =
          JOParser.newParserFromStream(Optional.of(this.file), s, ls);
        p.run();
      }

      if (ls.error_count > 0) {
        System.err.printf(
          "error: Encountered %d errors.\n",
          Integer.valueOf(ls.error_count));
        System.err.flush();
        System.exit(1);
      }

    } catch (final IOException e) {
      System.err.printf(
        "error: i/o error: %s: %s\n", e.getClass().getName(), e.getMessage());
      System.err.flush();
      System.exit(127);
    } catch (final CmdLineException e) {
      showHelp(parser);
      System.err.flush();
      System.exit(127);
    }
  }

  private static final class Listener implements JOParserEventListenerType
  {
    private final Map<Integer, String> lines;
    private int error_count;

    Listener()
    {
      this.error_count = 0;
      this.lines = new HashMap<>(8192);
    }

    private void errorShow(
      final LexicalPositionType<Path> p,
      final String message)
    {
      final String name = p.file().map(q -> q.toString() + ":").orElse("");
      System.err.printf(
        "error: %s%d:%d: %s\n",
        name,
        Integer.valueOf(p.line()),
        Integer.valueOf(p.column()),
        message);

      final String line = this.lines.get(Integer.valueOf(p.line()));
      if (line != null) {
        System.err.println(line);
        for (int index = 1; index < p.column(); ++index) {
          System.err.print(" ");
        }
        System.err.println("^");
      }
    }

    @Override
    public void onFatalError(
      final LexicalPositionType<Path> p,
      final Optional<Throwable> e,
      final String message)
    {

    }

    @Override
    public void onError(
      final LexicalPositionType<Path> p,
      final JOParserErrorCode e,
      final String message)
    {
      ++this.error_count;

      switch (e) {
        case JOP_ERROR_BAD_COMMAND_SYNTAX: {
          this.errorShow(p, "Bad command syntax: " + message);
          break;
        }
        case JOP_ERROR_BAD_VERTEX_SYNTAX: {
          this.errorShow(p, "Bad vertex syntax: " + message);
          break;
        }
        case JOP_ERROR_UNRECOGNIZED_COMMAND: {
          this.errorShow(p, "Unrecognized command: " + message);
          break;
        }
        case JOP_ERROR_NONEXISTENT_V: {
          this.errorShow(p, "Nonexistent v component: " + message);
          break;
        }
        case JOP_ERROR_NONEXISTENT_VT: {
          this.errorShow(p, "Nonexistent vt component: " + message);
          break;
        }
        case JOP_ERROR_NONEXISTENT_VN: {
          this.errorShow(p, "Nonexistent vn component: " + message);
          break;
        }
      }
    }

    @Override
    public void onLine(
      final LexicalPositionType<Path> p,
      final String line)
    {
      this.lines.put(Integer.valueOf(p.line()), line);
    }

    @Override
    public void onEOF(final LexicalPositionType<Path> p)
    {

    }

    @Override
    public void onComment(
      final LexicalPositionType<Path> p,
      final String text)
    {

    }

    @Override
    public void onCommandUsemtl(
      final LexicalPositionType<Path> p,
      final String name)
    {

    }

    @Override
    public void onCommandMtllib(
      final LexicalPositionType<Path> p,
      final String name)
    {

    }

    @Override
    public void onCommandO(
      final LexicalPositionType<Path> p,
      final String name)
    {

    }

    @Override
    public void onCommandS(
      final LexicalPositionType<Path> p,
      final int group_number)
    {

    }

    @Override
    public void onCommandV(
      final LexicalPositionType<Path> p,
      final int index,
      final double x,
      final double y,
      final double z,
      final double w)
    {

    }

    @Override
    public void onCommandVN(
      final LexicalPositionType<Path> p,
      final int index,
      final double x,
      final double y,
      final double z)
    {

    }

    @Override
    public void onCommandVT(
      final LexicalPositionType<Path> p,
      final int index,
      final double x,
      final double y,
      final double z)
    {

    }

    @Override
    public void onCommandFVertexV_VT_VN(
      final LexicalPositionType<Path> p,
      final int index,
      final int v,
      final int vt,
      final int vn)
    {

    }

    @Override
    public void onCommandFVertexV_VT(
      final LexicalPositionType<Path> p,
      final int index,
      final int v,
      final int vt)
    {

    }

    @Override
    public void onCommandFVertexV_VN(
      final LexicalPositionType<Path> p,
      final int index,
      final int v,
      final int vn)
    {

    }

    @Override
    public void onCommandFVertexV(
      final LexicalPositionType<Path> p,
      final int index,
      final int v)
    {

    }

    @Override
    public void onCommandFStarted(
      final LexicalPositionType<Path> p,
      final int index)
    {

    }

    @Override
    public void onCommandFFinished(
      final LexicalPositionType<Path> p,
      final int index)
    {

    }
  }
}
