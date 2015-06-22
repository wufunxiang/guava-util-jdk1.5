/*
 * Copyright (C) 2011 The Guava Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package google.common.collect;

import google.common.annotations.GwtCompatible;
import google.common.collect.Multiset.Entry;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

/**
 * Provides static utility methods for creating and working with
 * {@link google.common.collect.SortedMultiset} instances.
 * 
 * @author Louis Wasserman
 */
@GwtCompatible
final class SortedMultisets {
  private SortedMultisets() {
  }

  /**
   * A skeleton implementation for {@link google.common.collect.SortedMultiset#elementSet}.
   */
  static abstract class ElementSet<E> extends Multisets.ElementSet<E> implements
      SortedSet<E> {
     abstract SortedMultiset<E> multiset();

     public Comparator<? super E> comparator() {
      return multiset().comparator();
    }

     public SortedSet<E> subSet(E fromElement, E toElement) {
      return multiset().subMultiset(fromElement, BoundType.CLOSED, toElement,
          BoundType.OPEN).elementSet();
    }

     public SortedSet<E> headSet(E toElement) {
      return multiset().headMultiset(toElement, BoundType.OPEN).elementSet();
    }

     public SortedSet<E> tailSet(E fromElement) {
      return multiset().tailMultiset(fromElement, BoundType.CLOSED)
          .elementSet();
    }

     public E first() {
      return getElementOrThrow(multiset().firstEntry());
    }

     public E last() {
      return getElementOrThrow(multiset().lastEntry());
    }
  }

  private static <E> E getElementOrThrow(Entry<E> entry) {
    if (entry == null) {
      throw new NoSuchElementException();
    }
    return entry.getElement();
  }
  
  /**
   * A skeleton implementation of a descending multiset.  Only needs
   * {@code forwardMultiset()} and {@code entryIterator()}.
   */
  static abstract class DescendingMultiset<E> extends ForwardingMultiset<E>
      implements SortedMultiset<E> {
    abstract SortedMultiset<E> forwardMultiset();

    private transient Comparator<? super E> comparator;

     public Comparator<? super E> comparator() {
      Comparator<? super E> result = comparator;
      if (result == null) {
        return comparator =
            Ordering.from(forwardMultiset().comparator()).<E>reverse();
      }
      return result;
    }

    private transient SortedSet<E> elementSet;

     public SortedSet<E> elementSet() {
      SortedSet<E> result = elementSet;
      if (result == null) {
        return elementSet = new ElementSet<E>() {
           SortedMultiset<E> multiset() {
            return DescendingMultiset.this;
          }
        };
      }
      return result;
    }

     public Entry<E> pollFirstEntry() {
      return forwardMultiset().pollLastEntry();
    }

     public Entry<E> pollLastEntry() {
      return forwardMultiset().pollFirstEntry();
    }

     public SortedMultiset<E> headMultiset(E toElement,
        BoundType boundType) {
      return forwardMultiset().tailMultiset(toElement, boundType)
          .descendingMultiset();
    }

     public SortedMultiset<E> subMultiset(E fromElement,
        BoundType fromBoundType, E toElement, BoundType toBoundType) {
      return forwardMultiset().subMultiset(toElement, toBoundType, fromElement,
          fromBoundType).descendingMultiset();
    }

     public SortedMultiset<E> tailMultiset(E fromElement,
        BoundType boundType) {
      return forwardMultiset().headMultiset(fromElement, boundType)
          .descendingMultiset();
    }

     protected Multiset<E> delegate() {
      return forwardMultiset();
    }

     public SortedMultiset<E> descendingMultiset() {
      return forwardMultiset();
    }

     public Entry<E> firstEntry() {
      return forwardMultiset().lastEntry();
    }

     public Entry<E> lastEntry() {
      return forwardMultiset().firstEntry();
    }

    abstract Iterator<Entry<E>> entryIterator();

    private transient Set<Entry<E>> entrySet;

     public Set<Entry<E>> entrySet() {
      Set<Entry<E>> result = entrySet;
      return (result == null) ? entrySet = createEntrySet() : result;
    }

    Set<Entry<E>> createEntrySet() {
      return new Multisets.EntrySet<E>() {
         Multiset<E> multiset() {
          return DescendingMultiset.this;
        }

         public Iterator<Entry<E>> iterator() {
          return entryIterator();
        }

         public int size() {
          return forwardMultiset().entrySet().size();
        }
      };
    }

     public Iterator<E> iterator() {
      return Multisets.iteratorImpl(this);
    }

     public Object[] toArray() {
      return standardToArray();
    }

     public <T> T[] toArray(T[] array) {
      return standardToArray(array);
    }

     public String toString() {
      return entrySet().toString();
    }
  }
}
