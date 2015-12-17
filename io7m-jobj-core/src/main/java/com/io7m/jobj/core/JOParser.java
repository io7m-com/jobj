/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
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

package com.io7m.jobj.core;

import com.io7m.jlexing.core.ImmutableLexicalPosition;
import com.io7m.jlexing.core.MutableLexicalPosition;
import com.io7m.jlexing.core.MutableLexicalPositionType;
import com.io7m.jnull.NullCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of the {@link JOParserType} interface.
 */

public final class JOParser implements JOParserType
{
  private static final Logger  LOG;
  private static final Pattern SPACE;
  private static final Pattern ALPHA;
  private static final Pattern P_FACE_V_VT_VN;
  private static final Pattern P_FACE_V_VT;
  private static final Pattern P_FACE_V_VN;
  private static final Pattern P_FACE_V;

  static {
    LOG = LoggerFactory.getLogger(JOParser.class);
    SPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    ALPHA = Pattern.compile("\\p{Alpha}+", Pattern.UNICODE_CHARACTER_CLASS);
    P_FACE_V_VT_VN =
      Pattern.compile(
        "(\\p{Digit}+)/(\\p{Digit}+)/(\\p{Digit}+)",
        Pattern.UNICODE_CHARACTER_CLASS);
    P_FACE_V_VT = Pattern.compile(
      "(\\p{Digit}+)/(\\p{Digit}+)/",
      Pattern.UNICODE_CHARACTER_CLASS);
    P_FACE_V_VN = Pattern.compile(
      "(\\p{Digit}+)//(\\p{Digit}+)",
      Pattern.UNICODE_CHARACTER_CLASS);
    P_FACE_V = Pattern.compile(
      "(\\p{Digit}+)//",
      Pattern.UNICODE_CHARACTER_CLASS);
  }

  private final BufferedReader                   reader;
  private final JOParserEventListenerType        listener;
  private final MutableLexicalPositionType<Path> lex;
  private       int                              n_current;
  private       int                              t_current;
  private       int                              v_current;
  private       int                              f_current;

  private JOParser(
    final Optional<Path> in_path,
    final BufferedReader in_reader,
    final JOParserEventListenerType in_listener)
  {
    this.reader = NullCheck.notNull(in_reader);
    this.lex = MutableLexicalPosition.newPosition(1, 0);
    this.lex.setFile(in_path);
    this.listener = NullCheck.notNull(in_listener);

    this.v_current = 1;
    this.n_current = 1;
    this.t_current = 1;
    this.f_current = 1;
  }

  /**
   * Create a parser.
   *
   * @param in_path The input filename, if any
   * @param stream  The input stream
   * @param ls      A parser listener
   *
   * @return A new parser
   */

  public static JOParserType newParserFromStream(
    final Optional<Path> in_path,
    final InputStream stream,
    final JOParserEventListenerType ls)
  {
    return new JOParser(
      in_path,
      new BufferedReader(new InputStreamReader(stream)),
      ls);
  }

  private static List<Token> getTokens(final String text)
  {
    final List<Token> tokens = new ArrayList<>(8);
    int index = 0;
    for (final String t : JOParser.SPACE.split(text)) {
      tokens.add(new Token(index, t));
      index += t.length() + 1;
    }
    return tokens;
  }

  private static double getDouble(final Token token)
    throws ParseException
  {
    try {
      return Double.parseDouble(token.getText());
    } catch (final NumberFormatException e) {
      throw new ParseException(e.getMessage(), token.getPosition());
    }
  }

  private static FaceType getFaceType(final Token token)
    throws ParseException
  {
    final String text = token.getText();
    if (JOParser.P_FACE_V_VT_VN.matcher(text).matches()) {
      return FaceType.FACE_V_VT_VN;
    }
    if (JOParser.P_FACE_V_VT.matcher(text).matches()) {
      return FaceType.FACE_V_VT;
    }
    if (JOParser.P_FACE_V_VN.matcher(text).matches()) {
      return FaceType.FACE_V_VN;
    }
    if (JOParser.P_FACE_V.matcher(text).matches()) {
      return FaceType.FACE_V;
    }

    throw new ParseException("Invalid vertex format", token.getPosition());
  }

  private String getLine()
    throws IOException
  {
    boolean slash = false;
    final StringBuilder buffer = new StringBuilder(256);

    while (true) {
      final String c_line = this.reader.readLine();
      if (c_line == null) {
        JOParser.LOG.trace("eof");
        if (slash) {
          this.listener.onFatalError(
            this.lex, Optional.empty(), "Unexpected EOF");
        }
        this.listener.onEOF(this.lex);
        return null;
      }

      if (c_line.endsWith("\\") && !c_line.contains("#")) {
        slash = true;
        buffer.append(c_line.substring(0, c_line.length() - 1));
        this.lex.setLine(this.lex.getLine() + 1);
        this.lex.setColumn(0);
        continue;
      }

      buffer.append(c_line);
      return buffer.toString();
    }
  }

