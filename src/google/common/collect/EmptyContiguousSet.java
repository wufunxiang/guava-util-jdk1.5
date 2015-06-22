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

import google.common.annotations.GwtCompatible;
import google.common.annotations.GwtIncompatible;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An empty contiguous set.
 *
 * @author Gregory Kick
 */
@GwtCompatible(emulated = true)
@SuppressWarnings("unchecked") // allow ungenerified Comparable types
final class EmptyContiguousSet<C extends Comparable> extends ContiguousSet<C> {
  EmptyContiguousSet(DiscreteDomain<C> domain) {
    super(domain);
  }

   public C first() {
    throw new NoSuchElementException();
  }

   public C last() {
    throw new NoSuchElementException();
  }

   public int size() {
    return 0;
  }

   public ContiguousSet<C> intersection(ContiguousSet<C> other) {
    return this;
  }

   public Range<C> range() {
    throw new NoSuchElementException();
  }

   public Range<C> range(BoundType lowerBoundType, BoundType upperBoundType) {
    throw new NoSuchElementException();
  }

   ContiguousSet<C> headSetImpl(C toElement, boolean inclusive) {
    return this;
  }

   ContiguousSet<C> subSetImpl(
      C fromElement, boolean fromInclusive, C toElement, boolean toInclusive) {
    return this;
  }

   ContiguousSet<C> tailSetImpl(C fromElement, boolean fromInclusive) {
    return this;
  }

  @GwtIncompatible("not used by GWT emulation")
   int indexOf(Object target) {
    return -1;
  }

   public UnmodifiableIterator<C> iterator() {
    return Iterators.emptyIterator();
  }

   boolean isPartialView() {
    return false;
  }

   public boolean isEmpty() {
    return true;
  }

   public ImmutableList<C> asList() {
    return ImmutableList.of();
  }

   public String toString() {
    return "[]";
  }

   public boolean equals(@Nullable Object object) {
    if (object instanceof Set) {
      Set<?> that = (Set<?>) object;
      return that.isEmpty();
    }
    return false;
  }

   public int hashCode() {
    return 0;
  }

  @GwtIncompatible("serialization")
  private static final class SerializedForm<C extends Comparable> implements Serializable {
    private final DiscreteDomain<C> domain;

    private SerializedForm(DiscreteDomain<C> domain) {
      this.domain = domain;
    }

    private Object readResolve() {
      return new EmptyContiguousSet<C>(domain);
    }

    private static final long serialVersionUID = 0;
  }

  @GwtIncompatible("serialization")
  
  Object writeReplace() {
    return new SerializedForm<C>(domain);
  }

  @GwtIncompatible("NavigableSet")
  ImmutableSortedSet<C> createDescendingSet() {
    return new EmptyImmutableSortedSet<C>(Ordering.natural().reverse());
  }
}
