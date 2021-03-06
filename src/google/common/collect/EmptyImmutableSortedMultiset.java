/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package google.common.collect;

import static google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Comparator;

import javax.annotation.Nullable;

/**
 * An empty immutable sorted multiset.
 *
 * @author Louis Wasserman
 */
@SuppressWarnings("serial") // Uses writeReplace, not default serialization
final class EmptyImmutableSortedMultiset<E> extends ImmutableSortedMultiset<E> {
  private final ImmutableSortedSet<E> elementSet;

  EmptyImmutableSortedMultiset(Comparator<? super E> comparator) {
    this.elementSet = ImmutableSortedSet.emptySet(comparator);
  }

  
  public Entry<E> firstEntry() {
    return null;
  }

  
  public Entry<E> lastEntry() {
    return null;
  }

  
  public int count(@Nullable Object element) {
    return 0;
  }

  
  public boolean contains(@Nullable Object object) {
    return false;
  }

  
  public boolean containsAll(Collection<?> targets) {
    return targets.isEmpty();
  }

  
  public int size() {
    return 0;
  }

  
  public ImmutableSortedSet<E> elementSet() {
    return elementSet;
  }

  
  public ImmutableSet<Entry<E>> entrySet() {
    return ImmutableSet.of();
  }

  
  ImmutableSet<Entry<E>> createEntrySet() {
    throw new AssertionError("should never be called");
  }

  
  public ImmutableSortedMultiset<E> headMultiset(E upperBound, BoundType boundType) {
    checkNotNull(upperBound);
    checkNotNull(boundType);
    return this;
  }

  
  public ImmutableSortedMultiset<E> tailMultiset(E lowerBound, BoundType boundType) {
    checkNotNull(lowerBound);
    checkNotNull(boundType);
    return this;
  }

  
  public UnmodifiableIterator<E> iterator() {
    return Iterators.emptyIterator();
  }

  
  public boolean equals(@Nullable Object object) {
    if (object instanceof Multiset) {
      Multiset<?> other = (Multiset<?>) object;
      return other.isEmpty();
    }
    return false;
  }

  
  public int hashCode() {
    return 0;
  }

  
  public String toString() {
    return "[]";
  }

  
  boolean isPartialView() {
    return false;
  }

  
  public Object[] toArray() {
    return ObjectArrays.EMPTY_ARRAY;
  }

  
  public <T> T[] toArray(T[] other) {
    return asList().toArray(other);
  }

  
  public ImmutableList<E> asList() {
    return ImmutableList.of();
  }
}
