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


package com.io7m.tabla.cmdline.internal;

import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType.QConstant;
import com.io7m.quarrel.ext.logback.QLogback;
import com.io7m.tabla.core.TColumnWidthConstraint;
import com.io7m.tabla.core.TColumnWidthConstraintMaximumAny;
import com.io7m.tabla.core.TColumnWidthConstraintMinimumFitContent;
import com.io7m.tabla.core.TColumnWidthConstraintMinimumFitHeader;
import com.io7m.tabla.core.TTableBuilderType;
import com.io7m.tabla.core.TTableRendererType;
import com.io7m.tabla.core.TTableType;
import com.io7m.tabla.core.Tabla;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.io7m.tabla.core.TTableWidthConstraintType.withinRange;
import static java.lang.Boolean.FALSE;
import static org.apache.commons.csv.CSVFormat.DEFAULT;

/**
 * The "print-csv" command.
 */

public final class TCmdPrintCSV implements QCommandType
{
  /**
   * The renderer choice.
   */

  public enum Renderer
  {
    /**
     * CSV
     */

    CSV,

    /**
     * Pretty printed frames with Unicode characters.
     */

    FRAMED_UNICODE,

    /**
     * Pretty printed frames with ASCII characters.
     */

    FRAMED_ASCII
  }

  private static final QParameterNamed1<Path> INPUT =
    new QParameterNamed1<>(
      "--input",
      List.of(),
      new QConstant("The input CSV file."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Renderer> RENDERER =
    new QParameterNamed1<>(
      "--renderer",
      List.of(),
      new QConstant("The table renderer."),
      Optional.of(Renderer.FRAMED_UNICODE),
      Renderer.class
    );

  private static final QParameterNamed01<Integer> TABLE_MAX_WIDTH =
    new QParameterNamed01<>(
      "--table-max-width",
      List.of(),
      new QConstant("The maximum table width."),
      Optional.empty(),
      Integer.class
    );

  private static final QParameterNamed01<Integer> TABLE_MIN_WIDTH =
    new QParameterNamed01<>(
      "--table-min-width",
      List.of(),
      new QConstant("The minimum table width."),
      Optional.empty(),
      Integer.class
    );

  private static final QParameterNamed1<Boolean> COLUMN_FIT_CONTENT =
    new QParameterNamed1<>(
      "--column-fit-content",
      List.of(),
      new QConstant("Fit column widths to row content."),
      Optional.of(FALSE),
      Boolean.class
    );

  private final QCommandMetadata metadata;

  /**
   * The "print-csv" command.
   */

  public TCmdPrintCSV()
  {
    this.metadata =
      new QCommandMetadata(
        "print-csv",
        new QConstant("Print the contents of a CSV file as a table."),
        Optional.empty()
      );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return QLogback.plusParameters(
      List.of(
        INPUT,
        RENDERER,
        TABLE_MAX_WIDTH,
        TABLE_MIN_WIDTH,
        COLUMN_FIT_CONTENT
      )
    );
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType context)
    throws Exception
  {
    QLogback.configure(context);

    final TTableType table;
    final var inputFile =
      context.parameterValue(INPUT);

    try (var reader = Files.newBufferedReader(inputFile)) {
      try (var parser = DEFAULT.parse(reader)) {
        final var records = parser.getRecords();
        if (records.isEmpty()) {
          return QCommandStatus.SUCCESS;
        }

        final var firstRecord = records.get(0);
        final var builder = Tabla.builder();

        setTableConstraints(context, builder);

        final var constraint =
          createColumnConstraint(context);

        final var columnCount = firstRecord.size();
        for (var index = 0; index < columnCount; ++index) {
          builder.declareColumn(firstRecord.get(index), constraint);
        }

        for (var index = 1; index < records.size(); ++index) {
          final var record =
            records.get(index);
          final var recordSize =
            record.size();
          final var maxCount =
            Math.min(columnCount, recordSize);

          final var row =
            builder.addRow();
          for (var cellIndex = 0; cellIndex < maxCount; ++cellIndex) {
            row.addCell(record.get(cellIndex));
          }
          if (recordSize < columnCount) {
            for (var r = 0; r < columnCount - recordSize; ++r) {
              row.addCell("");
            }
          }
        }

        table = builder.build();
      }
    }

    final var renderer = createRenderer(context);
    final var lines = renderer.renderLines(table);
    final var output = context.output();

    for (final var line : lines) {
      output.println(line);
    }

    output.flush();
    return QCommandStatus.SUCCESS;
  }

  private static TTableRendererType createRenderer(
    final QCommandContextType context)
  {
    return switch (context.parameterValue(RENDERER)) {
      case CSV -> Tabla.csvRenderer();
      case FRAMED_UNICODE -> Tabla.framedUnicodeRenderer();
      case FRAMED_ASCII -> Tabla.framedASCIIRenderer();
    };
  }

  private static TColumnWidthConstraint createColumnConstraint(
    final QCommandContextType context)
  {
    final TColumnWidthConstraint constraint;
    if (context.parameterValue(COLUMN_FIT_CONTENT).booleanValue()) {
      constraint = new TColumnWidthConstraint(
        TColumnWidthConstraintMinimumFitContent.fitContent(),
        TColumnWidthConstraintMaximumAny.any()
      );
    } else {
      constraint = new TColumnWidthConstraint(
        TColumnWidthConstraintMinimumFitHeader.fitHeader(),
        TColumnWidthConstraintMaximumAny.any()
      );
    }
    return constraint;
  }

  private static void setTableConstraints(
    final QCommandContextType context,
    final TTableBuilderType builder)
  {
    final var maxWidth =
      context.parameterValue(TABLE_MAX_WIDTH);
    final var minWidth =
      context.parameterValue(TABLE_MIN_WIDTH);

    builder.setWidthConstraint(withinRange(minWidth, maxWidth));
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
  }
}
