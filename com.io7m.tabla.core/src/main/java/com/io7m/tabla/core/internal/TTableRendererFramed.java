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
import com.io7m.tabla.core.TTableRowType;
import com.io7m.tabla.core.TTableType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.CORNER_BOTTOM_LEFT;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.CORNER_BOTTOM_RIGHT;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.CORNER_TOP_LEFT;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.CORNER_TOP_RIGHT;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.EDGE_BOTTOM;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.EDGE_LEFT;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.EDGE_RIGHT;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.JUNCTION_CROSS;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.JUNCTION_LEFT_RIGHT_DOWN;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.JUNCTION_LEFT_RIGHT_UP;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.JUNCTION_UP_DOWN_LEFT;
import static com.io7m.tabla.core.internal.TTableRendererFramed.FramePieces.JUNCTION_UP_DOWN_RIGHT;

/**
 * A human-readable-computer-hostile table renderer.
 */

public final class TTableRendererFramed
  implements TTableRendererType
{
  private final Function<FramePieces, Character> charMap;
  private final StringBuilder lineBuffer;

  private TTableRendererFramed(
    final Function<FramePieces, Character> inCharMap)
  {
    this.charMap = Objects.requireNonNull(inCharMap, "charMap");
    this.lineBuffer = new StringBuilder(128);
  }

  /**
   * @return A unicode table renderer
   */

  public static TTableRendererType unicode()
  {
    return new TTableRendererFramed(TTableRendererFramed::frameUnicode);
  }

  /**
   * @return An ASCII table renderer
   */

  public static TTableRendererType ascii()
  {
    return new TTableRendererFramed(TTableRendererFramed::frameAscii);
  }

  enum FramePieces
  {
    CORNER_BOTTOM_LEFT,
    CORNER_BOTTOM_RIGHT,
    CORNER_TOP_LEFT,
    CORNER_TOP_RIGHT,
    EDGE_LEFT,
    EDGE_RIGHT,
    EDGE_BOTTOM,
    EDGE_TOP,
    JUNCTION_CROSS,
    JUNCTION_UP_DOWN_RIGHT,
    JUNCTION_UP_DOWN_LEFT,
    JUNCTION_LEFT_RIGHT_UP,
    JUNCTION_LEFT_RIGHT_DOWN
  }

  private static Character frameUnicode(
    final FramePieces p)
  {
    return switch (p) {
      case CORNER_BOTTOM_LEFT -> Character.valueOf('└');
      case CORNER_BOTTOM_RIGHT -> Character.valueOf('┘');
      case CORNER_TOP_LEFT -> Character.valueOf('┌');
      case CORNER_TOP_RIGHT -> Character.valueOf('┐');
      case EDGE_LEFT -> Character.valueOf('│');
      case EDGE_RIGHT -> Character.valueOf('│');
      case EDGE_BOTTOM -> Character.valueOf('─');
      case EDGE_TOP -> Character.valueOf('─');
      case JUNCTION_CROSS -> Character.valueOf('┼');
      case JUNCTION_UP_DOWN_RIGHT -> Character.valueOf('├');
      case JUNCTION_UP_DOWN_LEFT -> Character.valueOf('┤');
      case JUNCTION_LEFT_RIGHT_UP -> Character.valueOf('┴');
      case JUNCTION_LEFT_RIGHT_DOWN -> Character.valueOf('┬');
    };
  }

  private static Character frameAscii(
    final FramePieces p)
  {
    return switch (p) {
      case CORNER_BOTTOM_LEFT -> Character.valueOf('+');
      case CORNER_BOTTOM_RIGHT -> Character.valueOf('+');
      case CORNER_TOP_LEFT -> Character.valueOf('+');
      case CORNER_TOP_RIGHT -> Character.valueOf('+');
      case EDGE_LEFT -> Character.valueOf('|');
      case EDGE_RIGHT -> Character.valueOf('|');
      case EDGE_BOTTOM -> Character.valueOf('-');
      case EDGE_TOP -> Character.valueOf('-');
      case JUNCTION_CROSS -> Character.valueOf('+');
      case JUNCTION_UP_DOWN_RIGHT -> Character.valueOf('+');
      case JUNCTION_UP_DOWN_LEFT -> Character.valueOf('+');
      case JUNCTION_LEFT_RIGHT_UP -> Character.valueOf('+');
      case JUNCTION_LEFT_RIGHT_DOWN -> Character.valueOf('+');
    };
  }

  private char edge(
    final FramePieces p)
  {
    return this.charMap.apply(p).charValue();
  }

  @Override
  public List<String> renderLines(
    final TTableType table)
  {
    if (table.columnCount() == 0) {
      return List.of();
    }

    final var output = new ArrayList<String>();
    final var columnBoundaries = new HashSet<Integer>();
    var framedWidth = 0;
    for (int index = 0; index < table.columnCount(); ++index) {
      columnBoundaries.add(Integer.valueOf(framedWidth));
      // One character for the frame
      framedWidth += 1;
      // One character of padding
      framedWidth += 1;
      framedWidth += table.columnOf(index).width();
      // One character of padding
      framedWidth += 1;
    }
    // One character for the closing frame
    framedWidth += 1;

    output.add(this.renderTableFrameTop(columnBoundaries, framedWidth));
    output.add(this.renderTableHeader(table));

    if (table.rowCount() > 0) {
      output.add(this.renderTableFrameMiddle(columnBoundaries, framedWidth));
    } else {
      output.add(this.renderTableFrameBottom(columnBoundaries, framedWidth));
      return List.copyOf(output);
    }

    this.renderTableMainRows(table, output);
    output.add(this.renderTableFrameBottom(columnBoundaries, framedWidth));
    return List.copyOf(output);
  }

  private String renderTableHeader(
    final TTableType table)
  {
    this.lineBuffer.setLength(0);
    for (int index = 0; index < table.columnCount(); ++index) {
      final var column = table.columnOf(index);
      this.lineBuffer.append(this.edge(EDGE_LEFT));
      this.lineBuffer.append(' ');
      this.lineBuffer.append(column.headerContentFormatted());
      this.lineBuffer.append(' ');
    }
    this.lineBuffer.append(this.edge(EDGE_RIGHT));
    return this.lineBuffer.toString();
  }

  private void renderTableMainRows(
    final TTableType table,
    final ArrayList<String> output)
  {
    for (int rowIndex = 0; rowIndex < table.rowCount(); ++rowIndex) {
      final var row = table.rowOf(rowIndex);
      final var rowHeight = row.height();
      for (int rowLine = 0; rowLine < rowHeight; ++rowLine) {
        output.add(this.renderTableMainRowsOneLine(table, row, rowLine));
      }
      if (rowIndex + 1 < table.rowCount()) {
        output.add(this.renderTableMainRowsDivider(table, row));
      }
    }
  }

  private String renderTableMainRowsDivider(
    final TTableType table,
    final TTableRowType row)
  {
    this.lineBuffer.setLength(0);
    for (int cellIndex = 0; cellIndex < table.columnCount(); ++cellIndex) {
      if (cellIndex == 0) {
        this.lineBuffer.append(this.edge(JUNCTION_UP_DOWN_RIGHT));
      } else {
        this.lineBuffer.append(this.edge(JUNCTION_CROSS));
      }

      this.lineBuffer.append(this.edge(EDGE_BOTTOM));
      final var cellWidth = table.columnOf(cellIndex).width();
      for (int x = 0; x < cellWidth; ++x) {
        this.lineBuffer.append(this.edge(EDGE_BOTTOM));
      }
      this.lineBuffer.append(this.edge(EDGE_BOTTOM));
    }
    this.lineBuffer.append(this.edge(JUNCTION_UP_DOWN_LEFT));
    return this.lineBuffer.toString();
  }

  private String renderTableMainRowsOneLine(
    final TTableType table,
    final TTableRowType row,
    final int rowLine)
  {
    this.lineBuffer.setLength(0);
    for (int cellIndex = 0; cellIndex < table.columnCount(); ++cellIndex) {
      this.lineBuffer.append(this.edge(EDGE_LEFT));
      this.lineBuffer.append(' ');

      final var cellWidth =
        table.columnOf(cellIndex)
          .width();

      final var cellContentLines =
        row.cellContentFormatted(cellIndex);

      if (rowLine >= cellContentLines.size()) {
        this.lineBuffer.append(" ".repeat(cellWidth));
      } else {
        final var text = cellContentLines.get(rowLine);
        this.lineBuffer.append(text);
        final var pad = cellWidth - text.length();
        this.lineBuffer.append(" ".repeat(pad));
      }

      this.lineBuffer.append(' ');
    }
    this.lineBuffer.append(this.edge(EDGE_RIGHT));
    return this.lineBuffer.toString();
  }

  private String renderTableFrameTop(
    final HashSet<Integer> columnBoundaries,
    final int framedWidth)
  {
    this.lineBuffer.setLength(0);
    for (int index = 0; index < framedWidth; ++index) {
      if (index == 0) {
        this.lineBuffer.append(this.edge(CORNER_TOP_LEFT));
        continue;
      }
      if (columnBoundaries.contains(Integer.valueOf(index))) {
        this.lineBuffer.append(this.edge(JUNCTION_LEFT_RIGHT_DOWN));
        continue;
      }
      if (index + 1 == framedWidth) {
        this.lineBuffer.append(this.edge(CORNER_TOP_RIGHT));
        continue;
      }
      this.lineBuffer.append(this.edge(EDGE_BOTTOM));
    }
    return this.lineBuffer.toString();
  }

  private String renderTableFrameMiddle(
    final HashSet<Integer> columnBoundaries,
    final int framedWidth)
  {
    this.lineBuffer.setLength(0);
    for (int index = 0; index < framedWidth; ++index) {
      if (index == 0) {
        this.lineBuffer.append(this.edge(JUNCTION_UP_DOWN_RIGHT));
        continue;
      }
      if (columnBoundaries.contains(Integer.valueOf(index))) {
        this.lineBuffer.append(this.edge(JUNCTION_CROSS));
        continue;
      }
      if (index + 1 == framedWidth) {
        this.lineBuffer.append(this.edge(JUNCTION_UP_DOWN_LEFT));
        continue;
      }
      this.lineBuffer.append(this.edge(EDGE_BOTTOM));
    }
    return this.lineBuffer.toString();
  }

  private String renderTableFrameBottom(
    final HashSet<Integer> columnBoundaries,
    final int framedWidth)
  {
    this.lineBuffer.setLength(0);
    for (int index = 0; index < framedWidth; ++index) {
      if (index == 0) {
        this.lineBuffer.append(this.edge(CORNER_BOTTOM_LEFT));
        continue;
      }
      if (columnBoundaries.contains(Integer.valueOf(index))) {
        this.lineBuffer.append(this.edge(JUNCTION_LEFT_RIGHT_UP));
        continue;
      }
      if (index + 1 == framedWidth) {
        this.lineBuffer.append(this.edge(CORNER_BOTTOM_RIGHT));
        continue;
      }
      this.lineBuffer.append(this.edge(EDGE_BOTTOM));
    }
    return this.lineBuffer.toString();
  }
}
