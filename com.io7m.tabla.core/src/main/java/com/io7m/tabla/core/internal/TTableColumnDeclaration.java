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
import com.io7m.tabla.core.TColumnWidthConstraintMaximumAny;
import com.io7m.tabla.core.TColumnWidthConstraintMaximumAtMost;
import com.io7m.tabla.core.TColumnWidthConstraintMaximumType;
import com.io7m.tabla.core.TColumnWidthConstraintMinimumAny;
import com.io7m.tabla.core.TColumnWidthConstraintMinimumAtLeast;
import com.io7m.tabla.core.TColumnWidthConstraintMinimumFitContent;
import com.io7m.tabla.core.TColumnWidthConstraintMinimumFitHeader;
import com.io7m.tabla.core.TColumnWidthConstraintMinimumType;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.Objects;

import static org.chocosolver.solver.variables.IntVar.MAX_INT_BOUND;

final class TTableColumnDeclaration
{
  private final Model model;
  private final int index;
  private final String name;
  private final TColumnWidthConstraint constraint;
  private int maximumContentLength;

  public String name()
  {
    return this.name;
  }

  public IntVar createModelVariable()
  {
    final var cc =
      this.constraint;
    final var varName =
      "ColumnWidth[%d]".formatted(Integer.valueOf(this.index));

    return this.model.intVar(
      varName,
      this.minimumOf(cc.minimum()),
      maximumOf(cc.maximum())
    );
  }

  private static int maximumOf(
    final TColumnWidthConstraintMaximumType maximum)
  {
    if (maximum instanceof TColumnWidthConstraintMaximumAny) {
      return MAX_INT_BOUND;
    }
    if (maximum instanceof TColumnWidthConstraintMaximumAtMost c) {
      return c.size();
    }
    throw new IllegalStateException();
  }

  private int minimumOf(
    final TColumnWidthConstraintMinimumType minimum)
  {
    if (minimum instanceof TColumnWidthConstraintMinimumAny) {
      return 0;
    }
    if (minimum instanceof final TColumnWidthConstraintMinimumFitContent c) {
      return this.maximumContentLength;
    }
    if (minimum instanceof final TColumnWidthConstraintMinimumFitHeader c) {
      return this.name.length();
    }
    if (minimum instanceof final TColumnWidthConstraintMinimumAtLeast c) {
      return c.size();
    }
    throw new IllegalStateException();
  }

  TTableColumnDeclaration(
    final Model inModel,
    final int inIndex,
    final String inName,
    final TColumnWidthConstraint inConstraint)
  {
    this.model =
      Objects.requireNonNull(inModel, "inModel");
    this.index =
      inIndex;
    this.name =
      Objects.requireNonNull(inName, "name");
    this.constraint =
      Objects.requireNonNull(inConstraint, "inConstraint");
    this.maximumContentLength = 0;
  }

  public void notifyContentLength(
    final int length)
  {
    this.maximumContentLength =
      Math.max(this.maximumContentLength, length);
  }
}
