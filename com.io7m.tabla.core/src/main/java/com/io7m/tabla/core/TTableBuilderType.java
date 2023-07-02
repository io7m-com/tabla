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

package com.io7m.tabla.core;

/**
 * The type of mutable table builders.
 */

public interface TTableBuilderType
{
  /**
   * Declare a new column.
   *
   * @param name       The column name/header text
   * @param constraint The width constraint
   *
   * @return this
   */

  TTableBuilderType declareColumn(
    String name,
    TColumnWidthConstraint constraint
  );

  /**
   * Declare a new column. The column has a width constraint that sizes it
   * to at least the width of the header text.
   *
   * @param name The column name/header text
   *
   * @return this
   */

  default TTableBuilderType declareColumn(
    final String name)
  {
    return this.declareColumn(name, TColumnWidthConstraint.atLeastHeader());
  }

  /**
   * @return An immutable table based on the given values
   *
   * @throws TException On errors
   */

  TTableType build()
    throws TException;

  /**
   * Set the table width constraint.
   *
   * @param constraint The constraint
   *
   * @return this
   */

  TTableBuilderType setWidthConstraint(
    TTableWidthConstraintType constraint);

  /**
   * Add a row to the table.
   *
   * @return A row builder
   */

  TTableRowBuilderType addRow();
}
