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

import com.io7m.jobj.core.JOParser;
import com.io7m.jobj.core.JOParserEventListenerType;
import com.io7m.jobj.core.JOParserType;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;

public final class JOParserTest extends JOParserContract
{
  @Override protected JOParserType getParser(
    final String name,
    final JOParserEventListenerType listener)
    throws FileNotFoundException
  {
    final InputStream stream =
      JOParserTest.class.getResourceAsStream(name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }

    return JOParser.newParserFromStream(
      Optional.of(Paths.get(name)), stream, listener);
  }
}
