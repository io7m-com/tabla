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
 * The constraints on the width of a table column.
 *
 * @param minimum The constraint on the minimum width
 * @param maximum The constraint on the maximum width
 */

public record TColumnWidthConstraint(
  TColumnWidthConstraintMinimumType minimum,
  TColumnWidthConstraintMaximumType maximum)
{
  /**
   * @return A set of constraints that allow a column to be any size
   */

  public static TColumnWidthConstraint any()
  {
    return new TColumnWidthConstraint(
      TColumnWidthConstraintMinimumAny.any(),
      TColumnWidthConstraintMaximumAny.any()
    );
  }

  /**
   * Derive a constraint that requires a table column to be at least the
   * width of the text in the header.
   *
   * @return A constraint
   */

  public static TColumnWidthConstraint atLeastHeader()
  {
    return new TColumnWidthConstraint(
      TColumnWidthConstraintMinimumFitHeader.fitHeader(),
      TColumnWidthConstraintMaximumAny.any()
    );
  }

  /**
   * Derive a constraint that requires a table column to be at least the
   * length of the longest value in any row of the column.
   *
   * @return A constraint
   */

  public static TColumnWidthConstraint atLeastContent()
  {
    return new TColumnWidthConstraint(
      TColumnWidthConstraintMinimumFitContent.fitContent(),
      TColumnWidthConstraintMaximumAny.any()
    );
  }

  /**
   * Derive a constraint that requires a table column to be exactly the
   * given width.
   *
   * @param width The width
   *
   * @return A constraint
   */

  public static TColumnWidthConstraint exactWidth(
    final int width)
  {
    return new TColumnWidthConstraint(
      TColumnWidthConstraintMinimumAtLeast.atLeast(width),
      TColumnWidthConstraintMaximumAtMost.atMost(width)
    );
  }
}
