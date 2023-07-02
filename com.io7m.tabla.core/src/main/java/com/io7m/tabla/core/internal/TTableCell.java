/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.tabla.core.internal;

import com.io7m.jaffirm.core.Invariants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

final class TTableCell
{
  private static final Pattern WHITESPACE =
    Pattern.compile("\\s+");

  private final String contentRaw;
  private final List<String> contentFormatted;

  private TTableCell(
    final String inContentRaw,
    final List<String> inContentFormatted)
  {
    this.contentRaw = inContentRaw;
    this.contentFormatted = inContentFormatted;
  }

  public static TTableCell create(
    final int width,
    final String content)
  {
    if (width == 0) {
      return new TTableCell(content, List.of(""));
    }

    final var output =
      new ArrayList<String>();
    final var words =
      new LinkedList<>(List.of(WHITESPACE.split(content)));

    final var lineBuffer = new StringBuilder(width);

    while (!words.isEmpty()) {
      final var word = words.pollFirst();
      final var wordLength = word.length();
      final var remaining = width - lineBuffer.length();

      /*
       * Can this word fit on the current line?
       */

      if (wordLength > remaining) {

        /*
         * Would this word _ever_ be able to fit on the current line? If not,
         * then it must be hyphenated.
         */

        if (wordLength > width) {
          final var split = hyphenate(word, width);
          for (final var segment : split) {
            words.addLast(segment);
          }
          continue;
        }

        /*
         * No room remaining. Finish the current line and reinsert the
         * current word to be processed on the next iteration.
         */

        output.add(finishString(width, lineBuffer));
        lineBuffer.setLength(0);
        words.addFirst(word);
        continue;
      }

      final boolean spaceRequired = (wordLength < remaining);
      lineBuffer.append(word);
      if (spaceRequired) {
        lineBuffer.append(' ');
      }
    }

    if (!lineBuffer.isEmpty()) {
      output.add(finishString(width, lineBuffer));
    }

    return new TTableCell(
      content,
      List.copyOf(output)
    );
  }

  private static String finishString(
    final int width,
    final StringBuilder lineBuffer)
  {
    while (lineBuffer.length() < width) {
      lineBuffer.append(' ');
    }
    final var text = lineBuffer.toString();
    Invariants.checkInvariantV(
      text,
      text.length() == width,
      "Length of line '%s' must be %d (is %d)",
      text,
      Integer.valueOf(width),
      Integer.valueOf(text.length())
    );
    return text;
  }

  private static List<String> hyphenate(
    final String word,
    final int maxWidth)
  {
    /*
     * Break the string up into chunks of at most N-1 codepoints.
     */

    final var maxLengthMinusHyphen = maxWidth - 1;
    final var chunks = new LinkedList<List<Integer>>();
    final var currentChunk = new ArrayList<Integer>(maxWidth);
    for (final var codepoint : word.codePoints().boxed().toList()) {
      if (currentChunk.size() >= maxLengthMinusHyphen) {
        chunks.add(List.copyOf(currentChunk));
        currentChunk.clear();
      }
      currentChunk.add(codepoint);
    }
    chunks.add(List.copyOf(currentChunk));

    /*
     * Combine the codepoint chunks back into strings, adding a hyphen
     * at the end of each non-terminal string.
     */

    final var results = new ArrayList<String>();
    while (!chunks.isEmpty()) {
      final var chunk =
        chunks.pollFirst();
      final int[] codepointArray =
        chunk.stream()
          .mapToInt(Integer::intValue)
          .toArray();

      var text = new String(codepointArray, 0, codepointArray.length);
      if (!chunks.isEmpty()) {
        text = text + "-";
      }
      results.add(text);
    }
    return results;
  }

  public List<String> contentFormatted()
  {
    return this.contentFormatted;
  }

  public String contentRaw()
  {
    return this.contentRaw;
  }
}
