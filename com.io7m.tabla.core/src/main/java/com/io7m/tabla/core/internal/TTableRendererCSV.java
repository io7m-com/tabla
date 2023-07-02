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

import com.io7m.tabla.core.TTableRendererType;
import com.io7m.tabla.core.TTableType;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

/**
 * A CSV table renderer.
 */

public final class TTableRendererCSV
  implements TTableRendererType
{
  private static final TTableRendererCSV INSTANCE =
    new TTableRendererCSV();

  private TTableRendererCSV()
  {

  }

  /**
   * @return A CSV table renderer.
   */

  public static TTableRendererType csv()
  {
    return INSTANCE;
  }

  @Override
  public List<String> renderLines(
    final TTableType table)
  {
    final var lineBuffer = new StringBuilder(128);
    final var maxColumns = table.columnCount();
    for (int index = 0; index < maxColumns; ++index) {
      final var column = table.columnOf(index);
      lineBuffer.append('"');
      lineBuffer.append(escapeCsv(column.headerContentRaw()));
      lineBuffer.append('"');
      if (index + 1 < maxColumns) {
        lineBuffer.append(',');
      }
    }

    final var maxRows = table.rowCount();
    final var output = new ArrayList<String>(maxRows);
    output.add(lineBuffer.toString());

    for (int index = 0; index < maxRows; ++index) {
      final var row = table.rowOf(index);
      lineBuffer.setLength(0);
      for (int cellIndex = 0; cellIndex < maxColumns; ++cellIndex) {
        lineBuffer.append('"');
        lineBuffer.append(escapeCsv(row.cellContentRaw(cellIndex)));
        lineBuffer.append('"');
        if (cellIndex + 1 < maxColumns) {
          lineBuffer.append(',');
        }
      }
      output.add(lineBuffer.toString());
    }
    return List.copyOf(output);
  }
}
