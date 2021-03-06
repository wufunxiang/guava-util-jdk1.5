/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package google.common.collect;

import static google.common.base.Preconditions.checkNotNull;
import static google.common.base.Preconditions.checkPositionIndexes;
import static google.common.collect.BoundType.CLOSED;

import google.common.primitives.Ints;

import javax.annotation.Nullable;

/**
 * An immutable sorted multiset with one or more distinct elements.
 *
 * @author Louis Wasserman
 */
@SuppressWarnings("serial") // uses writeReplace, not default serialization
final class RegularImmutableSortedMultiset<E> extends ImmutableSortedMultiset<E> {
  private final transient RegularImmutableSortedSet<E> elementSet;
  private final transient int[] counts;
  private final transient long[] cumulativeCounts;
  private final transient int offset;
  private final transient int length;

  RegularImmutableSortedMultiset(
      RegularImmutableSortedSet<E> elementSet,
      int[] counts,
      long[] cumulativeCounts,
      int offset,
      int length) {
    this.elementSet = elementSet;
    this.counts = counts;
    this.cumulativeCounts = cumulativeCounts;
    this.offset = offset;
    this.length = length;
  }

  private Entry<E> getEntry(int index) {
    return Multisets.immutableEntry(
        elementSet.asList().get(index),
        counts[offset + index]);
  }

  
  public Entry<E> firstEntry() {
    return getEntry(0);
  }

  
  public Entry<E> lastEntry() {
    return getEntry(length - 1);
  }

  
  public int count(@Nullable Object element) {
    int index = elementSet.indexOf(element);
    return (index == -1) ? 0 : counts[index + offset];
  }

  
  public int size() {
    long size = cumulativeCounts[offset + length] - cumulativeCounts[offset];
    return Ints.saturatedCast(size);
  }

  
  public ImmutableSortedSet<E> elementSet() {
    return elementSet;
  }

  
  public ImmutableSortedMultiset<E> headMultiset(E upperBound, BoundType boundType) {
    return getSubMultiset(0, elementSet.headIndex(upperBound, checkNotNull(boundType) == CLOSED));
  }

  
  public ImmutableSortedMultiset<E> tailMultiset(E lowerBound, BoundType boundType) {
    return getSubMultiset(elementSet.tailIndex(lowerBound, checkNotNull(boundType) == CLOSED),
        length);
  }

  ImmutableSortedMultiset<E> getSubMultiset(int from, int to) {
    checkPositionIndexes(from, to, length);
    if (from == to) {
      return emptyMultiset(comparator());
    } else if (from == 0 && to == length) {
      return this;
    } else {
      RegularImmutableSortedSet<E> subElementSet =
          (RegularImmutableSortedSet<E>) elementSet.getSubSet(from, to);
      return new RegularImmutableSortedMultiset<E>(
          subElementSet, counts, cumulativeCounts, offset + from, to - from);
    }
  }

  
  ImmutableSet<Entry<E>> createEntrySet() {
    return new EntrySet();
  }

  private final class EntrySet extends ImmutableMultiset<E>.EntrySet {
    
    public int size() {
      return length;
    }

    
    public UnmodifiableIterator<Entry<E>> iterator() {
      return asList().iterator();
    }

    
    ImmutableList<Entry<E>> createAsList() {
      return new ImmutableAsList<Entry<E>>() {
        
        public Entry<E> get(int index) {
          return getEntry(index);
        }

        
        ImmutableCollection<Entry<E>> delegateCollection() {
          return EntrySet.this;
        }
      };
    }
  }

  
  boolean isPartialView() {
    return offset > 0 || length < counts.length;
  }
}