  @Override public void run()
  {
    try {
      while (true) {
        final String c_line = this.getLine();
        if (c_line == null) {
          return;
        }

        final String c_trim = c_line.trim();
        JOParser.LOG.trace(
          "[{}]: {}",
          Integer.valueOf(this.lex.getLine()), c_trim);
        this.listener.onLine(this.lex, c_trim);

        final String c_actual;
        final String c_comment;
        final int c_index = c_trim.indexOf('#');
        if (c_index != -1) {
          c_actual = c_trim.substring(0, c_index);
          c_comment = c_trim.substring(c_index, c_trim.length());
        } else {
          c_actual = c_trim;
          c_comment = null;
        }

        this.onCommand(c_actual);

        if (c_comment != null) {
          this.listener.onComment(this.lex, c_comment);
        }
        this.lex.setLine(this.lex.getLine() + 1);
        this.lex.setColumn(0);
      }
    } catch (final IOException e) {
      this.listener.onFatalError(this.lex, Optional.of(e), e.getMessage());
    }
  }

  private void onCommand(final String text)
  {
    if (!text.isEmpty()) {
      final List<Token> tokens = JOParser.getTokens(text);
      final Token cmd = tokens.get(0);

      JOParser.LOG.trace(
        "[{}]: command: {}",
        Integer.valueOf(this.lex.getLine()), cmd.getText());
      switch (cmd.getText()) {
        case "v":
          this.onCommandV(tokens);
          return;
        case "vn":
          this.onCommandVN(tokens);
          return;
        case "vt":
          this.onCommandVT(tokens);
          return;
        case "f":
          this.onCommandF(tokens);
          return;
        case "o":
          this.onCommandO(tokens);
          return;
      }

      this.listener.onError(
        this.lex,
        JOParserErrorCode.JOP_ERROR_UNRECOGNIZED_COMMAND,
        cmd.getText());
    }
  }

  private void onCommandO(final List<Token> tokens)
  {
    if (tokens.size() == 2) {
      this.listener.onCommandO(this.lex, tokens.get(1).getText());
      return;
    }

    this.listener.onError(
      this.lex,
      JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
      "Syntax: 'o' <name>");
  }

  private void onCommandF(final List<Token> tokens)
  {
    try {
      if (tokens.size() >= 4) {
        this.listener.onCommandFStarted(this.lex, this.f_current);

        final FaceType ft;

        try {
          ft = JOParser.getFaceType(tokens.get(1));
        } catch (final ParseException e) {
          final StringBuilder sb = new StringBuilder(128);
          sb.append("Syntax:\n");
          sb.append("  <integer>/<integer>/<integer>\n");
          sb.append("| <integer>/<integer>/\n");
          sb.append("| <integer>//<integer>\n");
          sb.append("| <integer>//\n");
          this.listener.onError(
            this.lex,
            JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX,
            sb.toString());
          return;
        }

        try {
          for (int index = 1; index < tokens.size(); ++index) {
            final Token t = tokens.get(index);
            switch (ft) {
              case FACE_V_VT_VN:
                this.onCommandF_V_VT_VN(t);
                break;
              case FACE_V_VT:
                this.onCommandF_V_VT(t);
                break;
              case FACE_V_VN:
                this.onCommandF_V_VN(t);
                break;
              case FACE_V:
                this.onCommandF_V(t);
                break;
            }
          }
        } catch (final ParseException e) {
          final StringBuilder sb = new StringBuilder(128);
          sb.append("Syntax:\n");
          sb.append("  <integer>/<integer>/<integer>\n");
          sb.append("| <integer>/<integer>/\n");
          sb.append("| <integer>//<integer>\n");
          sb.append("| <integer>//\n");
          this.listener.onError(
            this.lex,
            JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX,
            sb.toString());
          return;
        }

        this.listener.onCommandFFinished(this.lex, this.f_current);
        return;
      }

      this.listener.onError(
        this.lex,
        JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
        "Syntax: 'f' <vertex> <vertex> <vertex> [<vertex> ...]");

    } finally {
      ++this.f_current;
    }
  }

  private void onCommandF_V(final Token t)
    throws ParseException
  {
    final Matcher m = JOParser.P_FACE_V.matcher(t.getText());
    if (m.matches()) {
      final String i0 = m.group(1);
      final int i0_val = Integer.parseInt(i0);
      this.listener.onCommandFVertexV(
        this.lex,
        this.f_current,
        i0_val);
    } else {
      throw new ParseException("Invalid vertex syntax", t.getPosition());
    }
  }

  private void onCommandF_V_VN(final Token t)
    throws ParseException
  {
    final Matcher m = JOParser.P_FACE_V_VN.matcher(t.getText());
    if (m.matches()) {
      final String i0 = m.group(1);
      final String i1 = m.group(2);
      final int i0_val = Integer.parseInt(i0);
      final int i1_val = Integer.parseInt(i1);
      this.listener.onCommandFVertexV_VN(
        this.lex,
        this.f_current,
        i0_val,
        i1_val);
    } else {
      throw new ParseException("Invalid vertex syntax", t.getPosition());
    }
  }

