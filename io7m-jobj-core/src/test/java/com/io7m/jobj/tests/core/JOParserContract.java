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

package com.io7m.jobj.tests.core;

import com.io7m.jlexing.core.LexicalPositionType;
import com.io7m.jobj.core.JOParserErrorCode;
import com.io7m.jobj.core.JOParserEventListenerType;
import com.io7m.jobj.core.JOParserType;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class JOParserContract
{
  protected abstract JOParserType getParser(
    String name,
    JOParserEventListenerType listener)
    throws FileNotFoundException;

  @Test public final void testEmpty()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("empty.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
  }

  @Test public final void testO()
    throws Exception
  {
    final AtomicBoolean o_called = new AtomicBoolean(false);
    final AtomicBoolean eof = new AtomicBoolean(false);
    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandO(
        final LexicalPositionType<Path> p,
        final String name)
      {
        Assert.assertEquals("Cube", name);
        o_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("o.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(o_called.get());
  }

  @Test public final void testOBad()
    throws Exception
  {
    final AtomicBoolean error_called = new AtomicBoolean(false);
    final AtomicBoolean eof = new AtomicBoolean(false);
    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("o_bad.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testComment()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean line_called = new AtomicBoolean(false);
    final AtomicBoolean comment_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }

      @Override public void onComment(
        final LexicalPositionType<Path> lex,
        final String text)
      {
        Assert.assertEquals(1L, (long) lex.getLine());
        Assert.assertEquals("# comment", text);
        comment_called.set(true);
      }

      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {
        Assert.assertEquals(1L, (long) lex.getLine());
        Assert.assertEquals("# comment", line);
        line_called.set(true);
      }
    };

    final JOParserType p = this.getParser("comment.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(comment_called.get());
    Assert.assertTrue(line_called.get());
  }

  @Test public final void testTri()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicInteger v_count = new AtomicInteger(0);
    final AtomicInteger vn_count = new AtomicInteger(0);
    final AtomicInteger vt_count = new AtomicInteger(0);
    final AtomicInteger f_count = new AtomicInteger(0);
    final AtomicInteger fv_count = new AtomicInteger(0);
    final AtomicBoolean f_finished = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_count.incrementAndGet();
      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {
        switch (v_count.get()) {
          case 0:
            Assert.assertEquals(1L, (long) index);
            Assert.assertEquals(0.0, x, 0.000001);
            Assert.assertEquals(1.0, y, 0.000001);
            Assert.assertEquals(0.0, z, 0.000001);
            break;
          case 1:
            Assert.assertEquals(2L, (long) index);
            Assert.assertEquals(0.0, x, 0.000001);
            Assert.assertEquals(0.0, y, 0.000001);
            Assert.assertEquals(0.0, z, 0.000001);
            break;
          case 2:
            Assert.assertEquals(3L, (long) index);
            Assert.assertEquals(1.0, x, 0.000001);
            Assert.assertEquals(0.0, y, 0.000001);
            Assert.assertEquals(0.0, z, 0.000001);
            break;
        }

        vt_count.incrementAndGet();
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {
        switch (v_count.get()) {
          case 0:
            Assert.assertEquals(1L, (long) index);
            Assert.assertEquals(0.0, x, 0.000001);
            Assert.assertEquals(1.0, y, 0.000001);
            Assert.assertEquals(0.0, z, 0.000001);
            Assert.assertEquals(1.0, w, 0.000001);
            break;
          case 1:
            Assert.assertEquals(2L, (long) index);
            Assert.assertEquals(0.0, x, 0.000001);
            Assert.assertEquals(0.0, y, 0.000001);
            Assert.assertEquals(0.0, z, 0.000001);
            Assert.assertEquals(1.0, w, 0.000001);
            break;
          case 2:
            Assert.assertEquals(3L, (long) index);
            Assert.assertEquals(1.0, x, 0.000001);
            Assert.assertEquals(0.0, y, 0.000001);
            Assert.assertEquals(0.0, z, 0.000001);
            Assert.assertEquals(1.0, w, 0.000001);
            break;
        }

        v_count.incrementAndGet();
      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {
        Assert.assertEquals(1L, (long) index);
        Assert.assertEquals(0.0, x, 0.000001);
        Assert.assertEquals(0.0, y, 0.000001);
        Assert.assertEquals(1.0, z, 0.000001);
        vn_count.incrementAndGet();
      }

      @Override public void onCommandFFinished(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_finished.set(true);
      }

      @Override public void onCommandFVertexV_VT_VN(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vt,
        final int vn)
      {
        switch (fv_count.get()) {
          case 0:
            Assert.assertEquals(1L, (long) index);
            Assert.assertEquals(1L, (long) v);
            Assert.assertEquals(1L, (long) vt);
            Assert.assertEquals(1L, (long) vn);
            break;
          case 1:
            Assert.assertEquals(1L, (long) index);
            Assert.assertEquals(2L, (long) v);
            Assert.assertEquals(1L, (long) vt);
            Assert.assertEquals(2L, (long) vn);
            break;
          case 2:
            Assert.assertEquals(1L, (long) index);
            Assert.assertEquals(3L, (long) v);
            Assert.assertEquals(1L, (long) vt);
            Assert.assertEquals(3L, (long) vn);
            break;
        }

        fv_count.incrementAndGet();
      }

      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }
    };

    final JOParserType p = this.getParser("tri.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertEquals(3L, (long) v_count.get());
    Assert.assertEquals(1L, (long) vn_count.get());
    Assert.assertEquals(3L, (long) vt_count.get());
    Assert.assertEquals(1L, (long) f_count.get());
    Assert.assertEquals(3L, (long) fv_count.get());
    Assert.assertTrue(f_finished.get());
  }

  @Test public final void testSlash()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean line_called = new AtomicBoolean(false);
    final AtomicBoolean v_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {
        Assert.assertEquals(1L, (long) index);
        Assert.assertEquals(0.0, x, 0.000001);
        Assert.assertEquals(0.0, y, 0.000001);
        Assert.assertEquals(0.0, z, 0.000001);
        Assert.assertEquals(1.0, w, 0.000001);
        v_called.set(true);
      }

      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {
        Assert.assertEquals(2L, (long) lex.getLine());
        Assert.assertEquals("v 0.0 0.0 0.0", line);
        line_called.set(true);
      }
    };

    final JOParserType p = this.getParser("slash.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(line_called.get());
    Assert.assertTrue(v_called.get());
  }

  @Test public final void testSlashCommented()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean line_called = new AtomicBoolean(false);
    final AtomicBoolean comment_called = new AtomicBoolean(false);
    final AtomicBoolean v_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }

      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {
        Assert.assertEquals(1L, (long) lex.getLine());
        Assert.assertEquals("v 0.0 0.0 0.0 # \\", line);
        line_called.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {
        Assert.assertEquals(1L, (long) index);
        Assert.assertEquals(0.0, x, 0.000001);
        Assert.assertEquals(0.0, y, 0.000001);
        Assert.assertEquals(0.0, z, 0.000001);
        Assert.assertEquals(1.0, w, 0.000001);
        v_called.set(true);
      }

      @Override public void onComment(
        final LexicalPositionType<Path> lex,
        final String text)
      {
        Assert.assertEquals(1L, (long) lex.getLine());
        Assert.assertEquals("# \\", text);
        comment_called.set(true);
      }
    };

    final JOParserType p = this.getParser("slash_commented.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(comment_called.get());
    Assert.assertTrue(line_called.get());
    Assert.assertTrue(v_called.get());
  }

  @Test public final void testSlashBad()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }

      @Override public void onFatalError(
        final LexicalPositionType<Path> lex,
        final Optional<Throwable> e,
        final String message)
      {
        Assert.assertEquals(2L, (long) lex.getLine());
        Assert.assertEquals(Optional.empty(), e);
        Assert.assertEquals("Unexpected EOF", message);
        error_called.set(true);
      }
    };

    final JOParserType p = this.getParser("slash_bad.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testVBad0()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("v_bad_0.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testVBad1()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("v_bad_1.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testVNBad0()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("vn_bad_0.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testVNBad1()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("vn_bad_1.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testVTBad0()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("vt_bad_0.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testVTBad1()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("vt_bad_1.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testNonsense()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(
          JOParserErrorCode.JOP_ERROR_UNRECOGNIZED_COMMAND,
          e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("nonsense.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_V_VT_VN_Bad0()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {

      }

      @Override public void onCommandFVertexV_VT_VN(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vt,
        final int vn)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vt_vn_bad0.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_V_VT_VN_Bad1()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {

      }

      @Override public void onCommandFVertexV_VT_VN(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vt,
        final int vn)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vt_vn_bad1.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_V_VT_VN_Bad2()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {

      }

      @Override public void onCommandFVertexV_VT_VN(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vt,
        final int vn)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vt_vn_bad2.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_V_VT_VN()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicInteger f_count = new AtomicInteger(0);
    final AtomicInteger fv_count = new AtomicInteger(0);
    final AtomicBoolean f_finished = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandFFinished(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_finished.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_count.incrementAndGet();
      }

      @Override public void onCommandFVertexV_VT_VN(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vt,
        final int vn)
      {
        Assert.assertEquals(1L, (long) index);
        Assert.assertEquals(1L, (long) v);
        Assert.assertEquals(1L, (long) vt);
        Assert.assertEquals(1L, (long) vn);
        fv_count.incrementAndGet();
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vt_vn.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertEquals(1L, (long) f_count.get());
    Assert.assertEquals(3L, (long) fv_count.get());
    Assert.assertTrue(f_finished.get());
  }

  @Test public final void testF_V_VT()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicInteger f_count = new AtomicInteger(0);
    final AtomicInteger fv_count = new AtomicInteger(0);
    final AtomicBoolean f_finished = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandFFinished(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_finished.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_count.incrementAndGet();
      }

      @Override public void onCommandFVertexV_VT(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vt)
      {
        Assert.assertEquals(1L, (long) index);
        Assert.assertEquals(1L, (long) v);
        Assert.assertEquals(1L, (long) vt);
        fv_count.incrementAndGet();
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vt.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertEquals(1L, (long) f_count.get());
    Assert.assertEquals(3L, (long) fv_count.get());
    Assert.assertTrue(f_finished.get());
  }

  @Test public final void testF_V_VT_Bad0()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {

      }

      @Override public void onCommandFVertexV_VT(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vt)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vt_bad0.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_V_Bad0()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {

      }

      @Override public void onCommandFVertexV(
        final LexicalPositionType<Path> p,
        final int index,
        final int v)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_bad0.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_V_VN_Bad0()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandVT(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {

      }

      @Override public void onCommandFVertexV_VN(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vn)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX, e);
        error_called.set(true);
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vn_bad0.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_V_VN()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicInteger f_count = new AtomicInteger(0);
    final AtomicInteger fv_count = new AtomicInteger(0);
    final AtomicBoolean f_finished = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandFFinished(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_finished.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandVN(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_count.incrementAndGet();
      }

      @Override public void onCommandFVertexV_VN(
        final LexicalPositionType<Path> p,
        final int index,
        final int v,
        final int vn)
      {
        Assert.assertEquals(1L, (long) index);
        Assert.assertEquals(1L, (long) v);
        Assert.assertEquals(1L, (long) vn);
        fv_count.incrementAndGet();
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v_vn.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertEquals(1L, (long) f_count.get());
    Assert.assertEquals(3L, (long) fv_count.get());
    Assert.assertTrue(f_finished.get());
  }

  @Test public final void testF_V()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicInteger f_count = new AtomicInteger(0);
    final AtomicInteger fv_count = new AtomicInteger(0);
    final AtomicBoolean f_finished = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onCommandFFinished(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_finished.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_count.incrementAndGet();
      }

      @Override public void onCommandFVertexV(
        final LexicalPositionType<Path> p,
        final int index,
        final int v)
      {
        Assert.assertEquals(1L, (long) index);
        Assert.assertEquals(1L, (long) v);
        fv_count.incrementAndGet();
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_v.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertEquals(1L, (long) f_count.get());
    Assert.assertEquals(3L, (long) fv_count.get());
    Assert.assertTrue(f_finished.get());
  }

  @Test public final void testF_Unknown()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicInteger f_count = new AtomicInteger(0);
    final AtomicInteger fv_count = new AtomicInteger(0);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(
          JOParserErrorCode.JOP_ERROR_BAD_VERTEX_SYNTAX,
          e);
        error_called.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onCommandFStarted(
        final LexicalPositionType<Path> p,
        final int index)
      {
        Assert.assertEquals(1L, (long) index);
        f_count.incrementAndGet();
      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_unknown.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertEquals(1L, (long) f_count.get());
    Assert.assertEquals(0L, (long) fv_count.get());
    Assert.assertTrue(error_called.get());
  }

  @Test public final void testF_TooFew()
    throws Exception
  {
    final AtomicBoolean eof = new AtomicBoolean(false);
    final AtomicInteger fv_count = new AtomicInteger(0);
    final AtomicBoolean error_called = new AtomicBoolean(false);

    final UnreachableListener ls = new UnreachableListener()
    {
      @Override public void onLine(
        final LexicalPositionType<Path> lex,
        final String line)
      {

      }

      @Override public void onError(
        final LexicalPositionType<Path> lex,
        final JOParserErrorCode e,
        final String message)
      {
        Assert.assertEquals(
          JOParserErrorCode.JOP_ERROR_BAD_COMMAND_SYNTAX,
          e);
        error_called.set(true);
      }

      @Override public void onCommandV(
        final LexicalPositionType<Path> p,
        final int index,
        final double x,
        final double y,
        final double z,
        final double w)
      {

      }

      @Override public void onEOF(final LexicalPositionType<Path> lex)
      {
        eof.set(true);
      }
    };

    final JOParserType p = this.getParser("f_too_few.obj", ls);
    p.run();

    Assert.assertTrue(eof.get());
    Assert.assertEquals(0L, (long) fv_count.get());
    Assert.assertTrue(error_called.get());
  }
}
