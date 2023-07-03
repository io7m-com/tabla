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

import com.io7m.tabla.core.TColumnWidthConstraint;
import com.io7m.tabla.core.TException;
import com.io7m.tabla.core.TTableBuilderType;
import com.io7m.tabla.core.TTableRowBuilderType;
import com.io7m.tabla.core.TTableType;
import com.io7m.tabla.core.TTableWidthConstraintAny;
import com.io7m.tabla.core.TTableWidthConstraintRange;
import com.io7m.tabla.core.TTableWidthConstraintType;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import static org.chocosolver.solver.variables.IntVar.MAX_INT_BOUND;

/**
 * The main table functionality.
 */

public final class TTables
{
  private TTables()
  {

  }

  /**
   * Create a new mutable table builder.
   *
   * @return The builder
   */

  public static TTableBuilderType builder()
  {
    return new TTableBuilder();
  }

  private static final class TTableBuilder
    implements TTableBuilderType
  {
    private TTableWidthConstraintType widthConstraint;
    private final ArrayList<TTableColumnDeclaration> columns;
    private final ArrayList<TTableRowBuilder> rows;
    private final Model model;

    private TTableBuilder()
    {
      this.model =
        new Model();
      this.widthConstraint =
        TTableWidthConstraintType.any();
      this.rows =
        new ArrayList<>();
      this.columns =
        new ArrayList<>();
    }

    @Override
    public TTableBuilderType declareColumn(
      final String name,
      final TColumnWidthConstraint constraint)
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(constraint, "constraint");

      final var index = this.columns.size();
      this.columns.add(
        new TTableColumnDeclaration(this.model, index, name, constraint)
      );
      return this;
    }

    @Override
    public TTableType build()
      throws TException
    {
      final IntVar tableWidthVar =
        this.createTableWidthVariable();

      final var columnCount =
        this.columns.size();
      final IntVar[] columnVars =
        new IntVar[columnCount];

      for (int index = 0; index < columnVars.length; ++index) {
        columnVars[index] = this.columns.get(index).createModelVariable();
      }

      final var tableColumnSumVar =
        this.model.sum("TableColumnWidthsSum", columnVars);

      final var tableWidthConstraint =
        this.model.arithm(tableColumnSumVar, "=", tableWidthVar);

      tableWidthConstraint.setName("TableColumnWidthsSum = TableWidth");
      switch (this.widthConstraint.hardness()) {
        case SOFT_CONSTRAINT -> tableWidthConstraint.reify();
        case HARD_CONSTRAINT -> tableWidthConstraint.post();
      }

      this.solveConstraints();

      final var columnResults =
        new ArrayList<TTableColumn>(columnCount);
      for (int columnIndex = 0; columnIndex < columnVars.length; ++columnIndex) {
        columnResults.add(
          new TTableColumn(
            this.columns.get(columnIndex).name(),
            columnVars[columnIndex].getValue()
          )
        );
      }

      final var rowResults =
        new ArrayList<TTableRow>(this.rows.size());

      for (int rowIndex = 0; rowIndex < this.rows.size(); ++rowIndex) {
        final var row =
          this.rows.get(rowIndex);
        final var cells =
          new ArrayList<TTableCell>(columnCount);

        if (row.cells.size() != columnCount) {
          throw errorTooFewCells(columnCount, rowIndex, row);
        }

        for (int cellIndex = 0; cellIndex < columnCount; ++cellIndex) {
          cells.add(
            TTableCell.create(
              columnVars[cellIndex].getValue(),
              row.cells.get(cellIndex)
            )
          );
        }
        rowResults.add(new TTableRow(cells));
      }

      return new TTable(
        List.copyOf(rowResults),
        List.copyOf(columnResults)
      );
    }

    private static TException errorTooFewCells(
      final int columnCount,
      final int rowIndex,
      final TTableRowBuilder row)
    {
      final var attributes = new TreeMap<String, String>();
      attributes.put("Row Index", Integer.toString(rowIndex));
      attributes.put("Expected Count", Integer.toString(columnCount));
      attributes.put("Received Count", Integer.toString(row.cells.size()));

      return new TException(
        "Too few cells in row.",
        "error-too-few-cells",
        attributes,
        Optional.empty()
      );
    }

    private void solveConstraints()
      throws TException
    {
      final var solver =
        this.model.getSolver();
      final var solved =
        solver.solve();

      if (!solved) {
        throw this.errorSolveFailed();
      }
    }

    private TException errorSolveFailed()
    {
      final var attributes = new TreeMap<String, String>();
      for (final var v : this.model.getVars()) {
        if (v.getName().startsWith("cste")) {
          continue;
        }

        if (v instanceof final IntVar iv) {
          final String chosen;
          if (iv.isInstantiated()) {
            chosen = Integer.toString(iv.getValue());
          } else {
            chosen = "<indeterminate>";
          }

          final var value =
            String.format(
              "Allowed Range [%d, %d], Chosen Value (%s)",
              Integer.valueOf(iv.getLB()),
              Integer.valueOf(iv.getUB()),
              chosen
            );
          attributes.put("Variable[%s]".formatted(v.getName()), value);
        }
      }

      for (final var c : this.model.getCstrs()) {
        attributes.put(
          "Constraint[%s]".formatted(c.getName()),
          c.isSatisfied().toString()
        );
      }

      return new TException(
        "Unable to solve table constraints.",
        "error-constraints",
        attributes,
        Optional.empty()
      );
    }

    @Override
    public TTableBuilderType setWidthConstraint(
      final TTableWidthConstraintType constraint)
    {
      this.widthConstraint =
        Objects.requireNonNull(constraint, "constraint");
      return this;
    }

    @Override
    public TTableRowBuilderType addRow()
    {
      final var rowBuilder = new TTableRowBuilder(this);
      this.rows.add(rowBuilder);
      return rowBuilder;
    }

    private IntVar createTableWidthVariable()
    {
      if (this.widthConstraint instanceof final TTableWidthConstraintAny any) {
        return this.model.intVar(
          "TableWidth",
          0,
          MAX_INT_BOUND,
          true
        );
      }

      if (this.widthConstraint instanceof final TTableWidthConstraintRange ranged) {
        return this.model.intVar(
          "TableWidth",
          ranged.minimumSize(),
          ranged.maximumSize()
        );
      }

      throw new IllegalStateException();
    }

    public void notifyColumnContentLength(
      final int index,
      final int length)
    {
      this.columns.get(index).notifyContentLength(length);
    }
  }

  private static final class TTableRowBuilder implements TTableRowBuilderType
  {
    private final TTableBuilder owner;
    private final ArrayList<String> cells;

    TTableRowBuilder(final TTableBuilder inOwner)
    {
      this.owner =
        Objects.requireNonNull(inOwner, "owner");
      this.cells =
        new ArrayList<>();
    }

    @Override
    public TTableRowBuilderType addCell(
      final String content)
      throws TException
    {
      Objects.requireNonNull(content, "content");
      this.checkCellCount();

      final var trimmedContent = content.trim();
      final var index = this.cells.size();

      this.owner.notifyColumnContentLength(index, trimmedContent.length());
      this.cells.add(trimmedContent);
      return this;
    }

    private void checkCellCount()
      throws TException
    {
      if (this.cells.size() + 1 > this.owner.columns.size()) {
        throw new TException(
          "Too many cells for this row.",
          "error-too-many-cells",
          Map.ofEntries(
            Map.entry(
              "Maximum Cells", Integer.toString(this.owner.columns.size())
            )
          ),
          Optional.empty()
        );
      }
    }
  }
}
