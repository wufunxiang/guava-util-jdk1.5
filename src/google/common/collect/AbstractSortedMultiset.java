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

import google.common.annotations.GwtCompatible;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import javax.annotation.Nullable;

/**
 * This class provides a skeletal implementation of the {@link SortedMultiset} interface.
 *
 * <p>The {@link #count} and {@link #size} implementations all iterate across the set returned by
 * {@link Multiset#entrySet()}, as do many methods acting on the set returned by
 * {@link #elementSet()}. Override those methods for better performance.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
abstract class AbstractSortedMultiset<E> extends AbstractMultiset<E> implements SortedMultiset<E> {
  @GwtTransient final Comparator<? super E> comparator;

  // needed for serialization
  @SuppressWarnings("unchecked")
  AbstractSortedMultiset() {
    this((Comparator) Ordering.natural());
  }

  AbstractSortedMultiset(Comparator<? super E> comparator) {
    this.comparator = checkNotNull(comparator);
  }

  
  public SortedSet<E> elementSet() {
    return (SortedSet<E>) super.elementSet();
  }

  
  SortedSet<E> createElementSet() {
    return new SortedMultisets.ElementSet<E>() {
      
      SortedMultiset<E> multiset() {
        return AbstractSortedMultiset.this;
      }
    };
  }

  
  public Comparator<? super E> comparator() {
    return comparator;
  }

  
  public Entry<E> firstEntry() {
    Iterator<Entry<E>> entryIterator = entryIterator();
    return entryIterator.hasNext() ? entryIterator.next() : null;
  }

  
  public Entry<E> lastEntry() {
    Iterator<Entry<E>> entryIterator = descendingEntryIterator();
    return entryIterator.hasNext() ? entryIterator.next() : null;
  }

  
  public Entry<E> pollFirstEntry() {
    Iterator<Entry<E>> entryIterator = entryIterator();
    if (entryIterator.hasNext()) {
      Entry<E> result = entryIterator.next();
      result = Multisets.immutableEntry(result.getElement(), result.getCount());
      entryIterator.remove();
      return result;
    }
    return null;
  }

  
  public Entry<E> pollLastEntry() {
    Iterator<Entry<E>> entryIterator = descendingEntryIterator();
    if (entryIterator.hasNext()) {
      Entry<E> result = entryIterator.next();
      result = Multisets.immutableEntry(result.getElement(), result.getCount());
      entryIterator.remove();
      return result;
    }
    return null;
  }

  
  public SortedMultiset<E> subMultiset(@Nullable E fromElement, BoundType fromBoundType,
      @Nullable E toElement, BoundType toBoundType) {
    // These are checked elsewhere, but NullPointerTester wants them checked eagerly.
    checkNotNull(fromBoundType);
    checkNotNull(toBoundType);
    return tailMultiset(fromElement, fromBoundType).headMultiset(toElement, toBoundType);
  }

  abstract Iterator<Entry<E>> descendingEntryIterator();

  Iterator<E> descendingIterator() {
    return Multisets.iteratorImpl(descendingMultiset());
  }

  private transient SortedMultiset<E> descendingMultiset;

  
  public SortedMultiset<E> descendingMultiset() {
    SortedMultiset<E> result = descendingMultiset;
    return (result == null) ? descendingMultiset = createDescendingMultiset() : result;
  }

  SortedMultiset<E> createDescendingMultiset() {
    return new SortedMultisets.DescendingMultiset<E>() {
      
      SortedMultiset<E> forwardMultiset() {
        return AbstractSortedMultiset.this;
      }

      
      Iterator<Entry<E>> entryIterator() {
        return descendingEntryIterator();
      }

      
      public Iterator<E> iterator() {
        return descendingIterator();
      }
    };
  }
}
