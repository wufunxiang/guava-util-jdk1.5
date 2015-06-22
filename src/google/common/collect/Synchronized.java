/*
 * Copyright (C) 2007 The Guava Authors
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

import static google.common.base.Preconditions.checkNotNull;

import google.common.annotations.GwtCompatible;
import google.common.annotations.GwtIncompatible;
import google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import javax.annotation.Nullable;

/**
 * Synchronized collection views. The returned synchronized collection views are
 * serializable if the backing collection and the mutex are serializable.
 *
 * <p>If {@code null} is passed as the {@code mutex} parameter to any of this
 * class's top-level methods or inner class constructors, the created object
 * uses itself as the synchronization mutex.
 *
 * <p>This class should be used by other collection classes only.
 *
 * @author Mike Bostock
 * @author Jared Levy
 */
@GwtCompatible(emulated = true)
final class Synchronized {
  private Synchronized() {}

  static class SynchronizedObject implements Serializable {
    final Object delegate;
    final Object mutex;

    SynchronizedObject(Object delegate, @Nullable Object mutex) {
      this.delegate = checkNotNull(delegate);
      this.mutex = (mutex == null) ? this : mutex;
    }

    Object delegate() {
      return delegate;
    }

    // No equals and hashCode; see ForwardingObject for details.

     public String toString() {
      synchronized (mutex) {
        return delegate.toString();
      }
    }

    // Serialization invokes writeObject only when it's private.
    // The SynchronizedObject subclasses don't need a writeObject method since
    // they don't contain any non-transient member variables, while the
    // following writeObject() handles the SynchronizedObject members.

    @GwtIncompatible("java.io.ObjectOutputStream")
    private void writeObject(ObjectOutputStream stream) throws IOException {
      synchronized (mutex) {
        stream.defaultWriteObject();
      }
    }

    @GwtIncompatible("not needed in emulated source")
    private static final long serialVersionUID = 0;
  }

  private static <E> Collection<E> collection(
      Collection<E> collection, @Nullable Object mutex) {
    return new SynchronizedCollection<E>(collection, mutex);
  }