  private void onCommandF_V_VT(final Token t)
    throws ParseException
  {
    final Matcher m = JOParser.P_FACE_V_VT.matcher(t.getText());
    if (m.matches()) {
      final String i0 = m.group(1);
      final String i1 = m.group(2);

      final int i0_val = Integer.parseInt(i0);
      final int i1_val = Integer.parseInt(i1);

      this.listener.onCommandFVertexV_VT(
        this.lex,
        this.f_current,
        i0_val,
        i1_val);
    } else {
      throw new ParseException("Invalid vertex syntax", t.getPosition());
    }
  }

  private void onCommandF_V_VT_VN(final Token t)
    throws ParseException
  {
    final Matcher m = JOParser.P_FACE_V_VT_VN.matcher(t.getText());
    if (m.matches()) {
      final String i0 = m.group(1);
      final String i1 = m.group(2);
      final String i2 = m.group(3);

      final int i0_val = Integer.parseInt(i0);
      final int i1_val = Integer.parseInt(i1);
      final int i2_val = Integer.parseInt(i2);

      this.listener.onCommandFVertexV_VT_VN(
        this.lex,
        this.f_current,
        i0_val,
        i1_val,
        i2_val);
    } else {
      throw new ParseException("Invalid vertex syntax", t.getPosition());
    }
  }

  private void onCommandVT(final List<Token> tokens)
  {
    try {
      switch (tokens.size()) {
        case 2: {
          final double x = JOParser.getDouble(tokens.get(1));
          final double y = 0.0;
          final double z = 0.0;
          this.listener.onCommandVT(this.lex, this.t_current, x, y, z);
          return;
        }
        case 3: {
          final double x = JOParser.getDouble(tokens.get(1));
          final double y = JOParser.getDouble(tokens.get(2));
          final double z = 0.0;
          this.listener.onCommandVT(this.lex, this.t_current, x, y, z);
          return;
        }
        case 4: {
          final double x = JOParser.getDouble(tokens.get(1));
          final double y = JOParser.getDouble(tokens.get(2));
          final double z = JOParser.getDouble(tokens.get(3));
          this.listener.onCommandVT(this.lex, this.t_current, x, y, z);
          return;
        }
      }

      this.listener.onError(
        this.lex,
        JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
        "Syntax: 'vt' <float> [<float>] [<float>]");

    } catch (final ParseException e) {
      this.listener.onError(
        ImmutableLexicalPosition.newPosition(
          this.lex.getLine(), e.getErrorOffset()),
        JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
        e.getMessage());
    } finally {
      ++this.t_current;
    }
  }

  private void onCommandV(final List<Token> tokens)
  {
    try {
      switch (tokens.size()) {
        case 4: {
          final double x = JOParser.getDouble(tokens.get(1));
          final double y = JOParser.getDouble(tokens.get(2));
          final double z = JOParser.getDouble(tokens.get(3));
          final double w = 1.0;
          this.listener.onCommandV(this.lex, this.v_current, x, y, z, w);
          return;
        }
        case 5: {
          final double x = JOParser.getDouble(tokens.get(1));
          final double y = JOParser.getDouble(tokens.get(2));
          final double z = JOParser.getDouble(tokens.get(3));
          final double w = JOParser.getDouble(tokens.get(4));
          this.listener.onCommandV(this.lex, this.v_current, x, y, z, w);
          return;
        }
      }

      this.listener.onError(
        this.lex,
        JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
        "Syntax: 'v' <float> <float> <float> [<float>]");

    } catch (final ParseException e) {
      this.listener.onError(
        ImmutableLexicalPosition.newPosition(
          this.lex.getLine(), e.getErrorOffset()),
        JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
        e.getMessage());
    } finally {
      ++this.v_current;
    }
  }

  private void onCommandVN(final List<Token> tokens)
  {
    try {
      switch (tokens.size()) {
        case 4: {
          final double x = JOParser.getDouble(tokens.get(1));
          final double y = JOParser.getDouble(tokens.get(2));
          final double z = JOParser.getDouble(tokens.get(3));
          this.listener.onCommandVN(this.lex, this.n_current, x, y, z);
          return;
        }
      }

      this.listener.onError(
        this.lex,
        JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
        "Syntax: 'vn' <float> <float> <float>");

    } catch (final ParseException e) {
      this.listener.onError(
        ImmutableLexicalPosition.newPosition(
          this.lex.getLine(), e.getErrorOffset()),
        JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
        e.getMessage());
    } finally {
      ++this.n_current;
    }
  }

  private enum FaceType
  {
    FACE_V_VT_VN,
    FACE_V_VT,
    FACE_V_VN,
    FACE_V
  }

  private static final class Token
  {
    private final String text;
    private final int    position;

    Token(
      final int in_position,
      final String in_text)
    {
      this.position = in_position;
      this.text = in_text;
    }

    public int getPosition()
    {
      return this.position;
    }

    public String getText()
    {
      return this.text;
    }
  }
}
