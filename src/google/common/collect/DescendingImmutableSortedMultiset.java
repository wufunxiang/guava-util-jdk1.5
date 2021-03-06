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

import javax.annotation.Nullable;

/**
 * A descending wrapper around an {@code ImmutableSortedMultiset}
 *
 * @author Louis Wasserman
 */
@SuppressWarnings("serial") // uses writeReplace, not default serialization
final class DescendingImmutableSortedMultiset<E> extends ImmutableSortedMultiset<E> {
  private final transient ImmutableSortedMultiset<E> forward;

  DescendingImmutableSortedMultiset(ImmutableSortedMultiset<E> forward) {
    this.forward = forward;
  }

  
  public int count(@Nullable Object element) {
    return forward.count(element);
  }

  
  public Entry<E> firstEntry() {
    return forward.lastEntry();
  }

  
  public Entry<E> lastEntry() {
    return forward.firstEntry();
  }

  
  public int size() {
    return forward.size();
  }

  
  public ImmutableSortedSet<E> elementSet() {
    return forward.elementSet().descendingSet();
  }

  
  ImmutableSet<Entry<E>> createEntrySet() {
    final ImmutableSet<Entry<E>> forwardEntrySet = forward.entrySet();
    return new EntrySet() {
      
      public int size() {
        return forwardEntrySet.size();
      }

      
      public UnmodifiableIterator<Entry<E>> iterator() {
        return asList().iterator();
      }

      
      ImmutableList<Entry<E>> createAsList() {
        return forwardEntrySet.asList().reverse();
      }
    };
  }

  
  public ImmutableSortedMultiset<E> descendingMultiset() {
    return forward;
  }

  
  public ImmutableSortedMultiset<E> headMultiset(E upperBound, BoundType boundType) {
    return forward.tailMultiset(upperBound, boundType).descendingMultiset();
  }

  
  public ImmutableSortedMultiset<E> tailMultiset(E lowerBound, BoundType boundType) {
    return forward.headMultiset(lowerBound, boundType).descendingMultiset();
  }

  
  boolean isPartialView() {
    return forward.isPartialView();
  }
}
