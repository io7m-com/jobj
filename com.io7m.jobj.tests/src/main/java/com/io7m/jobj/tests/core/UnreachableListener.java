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

package com.io7m.jobj.tests.core;

import com.io7m.jlexing.core.LexicalPositionType;
import com.io7m.jobj.core.JOParserErrorCode;
import com.io7m.jobj.core.JOParserEventListenerType;

import java.nio.file.Path;
import java.util.Optional;

public class UnreachableListener implements JOParserEventListenerType
{
  UnreachableListener()
  {

  }

  @Override public void onFatalError(
    final LexicalPositionType<Path> lex,
    final Optional<Throwable> e,
    final String message)
  {
    throw new IllegalStateException();
  }

  @Override public void onError(
    final LexicalPositionType<Path> lex,
    final JOParserErrorCode e,
    final String message)
  {
    System.err.println(message);
    throw new IllegalStateException();
  }

  @Override public void onLine(
    final LexicalPositionType<Path> lex,
    final String line)
  {
    throw new IllegalStateException();
  }

  @Override public void onEOF(final LexicalPositionType<Path> lex)
  {
    throw new IllegalStateException();
  }

  @Override public void onComment(
    final LexicalPositionType<Path> lex,
    final String text)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandUsemtl(
    final LexicalPositionType<Path> p,
    final String name)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandMtllib(
    final LexicalPositionType<Path> p,
    final String name)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandO(
    final LexicalPositionType<Path> p,
    final String name)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandS(
    final LexicalPositionType<Path> p,
    final int group_number)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandV(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z,
    final double w)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandVN(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandVT(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandFVertexV_VT_VN(
    final LexicalPositionType<Path> p,
    final int index,
    final int v,
    final int vt,
    final int vn)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandFVertexV_VT(
    final LexicalPositionType<Path> p,
    final int index,
    final int v,
    final int vt)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandFVertexV_VN(
    final LexicalPositionType<Path> p,
    final int index,
    final int v,
    final int vn)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandFVertexV(
    final LexicalPositionType<Path> p,
    final int index,
    final int v)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandFStarted(
    final LexicalPositionType<Path> p,
    final int index)
  {
    throw new IllegalStateException();
  }

  @Override public void onCommandFFinished(
    final LexicalPositionType<Path> p,
    final int index)
  {
    throw new IllegalStateException();
  }
}