  @VisibleForTesting static class SynchronizedCollection<E>
      extends SynchronizedObject implements Collection<E> {
    private SynchronizedCollection(
        Collection<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @SuppressWarnings("unchecked")
     Collection<E> delegate() {
      return (Collection<E>) super.delegate();
    }

    
    public boolean add(E e) {
      synchronized (mutex) {
        return delegate().add(e);
      }
    }

    
    public boolean addAll(Collection<? extends E> c) {
      synchronized (mutex) {
        return delegate().addAll(c);
      }
    }

    
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }

    
    public boolean contains(Object o) {
      synchronized (mutex) {
        return delegate().contains(o);
      }
    }

    
    public boolean containsAll(Collection<?> c) {
      synchronized (mutex) {
        return delegate().containsAll(c);
      }
    }

    
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }

    
    public Iterator<E> iterator() {
      return delegate().iterator(); // manually synchronized
    }

    
    public boolean remove(Object o) {
      synchronized (mutex) {
        return delegate().remove(o);
      }
    }

    
    public boolean removeAll(Collection<?> c) {
      synchronized (mutex) {
        return delegate().removeAll(c);
      }
    }

    
    public boolean retainAll(Collection<?> c) {
      synchronized (mutex) {
        return delegate().retainAll(c);
      }
    }

    
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }

    
    public Object[] toArray() {
      synchronized (mutex) {
        return delegate().toArray();
      }
    }

    
    public <T> T[] toArray(T[] a) {
      synchronized (mutex) {
        return delegate().toArray(a);
      }
    }

    private static final long serialVersionUID = 0;
  }

  @VisibleForTesting static <E> Set<E> set(Set<E> set, @Nullable Object mutex) {
    return new SynchronizedSet<E>(set, mutex);
  }

  static class SynchronizedSet<E>
      extends SynchronizedCollection<E> implements Set<E> {

    SynchronizedSet(Set<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     Set<E> delegate() {
      return (Set<E>) super.delegate();
    }

     public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

     public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static <E> SortedSet<E> sortedSet(
      SortedSet<E> set, @Nullable Object mutex) {
    return new SynchronizedSortedSet<E>(set, mutex);
  }

  static class SynchronizedSortedSet<E> extends SynchronizedSet<E>
      implements SortedSet<E> {
    SynchronizedSortedSet(SortedSet<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     SortedSet<E> delegate() {
      return (SortedSet<E>) super.delegate();
    }

    
    public Comparator<? super E> comparator() {
      synchronized (mutex) {
        return delegate().comparator();
      }
    }

    
    public SortedSet<E> subSet(E fromElement, E toElement) {
      synchronized (mutex) {
        return sortedSet(delegate().subSet(fromElement, toElement), mutex);
      }
    }

    
    public SortedSet<E> headSet(E toElement) {
      synchronized (mutex) {
        return sortedSet(delegate().headSet(toElement), mutex);
      }
    }

    
    public SortedSet<E> tailSet(E fromElement) {
      synchronized (mutex) {
        return sortedSet(delegate().tailSet(fromElement), mutex);
      }
    }

    
    public E first() {
      synchronized (mutex) {
        return delegate().first();
      }
    }

    
    public E last() {
      synchronized (mutex) {
        return delegate().last();
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static <E> List<E> list(List<E> list, @Nullable Object mutex) {
    return (list instanceof RandomAccess)
        ? new SynchronizedRandomAccessList<E>(list, mutex)
        : new SynchronizedList<E>(list, mutex);
  }

  private static class SynchronizedList<E> extends SynchronizedCollection<E>
      implements List<E> {
    SynchronizedList(List<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     List<E> delegate() {
      return (List<E>) super.delegate();
    }

    
    public void add(int index, E element) {
      synchronized (mutex) {
        delegate().add(index, element);
      }
    }

    
    public boolean addAll(int index, Collection<? extends E> c) {
      synchronized (mutex) {
        return delegate().addAll(index, c);
      }
    }

    
    public E get(int index) {
      synchronized (mutex) {
        return delegate().get(index);
      }
    }

    
    public int indexOf(Object o) {
      synchronized (mutex) {
        return delegate().indexOf(o);
      }
    }

    
    public int lastIndexOf(Object o) {
      synchronized (mutex) {
        return delegate().lastIndexOf(o);
      }
    }

    
    public ListIterator<E> listIterator() {
      return delegate().listIterator(); // manually synchronized
    }

    
    public ListIterator<E> listIterator(int index) {
      return delegate().listIterator(index); // manually synchronized
    }

    
    public E remove(int index) {
      synchronized (mutex) {
        return delegate().remove(index);
      }
    }

    
    public E set(int index, E element) {
      synchronized (mutex) {
        return delegate().set(index, element);
      }
    }

    
    public List<E> subList(int fromIndex, int toIndex) {
      synchronized (mutex) {
        return list(delegate().subList(fromIndex, toIndex), mutex);
      }
    }

     public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

     public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static class SynchronizedRandomAccessList<E>
      extends SynchronizedList<E> implements RandomAccess {
    SynchronizedRandomAccessList(List<E> list, @Nullable Object mutex) {
      super(list, mutex);
    }
    private static final long serialVersionUID = 0;
  }

  static <E> Multiset<E> multiset(
      Multiset<E> multiset, @Nullable Object mutex) {
    if (multiset instanceof SynchronizedMultiset ||
        multiset instanceof ImmutableMultiset) {
      return multiset;
    }
    return new SynchronizedMultiset<E>(multiset, mutex);
  }

  private static class SynchronizedMultiset<E> extends SynchronizedCollection<E>
      implements Multiset<E> {
    transient Set<E> elementSet;
    transient Set<Entry<E>> entrySet;

    SynchronizedMultiset(Multiset<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     Multiset<E> delegate() {
      return (Multiset<E>) super.delegate();
    }

    
    public int count(Object o) {
      synchronized (mutex) {
        return delegate().count(o);
      }
    }

    
    public int add(E e, int n) {
      synchronized (mutex) {
        return delegate().add(e, n);
      }
    }

    
    public int remove(Object o, int n) {
      synchronized (mutex) {
        return delegate().remove(o, n);
      }
    }

    
    public int setCount(E element, int count) {
      synchronized (mutex) {
        return delegate().setCount(element, count);
      }
    }

    
    public boolean setCount(E element, int oldCount, int newCount) {
      synchronized (mutex) {
        return delegate().setCount(element, oldCount, newCount);
      }
    }

    
    public Set<E> elementSet() {
      synchronized (mutex) {
        if (elementSet == null) {
          elementSet = typePreservingSet(delegate().elementSet(), mutex);
        }
        return elementSet;
      }
    }

    
    public Set<Entry<E>> entrySet() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = typePreservingSet(delegate().entrySet(), mutex);
        }
        return entrySet;
      }
    }

     public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

     public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> Multimap<K, V> multimap(
      Multimap<K, V> multimap, @Nullable Object mutex) {
    if (multimap instanceof SynchronizedMultimap ||
        multimap instanceof ImmutableMultimap) {
      return multimap;
    }
    return new SynchronizedMultimap<K, V>(multimap, mutex);
  }

  private static class SynchronizedMultimap<K, V> extends SynchronizedObject
      implements Multimap<K, V> {
    transient Set<K> keySet;
    transient Collection<V> valuesCollection;
    transient Collection<Entry<K, V>> entries;
    transient Map<K, Collection<V>> asMap;
    transient Multiset<K> keys;

    @SuppressWarnings("unchecked")
     Multimap<K, V> delegate() {
      return (Multimap<K, V>) super.delegate();
    }

    SynchronizedMultimap(Multimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

    
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }

    
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }

    
    public boolean containsKey(Object key) {
      synchronized (mutex) {
        return delegate().containsKey(key);
      }
    }

    
    public boolean containsValue(Object value) {
      synchronized (mutex) {
        return delegate().containsValue(value);
      }
    }

    
    public boolean containsEntry(Object key, Object value) {
      synchronized (mutex) {
        return delegate().containsEntry(key, value);
      }
    }

    
    public Collection<V> get(K key) {
      synchronized (mutex) {
        return typePreservingCollection(delegate().get(key), mutex);
      }
    }

    
    public boolean put(K key, V value) {
      synchronized (mutex) {
        return delegate().put(key, value);
      }
    }

    
    public boolean putAll(K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().putAll(key, values);
      }
    }

    
    public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      synchronized (mutex) {
        return delegate().putAll(multimap);
      }
    }

    
    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }

    
    public boolean remove(Object key, Object value) {
      synchronized (mutex) {
        return delegate().remove(key, value);
      }
    }

    
    public Collection<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }

    
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }

    
    public Set<K> keySet() {
      synchronized (mutex) {
        if (keySet == null) {
          keySet = typePreservingSet(delegate().keySet(), mutex);
        }
        return keySet;
      }
    }

    
    public Collection<V> values() {
      synchronized (mutex) {
        if (valuesCollection == null) {
          valuesCollection = collection(delegate().values(), mutex);
        }
        return valuesCollection;
      }
    }

    
    public Collection<Entry<K, V>> entries() {
      synchronized (mutex) {
        if (entries == null) {
          entries = typePreservingCollection(delegate().entries(), mutex);
        }
        return entries;
      }
    }

    
    public Map<K, Collection<V>> asMap() {
      synchronized (mutex) {
        if (asMap == null) {
          asMap = new SynchronizedAsMap<K, V>(delegate().asMap(), mutex);
        }
        return asMap;
      }
    }

    
    public Multiset<K> keys() {
      synchronized (mutex) {
        if (keys == null) {
          keys = multiset(delegate().keys(), mutex);
        }
        return keys;
      }
    }

     public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

     public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> ListMultimap<K, V> listMultimap(
      ListMultimap<K, V> multimap, @Nullable Object mutex) {
    if (multimap instanceof SynchronizedListMultimap ||
        multimap instanceof ImmutableListMultimap) {
      return multimap;
    }
    return new SynchronizedListMultimap<K, V>(multimap, mutex);
  }

  private static class SynchronizedListMultimap<K, V>
      extends SynchronizedMultimap<K, V> implements ListMultimap<K, V> {
    SynchronizedListMultimap(
        ListMultimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
     ListMultimap<K, V> delegate() {
      return (ListMultimap<K, V>) super.delegate();
    }
     public List<V> get(K key) {
      synchronized (mutex) {
        return list(delegate().get(key), mutex);
      }
    }
     public List<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }
     public List<V> replaceValues(
        K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }
    private static final long serialVersionUID = 0;
  }

  static <K, V> SetMultimap<K, V> setMultimap(
      SetMultimap<K, V> multimap, @Nullable Object mutex) {
    if (multimap instanceof SynchronizedSetMultimap ||
        multimap instanceof ImmutableSetMultimap) {
      return multimap;
    }
    return new SynchronizedSetMultimap<K, V>(multimap, mutex);
  }

  private static class SynchronizedSetMultimap<K, V>
      extends SynchronizedMultimap<K, V> implements SetMultimap<K, V> {
    transient Set<Entry<K, V>> entrySet;

    SynchronizedSetMultimap(
        SetMultimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
     SetMultimap<K, V> delegate() {
      return (SetMultimap<K, V>) super.delegate();
    }
     public Set<V> get(K key) {
      synchronized (mutex) {
        return set(delegate().get(key), mutex);
      }
    }
     public Set<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }
     public Set<V> replaceValues(
        K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }
     public Set<Entry<K, V>> entries() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = set(delegate().entries(), mutex);
        }
        return entrySet;
      }
    }
    private static final long serialVersionUID = 0;
  }

  static <K, V> SortedSetMultimap<K, V> sortedSetMultimap(
      SortedSetMultimap<K, V> multimap, @Nullable Object mutex) {
    if (multimap instanceof SynchronizedSortedSetMultimap) {
      return multimap;
    }
    return new SynchronizedSortedSetMultimap<K, V>(multimap, mutex);
  }

  private static class SynchronizedSortedSetMultimap<K, V>
      extends SynchronizedSetMultimap<K, V> implements SortedSetMultimap<K, V> {
    SynchronizedSortedSetMultimap(
        SortedSetMultimap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }
     SortedSetMultimap<K, V> delegate() {
      return (SortedSetMultimap<K, V>) super.delegate();
    }
     public SortedSet<V> get(K key) {
      synchronized (mutex) {
        return sortedSet(delegate().get(key), mutex);
      }
    }
     public SortedSet<V> removeAll(Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }
     public SortedSet<V> replaceValues(
        K key, Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }
    
    public Comparator<? super V> valueComparator() {
      synchronized (mutex) {
        return delegate().valueComparator();
      }
    }
    private static final long serialVersionUID = 0;
  }

  private static <E> Collection<E> typePreservingCollection(
      Collection<E> collection, @Nullable Object mutex) {
    if (collection instanceof SortedSet) {
      return sortedSet((SortedSet<E>) collection, mutex);
    }
    if (collection instanceof Set) {
      return set((Set<E>) collection, mutex);
    }
    if (collection instanceof List) {
      return list((List<E>) collection, mutex);
    }
    return collection(collection, mutex);
  }

  private static <E> Set<E> typePreservingSet(
      Set<E> set, @Nullable Object mutex) {
    if (set instanceof SortedSet) {
      return sortedSet((SortedSet<E>) set, mutex);
    } else {
      return set(set, mutex);
    }
  }

  private static class SynchronizedAsMapEntries<K, V>
      extends SynchronizedSet<Entry<K, Collection<V>>> {
    SynchronizedAsMapEntries(
        Set<Entry<K, Collection<V>>> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     public Iterator<Entry<K, Collection<V>>> iterator() {
      // Must be manually synchronized.
      final Iterator<Entry<K, Collection<V>>> iterator = super.iterator();
      return new ForwardingIterator<Entry<K, Collection<V>>>() {
         protected Iterator<Entry<K, Collection<V>>> delegate() {
          return iterator;
        }

         public Entry<K, Collection<V>> next() {
          final Entry<K, Collection<V>> entry = super.next();
          return new ForwardingMapEntry<K, Collection<V>>() {
             protected Entry<K, Collection<V>> delegate() {
              return entry;
            }
             public Collection<V> getValue() {
              return typePreservingCollection(entry.getValue(), mutex);
            }
          };
        }
      };
    }

    // See Collections.CheckedMap.CheckedEntrySet for details on attacks.

     public Object[] toArray() {
      synchronized (mutex) {
        return ObjectArrays.toArrayImpl(delegate());
      }
    }
     public <T> T[] toArray(T[] array) {
      synchronized (mutex) {
        return ObjectArrays.toArrayImpl(delegate(), array);
      }
    }
     public boolean contains(Object o) {
      synchronized (mutex) {
        return Maps.containsEntryImpl(delegate(), o);
      }
    }
     public boolean containsAll(Collection<?> c) {
      synchronized (mutex) {
        return Collections2.containsAllImpl(delegate(), c);
      }
    }
     public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return Sets.equalsImpl(delegate(), o);
      }
    }
     public boolean remove(Object o) {
      synchronized (mutex) {
        return Maps.removeEntryImpl(delegate(), o);
      }
    }
     public boolean removeAll(Collection<?> c) {
      synchronized (mutex) {
        return Iterators.removeAll(delegate().iterator(), c);
      }
    }
     public boolean retainAll(Collection<?> c) {
      synchronized (mutex) {
        return Iterators.retainAll(delegate().iterator(), c);
      }
    }

    private static final long serialVersionUID = 0;
  }

  @VisibleForTesting
  static <K, V> Map<K, V> map(Map<K, V> map, @Nullable Object mutex) {
    return new SynchronizedMap<K, V>(map, mutex);
  }

  private static class SynchronizedMap<K, V> extends SynchronizedObject
      implements Map<K, V> {
    transient Set<K> keySet;
    transient Collection<V> values;
    transient Set<Entry<K, V>> entrySet;

    SynchronizedMap(Map<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @SuppressWarnings("unchecked")
     Map<K, V> delegate() {
      return (Map<K, V>) super.delegate();
    }

    
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }

    
    public boolean containsKey(Object key) {
      synchronized (mutex) {
        return delegate().containsKey(key);
      }
    }

    
    public boolean containsValue(Object value) {
      synchronized (mutex) {
        return delegate().containsValue(value);
      }
    }

    
    public Set<Entry<K, V>> entrySet() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = set(delegate().entrySet(), mutex);
        }
        return entrySet;
      }
    }

    
    public V get(Object key) {
      synchronized (mutex) {
        return delegate().get(key);
      }
    }

    
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }

    
    public Set<K> keySet() {
      synchronized (mutex) {
        if (keySet == null) {
          keySet = set(delegate().keySet(), mutex);
        }
        return keySet;
      }
    }

    
    public V put(K key, V value) {
      synchronized (mutex) {
        return delegate().put(key, value);
      }
    }

    
    public void putAll(Map<? extends K, ? extends V> map) {
      synchronized (mutex) {
        delegate().putAll(map);
      }
    }

    
    public V remove(Object key) {
      synchronized (mutex) {
        return delegate().remove(key);
      }
    }

    
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }

    
    public Collection<V> values() {
      synchronized (mutex) {
        if (values == null) {
          values = collection(delegate().values(), mutex);
        }
        return values;
      }
    }

     public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

     public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> SortedMap<K, V> sortedMap(
      SortedMap<K, V> sortedMap, @Nullable Object mutex) {
    return new SynchronizedSortedMap<K, V>(sortedMap, mutex);
  }

  static class SynchronizedSortedMap<K, V> extends SynchronizedMap<K, V>
      implements SortedMap<K, V> {

    SynchronizedSortedMap(SortedMap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     SortedMap<K, V> delegate() {
      return (SortedMap<K, V>) super.delegate();
    }

     public Comparator<? super K> comparator() {
      synchronized (mutex) {
        return delegate().comparator();
      }
    }

     public K firstKey() {
      synchronized (mutex) {
        return delegate().firstKey();
      }
    }

     public SortedMap<K, V> headMap(K toKey) {
      synchronized (mutex) {
        return sortedMap(delegate().headMap(toKey), mutex);
      }
    }

     public K lastKey() {
      synchronized (mutex) {
        return delegate().lastKey();
      }
    }

     public SortedMap<K, V> subMap(K fromKey, K toKey) {
      synchronized (mutex) {
        return sortedMap(delegate().subMap(fromKey, toKey), mutex);
      }
    }

     public SortedMap<K, V> tailMap(K fromKey) {
      synchronized (mutex) {
        return sortedMap(delegate().tailMap(fromKey), mutex);
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> BiMap<K, V> biMap(BiMap<K, V> bimap, @Nullable Object mutex) {
    if (bimap instanceof SynchronizedBiMap ||
        bimap instanceof ImmutableBiMap) {
      return bimap;
    }
    return new SynchronizedBiMap<K, V>(bimap, mutex, null);
  }

  @VisibleForTesting static class SynchronizedBiMap<K, V>
      extends SynchronizedMap<K, V> implements BiMap<K, V>, Serializable {
    private transient Set<V> valueSet;
    private transient BiMap<V, K> inverse;

    private SynchronizedBiMap(BiMap<K, V> delegate, @Nullable Object mutex,
        @Nullable BiMap<V, K> inverse) {
      super(delegate, mutex);
      this.inverse = inverse;
    }

     BiMap<K, V> delegate() {
      return (BiMap<K, V>) super.delegate();
    }

     public Set<V> values() {
      synchronized (mutex) {
        if (valueSet == null) {
          valueSet = set(delegate().values(), mutex);
        }
        return valueSet;
      }
    }

    
    public V forcePut(K key, V value) {
      synchronized (mutex) {
        return delegate().forcePut(key, value);
      }
    }

    
    public BiMap<V, K> inverse() {
      synchronized (mutex) {
        if (inverse == null) {
          inverse
              = new SynchronizedBiMap<V, K>(delegate().inverse(), mutex, this);
        }
        return inverse;
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static class SynchronizedAsMap<K, V>
      extends SynchronizedMap<K, Collection<V>> {
    transient Set<Entry<K, Collection<V>>> asMapEntrySet;
    transient Collection<Collection<V>> asMapValues;

    SynchronizedAsMap(Map<K, Collection<V>> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     public Collection<V> get(Object key) {
      synchronized (mutex) {
        Collection<V> collection = super.get(key);
        return (collection == null) ? null
            : typePreservingCollection(collection, mutex);
      }
    }

     public Set<Entry<K, Collection<V>>> entrySet() {
      synchronized (mutex) {
        if (asMapEntrySet == null) {
          asMapEntrySet = new SynchronizedAsMapEntries<K, V>(
              delegate().entrySet(), mutex);
        }
        return asMapEntrySet;
      }
    }

     public Collection<Collection<V>> values() {
      synchronized (mutex) {
        if (asMapValues == null) {
          asMapValues
              = new SynchronizedAsMapValues<V>(delegate().values(), mutex);
        }
        return asMapValues;
      }
    }

     public boolean containsValue(Object o) {
      // values() and its contains() method are both synchronized.
      return values().contains(o);
    }

    private static final long serialVersionUID = 0;
  }

  private static class SynchronizedAsMapValues<V>
      extends SynchronizedCollection<Collection<V>> {
    SynchronizedAsMapValues(
        Collection<Collection<V>> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     public Iterator<Collection<V>> iterator() {
      // Must be manually synchronized.
      final Iterator<Collection<V>> iterator = super.iterator();
      return new ForwardingIterator<Collection<V>>() {
         protected Iterator<Collection<V>> delegate() {
          return iterator;
        }
         public Collection<V> next() {
          return typePreservingCollection(super.next(), mutex);
        }
      };
    }

    private static final long serialVersionUID = 0;
  }

  @GwtIncompatible("NavigableSet")
  @VisibleForTesting
  static class SynchronizedNavigableSet<E> extends SynchronizedSortedSet<E>
      implements NavigableSet<E> {
    SynchronizedNavigableSet(NavigableSet<E> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     NavigableSet<E> delegate() {
      return (NavigableSet<E>) super.delegate();
    }

     public E ceiling(E e) {
      synchronized (mutex) {
        return delegate().ceiling(e);
      }
    }

     public Iterator<E> descendingIterator() {
      return delegate().descendingIterator(); // manually synchronized
    }

    transient NavigableSet<E> descendingSet;

     public NavigableSet<E> descendingSet() {
      synchronized (mutex) {
        if (descendingSet == null) {
          NavigableSet<E> dS =
              Synchronized.navigableSet(delegate().descendingSet(), mutex);
          descendingSet = dS;
          return dS;
        }
        return descendingSet;
      }
    }

     public E floor(E e) {
      synchronized (mutex) {
        return delegate().floor(e);
      }
    }

     public NavigableSet<E> headSet(E toElement, boolean inclusive) {
      synchronized (mutex) {
        return Synchronized.navigableSet(
            delegate().headSet(toElement, inclusive), mutex);
      }
    }

     public E higher(E e) {
      synchronized (mutex) {
        return delegate().higher(e);
      }
    }

     public E lower(E e) {
      synchronized (mutex) {
        return delegate().lower(e);
      }
    }

     public E pollFirst() {
      synchronized (mutex) {
        return delegate().pollFirst();
      }
    }

     public E pollLast() {
      synchronized (mutex) {
        return delegate().pollLast();
      }
    }

     public NavigableSet<E> subSet(E fromElement,
        boolean fromInclusive, E toElement, boolean toInclusive) {
      synchronized (mutex) {
        return Synchronized.navigableSet(delegate().subSet(
            fromElement, fromInclusive, toElement, toInclusive), mutex);
      }
    }

     public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
      synchronized (mutex) {
        return Synchronized.navigableSet(
            delegate().tailSet(fromElement, inclusive), mutex);
      }
    }

     public SortedSet<E> headSet(E toElement) {
      return headSet(toElement, false);
    }

     public SortedSet<E> subSet(E fromElement, E toElement) {
      return subSet(fromElement, true, toElement, false);
    }

     public SortedSet<E> tailSet(E fromElement) {
      return tailSet(fromElement, true);
    }

    private static final long serialVersionUID = 0;
  }

  @GwtIncompatible("NavigableSet")
  static <E> NavigableSet<E> navigableSet(
      NavigableSet<E> navigableSet, @Nullable Object mutex) {
    return new SynchronizedNavigableSet<E>(navigableSet, mutex);
  }

  @GwtIncompatible("NavigableSet")
  static <E> NavigableSet<E> navigableSet(NavigableSet<E> navigableSet) {
    return navigableSet(navigableSet, null);
  }

  @GwtIncompatible("NavigableMap")
  static <K, V> NavigableMap<K, V> navigableMap(
      NavigableMap<K, V> navigableMap) {
    return navigableMap(navigableMap, null);
  }

  @GwtIncompatible("NavigableMap")
  static <K, V> NavigableMap<K, V> navigableMap(
      NavigableMap<K, V> navigableMap, @Nullable Object mutex) {
    return new SynchronizedNavigableMap<K, V>(navigableMap, mutex);
  }

  @GwtIncompatible("NavigableMap")
  @VisibleForTesting static class SynchronizedNavigableMap<K, V>
      extends SynchronizedSortedMap<K, V> implements NavigableMap<K, V> {

    SynchronizedNavigableMap(
        NavigableMap<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

     NavigableMap<K, V> delegate() {
      return (NavigableMap<K, V>) super.delegate();
    }

     public Entry<K, V> ceilingEntry(K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().ceilingEntry(key), mutex);
      }
    }

     public K ceilingKey(K key) {
      synchronized (mutex) {
        return delegate().ceilingKey(key);
      }
    }

    transient NavigableSet<K> descendingKeySet;

     public NavigableSet<K> descendingKeySet() {
      synchronized (mutex) {
        if (descendingKeySet == null) {
          return descendingKeySet =
              Synchronized.navigableSet(delegate().descendingKeySet(), mutex);
        }
        return descendingKeySet;
      }
    }

    transient NavigableMap<K, V> descendingMap;

     public NavigableMap<K, V> descendingMap() {
      synchronized (mutex) {
        if (descendingMap == null) {
          return descendingMap =
              navigableMap(delegate().descendingMap(), mutex);
        }
        return descendingMap;
      }
    }

     public Entry<K, V> firstEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().firstEntry(), mutex);
      }
    }

     public Entry<K, V> floorEntry(K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().floorEntry(key), mutex);
      }
    }

     public K floorKey(K key) {
      synchronized (mutex) {
        return delegate().floorKey(key);
      }
    }

     public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
      synchronized (mutex) {
        return navigableMap(
            delegate().headMap(toKey, inclusive), mutex);
      }
    }

     public Entry<K, V> higherEntry(K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().higherEntry(key), mutex);
      }
    }

     public K higherKey(K key) {
      synchronized (mutex) {
        return delegate().higherKey(key);
      }
    }

     public Entry<K, V> lastEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().lastEntry(), mutex);
      }
    }

     public Entry<K, V> lowerEntry(K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().lowerEntry(key), mutex);
      }
    }

     public K lowerKey(K key) {
      synchronized (mutex) {
        return delegate().lowerKey(key);
      }
    }

     public Set<K> keySet() {
      return navigableKeySet();
    }

    transient NavigableSet<K> navigableKeySet;

     public NavigableSet<K> navigableKeySet() {
      synchronized (mutex) {
        if (navigableKeySet == null) {
          return navigableKeySet =
              Synchronized.navigableSet(delegate().navigableKeySet(), mutex);
        }
        return navigableKeySet;
      }
    }

     public Entry<K, V> pollFirstEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().pollFirstEntry(), mutex);
      }
    }

     public Entry<K, V> pollLastEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().pollLastEntry(), mutex);
      }
    }

     public NavigableMap<K, V> subMap(
        K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      synchronized (mutex) {
        return navigableMap(
            delegate().subMap(fromKey, fromInclusive, toKey, toInclusive),
            mutex);
      }
    }

     public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
      synchronized (mutex) {
        return navigableMap(
            delegate().tailMap(fromKey, inclusive), mutex);
      }
    }

     public SortedMap<K, V> headMap(K toKey) {
      return headMap(toKey, false);
    }

     public SortedMap<K, V> subMap(K fromKey, K toKey) {
      return subMap(fromKey, true, toKey, false);
    }

     public SortedMap<K, V> tailMap(K fromKey) {
      return tailMap(fromKey, true);
    }

    private static final long serialVersionUID = 0;
  }

  @GwtIncompatible("works but is needed only for NavigableMap")
  private static <K, V> Entry<K, V> nullableSynchronizedEntry(
      @Nullable Entry<K, V> entry, @Nullable Object mutex) {
    if (entry == null) {
      return null;
    }
    return new SynchronizedEntry<K, V>(entry, mutex);
  }

  @GwtIncompatible("works but is needed only for NavigableMap")
  private static class SynchronizedEntry<K, V> extends SynchronizedObject
      implements Entry<K, V> {

    SynchronizedEntry(Entry<K, V> delegate, @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @SuppressWarnings("unchecked") // guaranteed by the constructor
     Entry<K, V> delegate() {
      return (Entry<K, V>) super.delegate();
    }

     public boolean equals(Object obj) {
      synchronized (mutex) {
        return delegate().equals(obj);
      }
    }

     public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

     public K getKey() {
      synchronized (mutex) {
        return delegate().getKey();
      }
    }

     public V getValue() {
      synchronized (mutex) {
        return delegate().getValue();
      }
    }

     public V setValue(V value) {
      synchronized (mutex) {
        return delegate().setValue(value);
      }
    }

    private static final long serialVersionUID = 0;
  }
}
