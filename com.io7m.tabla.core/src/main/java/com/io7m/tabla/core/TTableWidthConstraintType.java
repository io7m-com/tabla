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

import java.util.Optional;

import static org.chocosolver.solver.variables.IntVar.MAX_INT_BOUND;

/**
 * The type of table width constraints.
 */

public sealed interface TTableWidthConstraintType
  permits TTableWidthConstraintAny,
  TTableWidthConstraintRange
{
  /**
   * @return The constraint hardness
   */

  TConstraintHardness hardness();

  /**
   * @return A constraint such that the width can be anything
   */

  static TTableWidthConstraintAny any()
  {
    return TTableWidthConstraintAny.any();
  }

  /**
   * @param maximum  The maximum width
   * @param hardness The constraint hardness
   *
   * @return A constraint such that the width can be at most {@code maximum}
   */

  static TTableWidthConstraintRange tableWidthAtMost(
    final int maximum,
    final TConstraintHardness hardness)
  {
    return new TTableWidthConstraintRange(0, maximum, hardness);
  }

  /**
   * @param minimum  The minimum width
   * @param maximum  The maximum width
   * @param hardness The constraint hardness
   *
   * @return A constraint such that the width can be in the given range
   */

  static TTableWidthConstraintRange withinRange(
    final int minimum,
    final int maximum,
    final TConstraintHardness hardness)
  {
    return new TTableWidthConstraintRange(minimum, maximum, hardness);
  }

  /**
   * @param minimum The minimum width
   * @param maximum The maximum width
   *                @param hardness The constraint hardness
   *
   * @return A constraint such that the width can be in the given range
   */

  static TTableWidthConstraintRange withinRange(
    final Optional<Integer> minimum,
    final Optional<Integer> maximum,
    final TConstraintHardness hardness)
  {
    final var min =
      minimum.orElse(Integer.valueOf(0));

    return maximum.map(max -> {
      return withinRange(
        min.intValue(),
        max.intValue(),
        hardness
      );
    }).orElseGet(() -> {
      return withinRange(
        min.intValue(),
        MAX_INT_BOUND,
        hardness
      );
    });
  }

  /**
   * @param size     The size
   * @param hardness The constraint hardness
   *
   * @return A constraint such that the width must be exactly {@code size}
   */

  static TTableWidthConstraintRange tableWidthExact(
    final int size,
    final TConstraintHardness hardness)
  {
    return TTableWidthConstraintRange.exact(size, hardness);
  }
}
