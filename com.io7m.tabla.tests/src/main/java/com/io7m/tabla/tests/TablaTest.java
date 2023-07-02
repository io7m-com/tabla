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


package com.io7m.tabla.tests;

import com.io7m.tabla.core.TException;
import com.io7m.tabla.core.TTableType;
import com.io7m.tabla.core.Tabla;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.tabla.core.TColumnWidthConstraint.atLeastContent;
import static com.io7m.tabla.core.TColumnWidthConstraint.atLeastHeader;
import static com.io7m.tabla.core.TColumnWidthConstraint.exactWidth;
import static com.io7m.tabla.core.TTableWidthConstraintType.tableWidthAtMost;
import static com.io7m.tabla.core.TTableWidthConstraintType.tableWidthExact;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class TablaTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(TablaTest.class);

  /**
   * An empty table always has a zero size.
   */

  @Test
  public void testEmpty()
    throws Exception
  {
    final var table =
      Tabla.builder()
        .build();

    showTable(table);
    assertEquals(0, table.contentWidth());
    assertEquals(0, table.columnCount());
    assertEquals(0, table.rowCount());
  }

  /**
   * Without rowCount, an unconstrained table has a width equal to the sum of
   * the widths of the columnCount.
   */

  @Test
  public void testSimpleNoRows()
    throws Exception
  {
    final var table =
      Tabla.builder()
        .declareColumn("ID")
        .declareColumn("Name")
        .declareColumn("Description")
        .build();

    showTable(table);
    assertEquals(17, table.contentWidth());
    assertEquals(3, table.columnCount());
    assertEquals(0, table.rowCount());
  }

  /**
   * Without rowCount, a constrained table obeys its constraints and the column
   * widths sum to the constraint.
   */

  @Test
  public void testSimpleNoRowsConstrainedExact()
    throws Exception
  {
    final var table =
      Tabla.builder()
        .setWidthConstraint(tableWidthExact(80))
        .declareColumn("ID")
        .declareColumn("Name")
        .declareColumn("Description")
        .build();

    showTable(table);
    assertEquals(80, table.contentWidth());
    assertEquals(3, table.columnCount());
    assertEquals(0, table.rowCount());
  }

  /**
   * Without rowCount, a constrained table obeys its constraints and the column
   * widths sum to the constraint.
   */

  @Test
  public void testSimpleNoRowsConstrainedAtMost()
    throws Exception
  {
    final var table =
      Tabla.builder()
        .setWidthConstraint(tableWidthAtMost(40))
        .declareColumn("ID")
        .declareColumn("Name")
        .declareColumn("Description")
        .build();

    showTable(table);
    assertEquals(17, table.contentWidth());
    assertEquals(3, table.columnCount());
    assertEquals(0, table.rowCount());
  }

  /**
   * Table cells can be told to fit their content.
   */

  @Test
  public void testSimpleWithRowsAtLeastContent()
    throws Exception
  {
    final var builder =
      Tabla.builder()
        .declareColumn("ID", atLeastContent())
        .declareColumn("Name", atLeastContent())
        .declareColumn("Description", atLeastContent());

    builder.addRow()
      .addCell("af6b0d4f-383a-4d3a-807f-8cf53b64dfa8")
      .addCell("Battery")
      .addCell("A 9v battery.");

    builder.addRow()
      .addCell("abec6b1f-4fe7-4ddf-9c13-053575cd6a87")
      .addCell("HDMI 3m")
      .addCell("A 3m HDMI cable.");

    builder.addRow()
      .addCell("10542a71-bf3d-48d4-99e0-70f2fb5e4131")
      .addCell("Screen Cleaner")
      .addCell("A bottle of isopropyl alcohol.");

    final var table = builder.build();
    showTable(table);
    assertEquals(80, table.contentWidth());
    assertEquals(3, table.columnCount());
    assertEquals(3, table.rowCount());
  }

  /**
   * Table cells can be told to fit their headers.
   */

  @Test
  public void testSimpleWithRowsAtLeastHeader()
    throws Exception
  {
    final var builder =
      Tabla.builder()
        .declareColumn("ID", atLeastHeader())
        .declareColumn("Name", atLeastHeader())
        .declareColumn("Description", atLeastHeader());

    builder.addRow()
      .addCell("af6b0d4f-383a-4d3a-807f-8cf53b64dfa8")
      .addCell("Battery")
      .addCell("A 9v battery.");

    builder.addRow()
      .addCell("abec6b1f-4fe7-4ddf-9c13-053575cd6a87")
      .addCell("HDMI 3m")
      .addCell("A 3m HDMI cable.");

    builder.addRow()
      .addCell("10542a71-bf3d-48d4-99e0-70f2fb5e4131")
      .addCell("Screen Cleaner")
      .addCell("A bottle of isopropyl alcohol.");

    final var table = builder.build();
    showTable(table);
    assertEquals(17, table.contentWidth());
    assertEquals(3, table.columnCount());
    assertEquals(3, table.rowCount());
  }

  /**
   * Table cells can be told to fit their headers.
   */

  @Test
  public void testSimpleWithRowsAtLeastHeader2()
    throws Exception
  {
    final var builder =
      Tabla.builder()
        .declareColumn("Name", atLeastHeader())
        .declareColumn("Description", atLeastHeader());

    builder.addRow()
      .addCell("Battery")
      .addCell("A 9v battery.");

    builder.addRow()
      .addCell("HDMI 3m")
      .addCell("A 3m HDMI cable.");

    builder.addRow()
      .addCell("Screen Cleaner")
      .addCell("A bottle of isopropyl alcohol.");

    final var table = builder.build();
    showTable(table);
    assertEquals(15, table.contentWidth());
    assertEquals(2, table.columnCount());
    assertEquals(3, table.rowCount());
  }

  /**
   * Contradictory constraints cannot be solved.
   */

  @Test
  public void testErrorConstraint0()
    throws Exception
  {
    final var builder =
      Tabla.builder()
        .setWidthConstraint(tableWidthExact(80))
        .declareColumn("ID", exactWidth(20))
        .declareColumn("Name", exactWidth(20))
        .declareColumn("Description", exactWidth(20));

    final var ex =
      assertThrows(TException.class, builder::build);

    assertEquals("error-constraints", ex.errorCode());
    showException(ex);
  }

  /**
   * Table rows must have the right number of cells.
   */

  @Test
  public void testTooFewCellsForRow()
    throws Exception
  {
    final var builder =
      Tabla.builder()
        .declareColumn("Name", atLeastHeader())
        .declareColumn("Description", atLeastHeader());

    builder.addRow()
      .addCell("Battery")
      .addCell("A 9v battery.");

    final var row =
      builder.addRow()
        .addCell("HDMI 3m")
        .addCell("An HDMI cable.");

    final var ex =
      assertThrows(TException.class, () -> {
        row.addCell("Extra!");
      });

    assertEquals("error-too-many-cells", ex.errorCode());
    showException(ex);
  }

  /**
   * Table rows must have the right number of cells.
   */

  @Test
  public void testTooManyCellsForRow()
    throws Exception
  {
    final var builder =
      Tabla.builder()
        .declareColumn("Name", atLeastHeader())
        .declareColumn("Description", atLeastHeader());

    builder.addRow()
      .addCell("Battery")
      .addCell("A 9v battery.");

    builder.addRow()
      .addCell("HDMI 3m");

    final var ex =
      assertThrows(TException.class, builder::build);

    assertEquals("error-too-few-cells", ex.errorCode());
    showException(ex);
  }

  private static void showException(
    final TException ex)
  {
    for (final var e : ex.attributes().entrySet()) {
      LOG.debug("{}: {}", e.getKey(), e.getValue());
    }
  }

  private static void showTable(
    final TTableType table)
  {
    for (int columnIndex = 0; columnIndex < table.columnCount(); ++columnIndex) {
      final var column = table.columnOf(columnIndex);
      LOG.debug("Column [{}] Width {}", columnIndex, column.width());
    }

    {
      final var lines =
        Tabla.csvRenderer()
          .renderLines(table);

      for (final var line : lines) {
        LOG.debug("{}", line);
      }
    }

    {
      final var lines =
        Tabla.framedUnicodeRenderer()
          .renderLines(table);

      for (final var line : lines) {
        LOG.debug("{}", line);
      }
    }


    {
      final var lines =
        Tabla.framedASCIIRenderer()
          .renderLines(table);

      for (final var line : lines) {
        LOG.debug("{}", line);
      }
    }
  }
}
