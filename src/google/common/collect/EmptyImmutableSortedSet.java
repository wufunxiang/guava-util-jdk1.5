/*
 * Copyright (C) 2008 The Guava Authors
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

import google.common.annotations.GwtCompatible;

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An empty immutable sorted set.
 *
 * @author Jared Levy
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial") // uses writeReplace(), not default serialization
class EmptyImmutableSortedSet<E> extends ImmutableSortedSet<E> {
  EmptyImmutableSortedSet(Comparator<? super E> comparator) {
    super(comparator);
  }

  
  public int size() {
    return 0;
  }

   public boolean isEmpty() {
    return true;
  }

   public boolean contains(Object target) {
    return false;
  }

   public boolean containsAll(Collection<?> targets) {
    return targets.isEmpty();
  }

   public UnmodifiableIterator<E> iterator() {
    return Iterators.emptyIterator();
  }

   boolean isPartialView() {
    return false;
  }

   public ImmutableList<E> asList() {
    return ImmutableList.of();
  }

   public Object[] toArray() {
    return ObjectArrays.EMPTY_ARRAY;
  }

   public <T> T[] toArray(T[] a) {
    return asList().toArray(a);
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

   public String toString() {
    return "[]";
  }

  
  public E first() {
    throw new NoSuchElementException();
  }

  
  public E last() {
    throw new NoSuchElementException();
  }

  
  ImmutableSortedSet<E> headSetImpl(E toElement, boolean inclusive) {
    return this;
  }

  
  ImmutableSortedSet<E> subSetImpl(
      E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
    return this;
  }

  
  ImmutableSortedSet<E> tailSetImpl(E fromElement, boolean inclusive) {
    return this;
  }

   int indexOf(@Nullable Object target) {
    return -1;
  }

  
  ImmutableSortedSet<E> createDescendingSet() {
    return new EmptyImmutableSortedSet<E>(Ordering.from(comparator).reverse());
  }
}
