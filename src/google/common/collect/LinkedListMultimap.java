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

import static google.common.base.Preconditions.checkArgument;
import static google.common.base.Preconditions.checkState;
import static java.util.Collections.unmodifiableList;

import google.common.annotations.GwtCompatible;
import google.common.annotations.GwtIncompatible;
import google.common.base.Objects;
import google.common.base.Preconditions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An implementation of {@code ListMultimap} that supports deterministic
 * iteration order for both keys and values. The iteration order is preserved
 * across non-distinct key values. For example, for the following multimap
 * definition: <pre>   {@code
 *
 *   Multimap<K, V> multimap = LinkedListMultimap.create();
 *   multimap.put(key1, foo);
 *   multimap.put(key2, bar);
 *   multimap.put(key1, baz);}</pre>
 *
 * ... the iteration order for {@link #keys()} is {@code [key1, key2, key1]},
 * and similarly for {@link #entries()}. Unlike {@link google.common.collect.LinkedHashMultimap}, the
 * iteration order is kept consistent between keys, entries and values. For
 * example, calling: <pre>   {@code
 *
 *   map.remove(key1, foo);}</pre>
 *
 * changes the entries iteration order to {@code [key2=bar, key1=baz]} and the
 * key iteration order to {@code [key2, key1]}. The {@link #entries()} iterator
 * returns mutable map entries, and {@link #replaceValues} attempts to preserve
 * iteration order as much as possible.
 *
 * <p>The collections returned by {@link #keySet()} and {@link #asMap} iterate
 * through the keys in the order they were first added to the multimap.
 * Similarly, {@link #get}, {@link #removeAll}, and {@link #replaceValues}
 * return collections that iterate through the values in the order they were
 * added. The collections generated by {@link #entries()}, {@link #keys()}, and
 * {@link #values} iterate across the key-value mappings in the order they were
 * added to the multimap.
 *
 * <p>The {@link #values()} and {@link #entries()} methods both return a
 * {@code List}, instead of the {@code Collection} specified by the {@link
 * ListMultimap} interface.
 *
 * <p>The methods {@link #get}, {@link #keySet()}, {@link #keys()},
 * {@link #values}, {@link #entries()}, and {@link #asMap} return collections
 * that are views of the multimap. If the multimap is modified while an
 * iteration over any of those collections is in progress, except through the
 * iterator's methods, the results of the iteration are undefined.
 *
 * <p>Keys and values may be null. All optional multimap methods are supported,
 * and all returned views are modifiable.
 *
 * <p>This class is not threadsafe when any concurrent operations update the
 * multimap. Concurrent read operations will work correctly. To allow concurrent
 * update operations, wrap your multimap with a call to {@link
 * Multimaps#synchronizedListMultimap}.
 *
 * <p>See the Guava User Guide article on <a href=
 * "http://code.google.com/p/guava-libraries/wiki/NewCollectionTypesExplained#Multimap">
 * {@code Multimap}</a>.
 *
 * @author Mike Bostock
 * @since 2.0 (imported from Google Collections Library)
 */
@GwtCompatible(serializable = true, emulated = true)
public class LinkedListMultimap<K, V>
    implements ListMultimap<K, V>, Serializable {
  /*
   * Order is maintained using a linked list containing all key-value pairs. In
   * addition, a series of disjoint linked lists of "siblings", each containing
   * the values for a specific key, is used to implement {@link
   * ValueForKeyIterator} in constant time.
   */

  private static final class Node<K, V> {
    final K key;
    V value;
    Node<K, V> next; // the next node (with any key)
    Node<K, V> previous; // the previous node (with any key)
    Node<K, V> nextSibling; // the next node with the same key
    Node<K, V> previousSibling; // the previous node with the same key

    Node(@Nullable K key, @Nullable V value) {
      this.key = key;
      this.value = value;
    }

     public String toString() {
      return key + "=" + value;
    }
  }

  private transient Node<K, V> head; // the head for all keys
  private transient Node<K, V> tail; // the tail for all keys
  private transient Multiset<K> keyCount; // the number of values for each key
  private transient Map<K, Node<K, V>> keyToKeyHead; // the head for a given key
  private transient Map<K, Node<K, V>> keyToKeyTail; // the tail for a given key

  /**
   * Creates a new, empty {@code LinkedListMultimap} with the default initial
   * capacity.
   */
  public static <K, V> LinkedListMultimap<K, V> create() {
    return new LinkedListMultimap<K, V>();
  }

  /**
   * Constructs an empty {@code LinkedListMultimap} with enough capacity to hold
   * the specified number of keys without rehashing.
   *
   * @param expectedKeys the expected number of distinct keys
   * @throws IllegalArgumentException if {@code expectedKeys} is negative
   */
  public static <K, V> LinkedListMultimap<K, V> create(int expectedKeys) {
    return new LinkedListMultimap<K, V>(expectedKeys);
  }

  /**
   * Constructs a {@code LinkedListMultimap} with the same mappings as the
   * specified {@code Multimap}. The new multimap has the same
   * {@link Multimap#entries()} iteration order as the input multimap.
   *
   * @param multimap the multimap whose contents are copied to this multimap
   */
  public static <K, V> LinkedListMultimap<K, V> create(
      Multimap<? extends K, ? extends V> multimap) {
    return new LinkedListMultimap<K, V>(multimap);
  }

  LinkedListMultimap() {
    keyCount = LinkedHashMultiset.create();
    keyToKeyHead = Maps.newHashMap();
    keyToKeyTail = Maps.newHashMap();
  }

  private LinkedListMultimap(int expectedKeys) {
    keyCount = LinkedHashMultiset.create(expectedKeys);
    keyToKeyHead = Maps.newHashMapWithExpectedSize(expectedKeys);
    keyToKeyTail = Maps.newHashMapWithExpectedSize(expectedKeys);
  }

  private LinkedListMultimap(Multimap<? extends K, ? extends V> multimap) {
    this(multimap.keySet().size());
    putAll(multimap);
  }

  /**
   * Adds a new node for the specified key-value pair before the specified
   * {@code nextSibling} element, or at the end of the list if {@code
   * nextSibling} is null. Note: if {@code nextSibling} is specified, it MUST be
   * for an node for the same {@code key}!
   */
  private Node<K, V> addNode(
      @Nullable K key, @Nullable V value, @Nullable Node<K, V> nextSibling) {
    Node<K, V> node = new Node<K, V>(key, value);
    if (head == null) { // empty list
      head = tail = node;
      keyToKeyHead.put(key, node);
      keyToKeyTail.put(key, node);
    } else if (nextSibling == null) { // non-empty list, add to tail
      tail.next = node;
      node.previous = tail;
      Node<K, V> keyTail = keyToKeyTail.get(key);
      if (keyTail == null) { // first for this key
        keyToKeyHead.put(key, node);
      } else {
        keyTail.nextSibling = node;
        node.previousSibling = keyTail;
      }
      keyToKeyTail.put(key, node);
      tail = node;
    } else { // non-empty list, insert before nextSibling
      node.previous = nextSibling.previous;
      node.previousSibling = nextSibling.previousSibling;
      node.next = nextSibling;
      node.nextSibling = nextSibling;
      if (nextSibling.previousSibling == null) { // nextSibling was key head
        keyToKeyHead.put(key, node);
      } else {
        nextSibling.previousSibling.nextSibling = node;
      }
      if (nextSibling.previous == null) { // nextSibling was head
        head = node;
      } else {
        nextSibling.previous.next = node;
      }
      nextSibling.previous = node;
      nextSibling.previousSibling = node;
    }
    keyCount.add(key);
    return node;
  }

  /**
   * Removes the specified node from the linked list. This method is only
   * intended to be used from the {@code Iterator} classes. See also {@link
   * google.common.collect.LinkedListMultimap#removeAllNodes(Object)}.
   */
  private void removeNode(Node<K, V> node) {
    if (node.previous != null) {
      node.previous.next = node.next;
    } else { // node was head
      head = node.next;
    }
    if (node.next != null) {
      node.next.previous = node.previous;
    } else { // node was tail
      tail = node.previous;
    }
    if (node.previousSibling != null) {
      node.previousSibling.nextSibling = node.nextSibling;
    } else if (node.nextSibling != null) { // node was key head
      keyToKeyHead.put(node.key, node.nextSibling);
    } else {
      keyToKeyHead.remove(node.key); // don't leak a key-null entry
    }
    if (node.nextSibling != null) {
      node.nextSibling.previousSibling = node.previousSibling;
    } else if (node.previousSibling != null) { // node was key tail
      keyToKeyTail.put(node.key, node.previousSibling);
    } else {
      keyToKeyTail.remove(node.key); // don't leak a key-null entry
    }
    keyCount.remove(node.key);
  }

  /** Removes all nodes for the specified key. */
  private void removeAllNodes(@Nullable Object key) {
    for (Iterator<V> i = new ValueForKeyIterator(key); i.hasNext();) {
      i.next();
      i.remove();
    }
  }

  /** Helper method for verifying that an iterator element is present. */
  private static void checkElement(@Nullable Object node) {
    if (node == null) {
      throw new NoSuchElementException();
    }
  }

  /** An {@code Iterator} over all nodes. */
  private class NodeIterator implements ListIterator<Node<K, V>> {
    int nextIndex;
    Node<K, V> next;
    Node<K, V> current;
    Node<K, V> previous;

    NodeIterator() {
      next = head;
    }
    NodeIterator(int index) {
      int size = size();
      Preconditions.checkPositionIndex(index, size);
      if (index >= (size / 2)) {
        previous = tail;
        nextIndex = size;
        while (index++ < size) {
          previous();
        }
      } else {
        next = head;
        while (index-- > 0) {
          next();
        }
      }
      current = null;
    }
    
    public boolean hasNext() {
      return next != null;
    }
    
    public Node<K, V> next() {
      checkElement(next);
      previous = current = next;
      next = next.next;
      nextIndex++;
      return current;
    }
    
    public void remove() {
      checkState(current != null);
      if (current != next) { // after call to next()
        previous = current.previous;
        nextIndex--;
      } else { // after call to previous()
        next = current.next;
      }
      removeNode(current);
      current = null;
    }
    
    public boolean hasPrevious() {
      return previous != null;
    }
    
    public Node<K, V> previous() {
      checkElement(previous);
      next = current = previous;
      previous = previous.previous;
      nextIndex--;
      return current;
    }
    
    public int nextIndex() {
      return nextIndex;
    }
    
    public int previousIndex() {
      return nextIndex - 1;
    }
    
    public void set(Node<K, V> e) {
      throw new UnsupportedOperationException();
    }
    
    public void add(Node<K, V> e) {
      throw new UnsupportedOperationException();
    }
    void setValue(V value) {
      checkState(current != null);
      current.value = value;
    }
  }

  /** An {@code Iterator} over distinct keys in key head order. */
  private class DistinctKeyIterator implements Iterator<K> {
    final Set<K> seenKeys = Sets.<K>newHashSetWithExpectedSize(keySet().size());
    Node<K, V> next = head;
    Node<K, V> current;

    
    public boolean hasNext() {
      return next != null;
    }
    
    public K next() {
      checkElement(next);
      current = next;
      seenKeys.add(current.key);
      do { // skip ahead to next unseen key
        next = next.next;
      } while ((next != null) && !seenKeys.add(next.key));
      return current.key;
    }
    
    public void remove() {
      checkState(current != null);
      removeAllNodes(current.key);
      current = null;
    }
  }

  /** A {@code ListIterator} over values for a specified key. */
  private class ValueForKeyIterator implements ListIterator<V> {
    final Object key;
    int nextIndex;
    Node<K, V> next;
    Node<K, V> current;
    Node<K, V> previous;

    /** Constructs a new iterator over all values for the specified key. */
    ValueForKeyIterator(@Nullable Object key) {
      this.key = key;
      next = keyToKeyHead.get(key);
    }

    /**
     * Constructs a new iterator over all values for the specified key starting
     * at the specified index. This constructor is optimized so that it starts
     * at either the head or the tail, depending on which is closer to the
     * specified index. This allows adds to the tail to be done in constant
     * time.
     *
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public ValueForKeyIterator(@Nullable Object key, int index) {
      int size = keyCount.count(key);
      Preconditions.checkPositionIndex(index, size);
      if (index >= (size / 2)) {
        previous = keyToKeyTail.get(key);
        nextIndex = size;
        while (index++ < size) {
          previous();
        }
      } else {
        next = keyToKeyHead.get(key);
        while (index-- > 0) {
          next();
        }
      }
      this.key = key;
      current = null;
    }

    
    public boolean hasNext() {
      return next != null;
    }

    
    public V next() {
      checkElement(next);
      previous = current = next;
      next = next.nextSibling;
      nextIndex++;
      return current.value;
    }

    
    public boolean hasPrevious() {
      return previous != null;
    }

    
    public V previous() {
      checkElement(previous);
      next = current = previous;
      previous = previous.previousSibling;
      nextIndex--;
      return current.value;
    }

    
    public int nextIndex() {
      return nextIndex;
    }

    
    public int previousIndex() {
      return nextIndex - 1;
    }

    
    public void remove() {
      checkState(current != null);
      if (current != next) { // after call to next()
        previous = current.previousSibling;
        nextIndex--;
      } else { // after call to previous()
        next = current.nextSibling;
      }
      removeNode(current);
      current = null;
    }

    
    public void set(V value) {
      checkState(current != null);
      current.value = value;
    }

    
    @SuppressWarnings("unchecked")
    public void add(V value) {
      previous = addNode((K) key, value, next);
      nextIndex++;
      current = null;
    }
  }

  // Query Operations

  
  public int size() {
    return keyCount.size();
  }

  
  public boolean isEmpty() {
    return head == null;
  }

  
  public boolean containsKey(@Nullable Object key) {
    return keyToKeyHead.containsKey(key);
  }

  
  public boolean containsValue(@Nullable Object value) {
    for (Iterator<Node<K, V>> i = new NodeIterator(); i.hasNext();) {
      if (Objects.equal(i.next().value, value)) {
        return true;
      }
    }
    return false;
  }

  
  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    for (Iterator<V> i = new ValueForKeyIterator(key); i.hasNext();) {
      if (Objects.equal(i.next(), value)) {
        return true;
      }
    }
    return false;
  }

  // Modification Operations

  /**
   * Stores a key-value pair in the multimap.
   *
   * @param key key to store in the multimap
   * @param value value to store in the multimap
   * @return {@code true} always
   */
  
  public boolean put(@Nullable K key, @Nullable V value) {
    addNode(key, value, null);
    return true;
  }

  
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    Iterator<V> values = new ValueForKeyIterator(key);
    while (values.hasNext()) {
      if (Objects.equal(values.next(), value)) {
        values.remove();
        return true;
      }
    }
    return false;
  }

  // Bulk Operations

  
  public boolean putAll(@Nullable K key, Iterable<? extends V> values) {
    boolean changed = false;
    for (V value : values) {
      changed |= put(key, value);
    }
    return changed;
  }

  
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    boolean changed = false;
    for (Entry<? extends K, ? extends V> entry : multimap.entries()) {
      changed |= put(entry.getKey(), entry.getValue());
    }
    return changed;
  }

  /**
   * {@inheritDoc}
   *
   * <p>If any entries for the specified {@code key} already exist in the
   * multimap, their values are changed in-place without affecting the iteration
   * order.
   *
   * <p>The returned list is immutable and implements
   * {@link java.util.RandomAccess}.
   */
  
  public List<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
    List<V> oldValues = getCopy(key);
    ListIterator<V> keyValues = new ValueForKeyIterator(key);
    Iterator<? extends V> newValues = values.iterator();

    // Replace existing values, if any.
    while (keyValues.hasNext() && newValues.hasNext()) {
      keyValues.next();
      keyValues.set(newValues.next());
    }

    // Remove remaining old values, if any.
    while (keyValues.hasNext()) {
      keyValues.next();
      keyValues.remove();
    }

    // Add remaining new values, if any.
    while (newValues.hasNext()) {
      keyValues.add(newValues.next());
    }

    return oldValues;
  }

  private List<V> getCopy(@Nullable Object key) {
    return unmodifiableList(Lists.newArrayList(new ValueForKeyIterator(key)));
  }

  /**
   * {@inheritDoc}
   *
   * <p>The returned list is immutable and implements
   * {@link java.util.RandomAccess}.
   */
  
  public List<V> removeAll(@Nullable Object key) {
    List<V> oldValues = getCopy(key);
    removeAllNodes(key);
    return oldValues;
  }

  
  public void clear() {
    head = null;
    tail = null;
    keyCount.clear();
    keyToKeyHead.clear();
    keyToKeyTail.clear();
  }

  // Views

  /**
   * {@inheritDoc}
   *
   * <p>If the multimap is modified while an iteration over the list is in
   * progress (except through the iterator's own {@code add}, {@code set} or
   * {@code remove} operations) the results of the iteration are undefined.
   *
   * <p>The returned list is not serializable and does not have random access.
   */
  
  public List<V> get(final @Nullable K key) {
    return new AbstractSequentialList<V>() {
       public int size() {
        return keyCount.count(key);
      }
       public ListIterator<V> listIterator(int index) {
        return new ValueForKeyIterator(key, index);
      }
       public boolean removeAll(Collection<?> c) {
        return Iterators.removeAll(iterator(), c);
      }
       public boolean retainAll(Collection<?> c) {
        return Iterators.retainAll(iterator(), c);
      }
    };
  }

  private transient Set<K> keySet;

  
  public Set<K> keySet() {
    Set<K> result = keySet;
    if (result == null) {
      keySet = result = new Sets.ImprovedAbstractSet<K>() {
         public int size() {
          return keyCount.elementSet().size();
        }
         public Iterator<K> iterator() {
          return new DistinctKeyIterator();
        }
         public boolean contains(Object key) { // for performance
          return containsKey(key);
        }
        
        public boolean remove(Object o) { // for performance
          return !LinkedListMultimap.this.removeAll(o).isEmpty();
        }
      };
    }
    return result;
  }

  private transient Multiset<K> keys;

  
  public Multiset<K> keys() {
    Multiset<K> result = keys;
    if (result == null) {
      keys = result = new MultisetView();
    }
    return result;
  }

  private class MultisetView extends AbstractMultiset<K> {
    
    public int size() {
      return keyCount.size();
    }

    
    public int count(Object element) {
      return keyCount.count(element);
    }

    
    Iterator<Entry<K>> entryIterator() {
      return new TransformedIterator<K, Entry<K>>(new DistinctKeyIterator()) {
        
        Entry<K> transform(final K key) {
          return new Multisets.AbstractEntry<K>() {
            
            public K getElement() {
              return key;
            }

            
            public int getCount() {
              return keyCount.count(key);
            }
          };
        }
      };
    }

    
    int distinctElements() {
      return elementSet().size();
    }

     public Iterator<K> iterator() {
      return new TransformedIterator<Node<K, V>, K>(new NodeIterator()) {
        
        K transform(Node<K, V> node) {
          return node.key;
        }
      };
    }

    
    public int remove(@Nullable Object key, int occurrences) {
      checkArgument(occurrences >= 0);
      int oldCount = count(key);
      Iterator<V> values = new ValueForKeyIterator(key);
      while ((occurrences-- > 0) && values.hasNext()) {
        values.next();
        values.remove();
      }
      return oldCount;
    }

    
    public Set<K> elementSet() {
      return keySet();
    }

     public boolean equals(@Nullable Object object) {
      return keyCount.equals(object);
    }

     public int hashCode() {
      return keyCount.hashCode();
    }

     public String toString() {
      return keyCount.toString(); // XXX observe order?
    }
  }

  private transient List<V> valuesList;

  /**
   * {@inheritDoc}
   *
   * <p>The iterator generated by the returned collection traverses the values
   * in the order they were added to the multimap. Because the values may have
   * duplicates and follow the insertion ordering, this method returns a {@link
   * java.util.List}, instead of the {@link java.util.Collection} specified in the {@link
   * ListMultimap} interface.
   */
  
  public List<V> values() {
    List<V> result = valuesList;
    if (result == null) {
      valuesList = result = new AbstractSequentialList<V>() {
         public int size() {
          return keyCount.size();
        }
        
        public ListIterator<V> listIterator(int index) {
          final NodeIterator nodes = new NodeIterator(index);
          return new TransformedListIterator<Node<K, V>, V>(nodes) {
            
            V transform(Node<K, V> node) {
              return node.value;
            }

            
            public void set(V value) {
              nodes.setValue(value);
            }
          };
        }
      };
    }
    return result;
  }

  private static <K, V> Entry<K, V> createEntry(final Node<K, V> node) {
    return new AbstractMapEntry<K, V>() {
       public K getKey() {
        return node.key;
      }
       public V getValue() {
        return node.value;
      }
       public V setValue(V value) {
        V oldValue = node.value;
        node.value = value;
        return oldValue;
      }
    };
  }

  private transient List<Entry<K, V>> entries;

  /**
   * {@inheritDoc}
   *
   * <p>The iterator generated by the returned collection traverses the entries
   * in the order they were added to the multimap. Because the entries may have
   * duplicates and follow the insertion ordering, this method returns a {@link
   * java.util.List}, instead of the {@link java.util.Collection} specified in the {@link
   * ListMultimap} interface.
   *
   * <p>An entry's {@link java.util.Map.Entry#getKey} method always returns the same key,
   * regardless of what happens subsequently. As long as the corresponding
   * key-value mapping is not removed from the multimap, {@link java.util.Map.Entry#getValue}
   * returns the value from the multimap, which may change over time, and {@link
   * java.util.Map.Entry#setValue} modifies that value. Removing the mapping from the
   * multimap does not alter the value returned by {@code getValue()}, though a
   * subsequent {@code setValue()} call won't update the multimap but will lead
   * to a revised value being returned by {@code getValue()}.
   */
  
  public List<Entry<K, V>> entries() {
    List<Entry<K, V>> result = entries;
    if (result == null) {
      entries = result = new AbstractSequentialList<Entry<K, V>>() {
         public int size() {
          return keyCount.size();
        }

         public ListIterator<Entry<K, V>> listIterator(int index) {
          return new TransformedListIterator<Node<K, V>, Entry<K, V>>(new NodeIterator(index)) {
            
            Entry<K, V> transform(Node<K, V> node) {
              return createEntry(node);
            }
          };
        }
      };
    }
    return result;
  }

  private transient Map<K, Collection<V>> map;

  
  public Map<K, Collection<V>> asMap() {
    Map<K, Collection<V>> result = map;
    if (result == null) {
      map = result = new Multimaps.AsMap<K, V>() {
        
        public int size() {
          return keyCount.elementSet().size();
        }

        
        Multimap<K, V> multimap() {
          return LinkedListMultimap.this;
        }

        
        Iterator<Entry<K, Collection<V>>> entryIterator() {
          return new TransformedIterator<K, Entry<K, Collection<V>>>(new DistinctKeyIterator()) {
            
            Entry<K, Collection<V>> transform(final K key) {
              return new AbstractMapEntry<K, Collection<V>>() {
                 public K getKey() {
                  return key;
                }

                 public Collection<V> getValue() {
                  return LinkedListMultimap.this.get(key);
                }
              };
            }
          };
        }
      };
    }

    return result;
  }

  // Comparison and hashing

  /**
   * Compares the specified object to this multimap for equality.
   *
   * <p>Two {@code ListMultimap} instances are equal if, for each key, they
   * contain the same values in the same order. If the value orderings disagree,
   * the multimaps will not be considered equal.
   */
   public boolean equals(@Nullable Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof Multimap) {
      Multimap<?, ?> that = (Multimap<?, ?>) other;
      return this.asMap().equals(that.asMap());
    }
    return false;
  }

  /**
   * Returns the hash code for this multimap.
   *
   * <p>The hash code of a multimap is defined as the hash code of the map view,
   * as returned by {@link Multimap#asMap}.
   */
   public int hashCode() {
    return asMap().hashCode();
  }

  /**
   * Returns a string representation of the multimap, generated by calling
   * {@code toString} on the map returned by {@link Multimap#asMap}.
   *
   * @return a string representation of the multimap
   */
   public String toString() {
    return asMap().toString();
  }

  /**
   * @serialData the number of distinct keys, and then for each distinct key:
   *     the first key, the number of values for that key, and the key's values,
   *     followed by successive keys and values from the entries() ordering
   */
  @GwtIncompatible("java.io.ObjectOutputStream")
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeInt(size());
    for (Entry<K, V> entry : entries()) {
      stream.writeObject(entry.getKey());
      stream.writeObject(entry.getValue());
    }
  }

  @GwtIncompatible("java.io.ObjectInputStream")
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    keyCount = LinkedHashMultiset.create();
    keyToKeyHead = Maps.newHashMap();
    keyToKeyTail = Maps.newHashMap();
    int size = stream.readInt();
    for (int i = 0; i < size; i++) {
      @SuppressWarnings("unchecked") // reading data stored by writeObject
      K key = (K) stream.readObject();
      @SuppressWarnings("unchecked") // reading data stored by writeObject
      V value = (V) stream.readObject();
      put(key, value);
    }
  }

  @GwtIncompatible("java serialization not supported")
  private static final long serialVersionUID = 0;
}
