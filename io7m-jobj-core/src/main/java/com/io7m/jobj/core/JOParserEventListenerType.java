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

import com.io7m.jlexing.core.LexicalPositionType;

import java.nio.file.Path;
import java.util.Optional;

/**
 * The type of parser event listeners.
 */

public interface JOParserEventListenerType
{
  /**
   * A fatal error has occurred. Parsing will cease when this method returns (if
   * it returns).
   *
   * @param p       The lexical position
   * @param e       The exception, if any
   * @param message The error message
   */

  void onFatalError(
    LexicalPositionType<Path> p,
    Optional<Throwable> e,
    String message);

  /**
   * A non-fatal error has occurred.
   *
   * @param p       The lexical position
   * @param e       The error code
   * @param message The error message
   */

  void onError(
    LexicalPositionType<Path> p,
    JOParserErrorCode e,
    String message);

  /**
   * The given line is about to be parsed.
   *
   * @param p    The lexical position
   * @param line The line
   */

  void onLine(
    LexicalPositionType<Path> p,
    String line);

  /**
   * EOF has been reached.
   *
   * @param p The lexical position
   */

  void onEOF(LexicalPositionType<Path> p);

  /**
   * A comment was encountered.
   *
   * @param p    The lexical position
   * @param text The comment text (including '#')
   */

  void onComment(
    LexicalPositionType<Path> p,
    String text);

  /**
   * A {@code usemtl} command was encountered.
   *
   * @param p    The lexical position
   * @param name The material name
   */

  void onCommandUsemtl(
    LexicalPositionType<Path> p,
    String name);

  /**
   * An {@code mtllib} command was encountered.
   *
   * @param p    The lexical position
   * @param name The material file name
   */

  void onCommandMtllib(
    LexicalPositionType<Path> p,
    String name);

  /**
   * An {@code o} command was encountered.
   *
   * @param p    The lexical position
   * @param name The object name
   */

  void onCommandO(
    LexicalPositionType<Path> p,
    String name);

  /**
   * An {@code s} command was encountered.
   *
   * @param p            The lexical position
   * @param group_number The group number
   */

  void onCommandS(
    LexicalPositionType<Path> p,
    int group_number);

  /**
   * A {@code v} command was encountered.
   *
   * @param p     The lexical position
   * @param index The index
   * @param x     The {@code X} value
   * @param y     The {@code Y} value
   * @param z     The {@code Z} value
   * @param w     The {@code W} value
   */

  void onCommandV(
    LexicalPositionType<Path> p,
    int index,
    double x,
    double y,
    double z,
    double w);

  /**
   * A {@code vn} command was encountered.
   *
   * @param p     The lexical position
   * @param index The index
   * @param x     The {@code X} value
   * @param y     The {@code Y} value
   * @param z     The {@code Z} value
   */

  void onCommandVN(
    LexicalPositionType<Path> p,
    int index,
    double x,
    double y,
    double z);

  /**
   * A {@code vt} command was encountered.
   *
   * @param p     The lexical position
   * @param index The index
   * @param x     The {@code X} value
   * @param y     The {@code Y} value
   * @param z     The {@code Z} value
   */

  void onCommandVT(
    LexicalPositionType<Path> p,
    int index,
    double x,
    double y,
    double z);

  /**
   * A {@code v/vt/vn} vertex was specified for an {@code f} command.
   *
   * @param p     The lexical position
   * @param index The index
   * @param v     The {@code v} value
   * @param vt    The {@code vt} value
   * @param vn    The {@code vn} value
   */

  void onCommandFVertexV_VT_VN(
    LexicalPositionType<Path> p,
    int index,
    int v,
    int vt,
    int vn);

  /**
   * A {@code v/vt/} vertex was specified for an {@code f} command.
   *
   * @param p     The lexical position
   * @param index The index
   * @param v     The {@code v} value
   * @param vt    The {@code vt} value
   */

  void onCommandFVertexV_VT(
    LexicalPositionType<Path> p,
    int index,
    int v,
    int vt);

  /**
   * A {@code v//vn} vertex was specified for an {@code f} command.
   *
   * @param p     The lexical position
   * @param index The index
   * @param v     The {@code v} value
   * @param vn    The {@code vn} value
   */

  void onCommandFVertexV_VN(
    LexicalPositionType<Path> p,
    int index,
    int v,
    int vn);

  /**
   * A {@code v//} vertex was specified for an {@code f} command.
   *
   * @param p     The lexical position
   * @param index The index
   * @param v     The {@code v} value
   */

  void onCommandFVertexV(
    LexicalPositionType<Path> p,
    int index,
    int v);

  /**
   * An {@code f} command was encountered. Vertices will be delivered via the
   * {@code FVertex*} methods, with {@link #onCommandFFinished
   * (LexicalPositionType, int)} signalling the end of the face. If any errors
   * are encountered during parsing of vertices, {@link #onError
   * (LexicalPositionType, JOParserErrorCode, String)} will be called instead of
   * {@link #onCommandFFinished(LexicalPositionType, int)}.
   *
   * @param p     The lexical position
   * @param index The index
   *
   * @see #onCommandFVertexV(LexicalPositionType, int, int)
   * @see #onCommandFVertexV_VN(LexicalPositionType, int, int, int)
   * @see #onCommandFVertexV_VT(LexicalPositionType, int, int, int)
   * @see #onCommandFVertexV_VT_VN(LexicalPositionType, int, int, int, int)
   * @see #onCommandFFinished(LexicalPositionType, int)
   */

  void onCommandFStarted(
    LexicalPositionType<Path> p,
    int index);

  /**
   * An {@code f} command was completed successfully.
   *
   * @param p     The lexical position
   * @param index The index
   */

  void onCommandFFinished(
    LexicalPositionType<Path> p,
    int index);
}
