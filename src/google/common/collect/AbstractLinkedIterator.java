/*
 * Copyright (C) 2010 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package google.common.collect;

import google.common.annotations.Beta;
import google.common.annotations.GwtCompatible;

import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * This class provides a skeletal implementation of the {@code Iterator}
 * interface for sequences whose next element can always be derived from the
 * previous element. Null elements are not supported, nor is the
 * {@link #remove()} method.
 *
 * <p>Example: <pre>   {@code
 *
 *   Iterator<Integer> powersOfTwo = new AbstractLinkedIterator<Integer>(1) {
 *     protected Integer computeNext(Integer previous) {
 *       return (previous == 1 << 30) ? null : previous * 2;
 *     }
 *   };}</pre>
 *
 * @author Chris Povirk
 * @since 8.0
 * @deprecated This class has been renamed {@link AbstractSequentialIterator}.
 *     This class is scheduled to be removed in Guava release 13.0.
 */
@Beta
@Deprecated
@GwtCompatible
public abstract class AbstractLinkedIterator<T>
    extends UnmodifiableIterator<T> {
  private T nextOrNull;

  /**
   * Creates a new iterator with the given first element, or, if {@code
   * firstOrNull} is null, creates a new empty iterator.
   */
  protected AbstractLinkedIterator(@Nullable T firstOrNull) {
    this.nextOrNull = firstOrNull;
  }

  /**
   * Returns the element that follows {@code previous}, or returns {@code null}
   * if no elements remain. This method is invoked during each call to
   * {@link #next()} in order to compute the result of a <i>future</i> call to
   * {@code next()}.
   */
  protected abstract T computeNext(T previous);

  
  public final boolean hasNext() {
    return nextOrNull != null;
  }

  
  public final T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    try {
      return nextOrNull;
    } finally {
      nextOrNull = computeNext(nextOrNull);
    }
  }
}
