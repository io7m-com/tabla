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

import com.io7m.jaffirm.core.Preconditions;

import java.util.Objects;

/**
 * A constraint that says that the width of a table must be within the given
 * inclusive range.
 *
 * @param minimumSize The minimum size
 * @param maximumSize The maximum size
 * @param hardness    The constraint hardness
 */

public record TTableWidthConstraintRange(
  int minimumSize,
  int maximumSize,
  TConstraintHardness hardness)
  implements TTableWidthConstraintType
{
  /**
   * A constraint that says that the width of a table must be within the given
   * inclusive range.
   *
   * @param minimumSize The minimum size
   * @param maximumSize The maximum size
   * @param hardness    The constraint hardness
   */

  public TTableWidthConstraintRange
  {
    Preconditions.checkPreconditionV(
      minimumSize <= maximumSize,
      "Minimum size %d must be <= Maximum size %d",
      Integer.valueOf(minimumSize),
      Integer.valueOf(maximumSize)
    );

    Objects.requireNonNull(hardness, "hardness");
  }

  /**
   * @param size     The width
   * @param hardness The constraint hardness
   *
   * @return A constraint that requires a table be exactly the given width
   */

  public static TTableWidthConstraintRange exact(
    final int size,
    final TConstraintHardness hardness)
  {
    return new TTableWidthConstraintRange(size, size, hardness);
  }
}
