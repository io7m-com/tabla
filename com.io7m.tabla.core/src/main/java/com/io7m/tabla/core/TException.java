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

import com.io7m.seltzer.api.SStructuredErrorExceptionType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The type of exceptions raised by the {@code tabla} package.
 */

public final class TException
  extends Exception
  implements SStructuredErrorExceptionType<String>
{
  private final String errorCode;
  private final Map<String, String> attributes;
  private final Optional<String> remediatingAction;

  /**
   * Construct an exception.
   *
   * @param message             The error message
   * @param inErrorCode         The error code
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action
   */

  public TException(
    final String message,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction)
  {
    super(Objects.requireNonNull(message, "message"));

    this.errorCode =
      Objects.requireNonNull(inErrorCode, "errorCode");
    this.attributes =
      Objects.requireNonNull(inAttributes, "attributes");
    this.remediatingAction =
      Objects.requireNonNull(inRemediatingAction, "remediatingAction");
  }

  /**
   * Construct an exception.
   *
   * @param message             The error message
   * @param cause               The cause
   * @param inErrorCode         The error code
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action
   */

  public TException(
    final String message,
    final Throwable cause,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction)
  {
    super(
      Objects.requireNonNull(message, "message"),
      Objects.requireNonNull(cause, "cause")
    );
    this.errorCode =
      Objects.requireNonNull(inErrorCode, "errorCode");
    this.attributes =
      Objects.requireNonNull(inAttributes, "attributes");
    this.remediatingAction =
      Objects.requireNonNull(inRemediatingAction, "remediatingAction");
  }

  /**
   * Construct an exception.
   *
   * @param cause               The cause
   * @param inErrorCode         The error code
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action
   */

  public TException(
    final Throwable cause,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction)
  {
    super(Objects.requireNonNull(cause, "cause"));

    this.errorCode =
      Objects.requireNonNull(inErrorCode, "errorCode");
    this.attributes =
      Objects.requireNonNull(inAttributes, "attributes");
    this.remediatingAction =
      Objects.requireNonNull(inRemediatingAction, "remediatingAction");
  }

  /**
   * Construct an exception.
   *
   * @param message     The error message
   * @param cause       The cause
   * @param inErrorCode The error code
   */

  public TException(
    final Throwable cause,
    final String message,
    final String inErrorCode)
  {
    this(message, cause, inErrorCode, Map.of(), Optional.empty());
  }

  /**
   * Construct an exception.
   *
   * @param cause       The cause
   * @param inErrorCode The error code
   */

  public TException(
    final Throwable cause,
    final String inErrorCode)
  {
    this(cause, inErrorCode, Map.of(), Optional.empty());
  }

  @Override
  public String errorCode()
  {
    return this.errorCode;
  }

  @Override
  public Map<String, String> attributes()
  {
    return this.attributes;
  }

  @Override
  public Optional<String> remediatingAction()
  {
    return this.remediatingAction;
  }

  @Override
  public Optional<Throwable> exception()
  {
    return Optional.of(this);
  }
}
