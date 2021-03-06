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

import static google.common.base.Preconditions.checkNotNull;
import static google.common.collect.Maps.safeContainsKey;
import static google.common.collect.Maps.safeGet;

import google.common.annotations.GwtCompatible;
import google.common.base.Predicate;
import google.common.base.Predicates;
import google.common.base.Supplier;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * {@link Table} implementation backed by a map that associates row keys with
 * column key / value secondary maps. This class provides rapid access to
 * records by the row key alone or by both keys, but not by just the column key.
 *
 * <p>The views returned by {@link #column}, {@link #columnKeySet()}, and {@link
 * #columnMap()} have iterators that don't support {@code remove()}. Otherwise,
 * all optional operations are supported. Null row keys, columns keys, and
 * values are not supported.
 *
 * <p>Lookups by row key are often faster than lookups by column key, because
 * the data is stored in a {@code Map<R, Map<C, V>>}. A method call like {@code
 * column(columnKey).get(rowKey)} still runs quickly, since the row key is
 * provided. However, {@code column(columnKey).size()} takes longer, since an
 * iteration across all row keys occurs.
 *
 * <p>Note that this implementation is not synchronized. If multiple threads
 * access this table concurrently and one of the threads modifies the table, it
 * must be synchronized externally.
 *
 * @author Jared Levy
 */
@GwtCompatible
class StandardTable<R, C, V> implements Table<R, C, V>, Serializable {
  @GwtTransient final Map<R, Map<C, V>> backingMap;
  @GwtTransient final Supplier<? extends Map<C, V>> factory;

  StandardTable(Map<R, Map<C, V>> backingMap,
      Supplier<? extends Map<C, V>> factory) {
    this.backingMap = backingMap;
    this.factory = factory;
  }

  // Accessors

   public boolean contains(
      @Nullable Object rowKey, @Nullable Object columnKey) {
    if ((rowKey == null) || (columnKey == null)) {
      return false;
    }
    Map<C, V> map = safeGet(backingMap, rowKey);
    return map != null && safeContainsKey(map, columnKey);
  }

   public boolean containsColumn(@Nullable Object columnKey) {
    if (columnKey == null) {
      return false;
    }
    for (Map<C, V> map : backingMap.values()) {
      if (safeContainsKey(map, columnKey)) {
        return true;
      }
    }
    return false;
  }

   public boolean containsRow(@Nullable Object rowKey) {
    return rowKey != null && safeContainsKey(backingMap, rowKey);
  }

   public boolean containsValue(@Nullable Object value) {
    if (value == null) {
      return false;
    }
    for (Map<C, V> map : backingMap.values()) {
      if (map.containsValue(value)) {
        return true;
      }
    }
    return false;
  }

   public V get(@Nullable Object rowKey, @Nullable Object columnKey) {
    if ((rowKey == null) || (columnKey == null)) {
      return null;
    }
    Map<C, V> map = safeGet(backingMap, rowKey);
    return map == null ? null : safeGet(map, columnKey);
  }

   public boolean isEmpty() {
    return backingMap.isEmpty();
  }

   public int size() {
    int size = 0;
    for (Map<C, V> map : backingMap.values()) {
      size += map.size();
    }
    return size;
  }

   public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Table) {
      Table<?, ?, ?> other = (Table<?, ?, ?>) obj;
      return cellSet().equals(other.cellSet());
    }
    return false;
  }

   public int hashCode() {
    return cellSet().hashCode();
  }

  /**
   * Returns the string representation {@code rowMap().toString()}.
   */
   public String toString() {
    return rowMap().toString();
  }

  // Mutators

   public void clear() {
    backingMap.clear();
  }

  private Map<C, V> getOrCreate(R rowKey) {
    Map<C, V> map = backingMap.get(rowKey);
    if (map == null) {
      map = factory.get();
      backingMap.put(rowKey, map);
    }
    return map;
  }

   public V put(R rowKey, C columnKey, V value) {
    checkNotNull(rowKey);
    checkNotNull(columnKey);
    checkNotNull(value);
    return getOrCreate(rowKey).put(columnKey, value);
  }

   public void putAll(
      Table<? extends R, ? extends C, ? extends V> table) {
    for (Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
      put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }
  }

   public V remove(
      @Nullable Object rowKey, @Nullable Object columnKey) {
    if ((rowKey == null) || (columnKey == null)) {
      return null;
    }
    Map<C, V> map = safeGet(backingMap, rowKey);
    if (map == null) {
      return null;
    }
    V value = map.remove(columnKey);
    if (map.isEmpty()) {
      backingMap.remove(rowKey);
    }
    return value;
  }

  private Map<R, V> removeColumn(Object column) {
    Map<R, V> output = new LinkedHashMap<R, V>();
    Iterator<Entry<R, Map<C, V>>> iterator
        = backingMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<R, Map<C, V>> entry = iterator.next();
      V value = entry.getValue().remove(column);
      if (value != null) {
        output.put(entry.getKey(), value);
        if (entry.getValue().isEmpty()) {
          iterator.remove();
        }
      }
    }
    return output;
  }

  private boolean containsMapping(
      Object rowKey, Object columnKey, Object value) {
    return value != null && value.equals(get(rowKey, columnKey));
  }

  /** Remove a row key / column key / value mapping, if present. */
  private boolean removeMapping(Object rowKey, Object columnKey, Object value) {
    if (containsMapping(rowKey, columnKey, value)) {
      remove(rowKey, columnKey);
      return true;
    }
    return false;
  }

  // Views

  /**
   * Abstract collection whose {@code isEmpty()} returns whether the table is
   * empty and whose {@code clear()} clears all table mappings.
   */
  private abstract class TableCollection<T> extends AbstractCollection<T> {
     public boolean isEmpty() {
      return backingMap.isEmpty();
    }

     public void clear() {
      backingMap.clear();
    }
  }

  /**
   * Abstract set whose {@code isEmpty()} returns whether the table is empty and
   * whose {@code clear()} clears all table mappings.
   */
  private abstract class TableSet<T> extends AbstractSet<T> {
     public boolean isEmpty() {
      return backingMap.isEmpty();
    }

     public void clear() {
      backingMap.clear();
    }
  }

  private transient CellSet cellSet;

  /**
   * {@inheritDoc}
   *
   * <p>The set's iterator traverses the mappings for the first row, the
   * mappings for the second row, and so on.
   *
   * <p>Each cell is an immutable snapshot of a row key / column key / value
   * mapping, taken at the time the cell is returned by a method call to the
   * set or its iterator.
   */
   public Set<Cell<R, C, V>> cellSet() {
    CellSet result = cellSet;
    return (result == null) ? cellSet = new CellSet() : result;
  }

  private class CellSet extends TableSet<Cell<R, C, V>> {
     public Iterator<Cell<R, C, V>> iterator() {
      return new CellIterator();
    }

     public int size() {
      return StandardTable.this.size();
    }

     public boolean contains(Object obj) {
      if (obj instanceof Cell) {
        Cell<?, ?, ?> cell = (Cell<?, ?, ?>) obj;
        return containsMapping(
            cell.getRowKey(), cell.getColumnKey(), cell.getValue());
      }
      return false;
    }

     public boolean remove(Object obj) {
      if (obj instanceof Cell) {
        Cell<?, ?, ?> cell = (Cell<?, ?, ?>) obj;
        return removeMapping(
            cell.getRowKey(), cell.getColumnKey(), cell.getValue());
      }
      return false;
    }
  }

  private class CellIterator implements Iterator<Cell<R, C, V>> {
    final Iterator<Entry<R, Map<C, V>>> rowIterator
        = backingMap.entrySet().iterator();
    Entry<R, Map<C, V>> rowEntry;
    Iterator<Entry<C, V>> columnIterator
        = Iterators.emptyModifiableIterator();

     public boolean hasNext() {
      return rowIterator.hasNext() || columnIterator.hasNext();
    }

     public Cell<R, C, V> next() {
      if (!columnIterator.hasNext()) {
        rowEntry = rowIterator.next();
        columnIterator = rowEntry.getValue().entrySet().iterator();
      }
      Entry<C, V> columnEntry = columnIterator.next();
      return Tables.immutableCell(
          rowEntry.getKey(), columnEntry.getKey(), columnEntry.getValue());
    }

     public void remove() {
      columnIterator.remove();
      if (rowEntry.getValue().isEmpty()) {
        rowIterator.remove();
      }
    }
  }

   public Map<C, V> row(R rowKey) {
    return new Row(rowKey);
  }

  class Row extends AbstractMap<C, V> {
    final R rowKey;

    Row(R rowKey) {
      this.rowKey = checkNotNull(rowKey);
    }

    Map<C, V> backingRowMap;

    Map<C, V> backingRowMap() {
      return (backingRowMap == null
              || (backingRowMap.isEmpty() && backingMap.containsKey(rowKey)))
          ? backingRowMap = computeBackingRowMap()
          : backingRowMap;
    }

    Map<C, V> computeBackingRowMap() {
      return backingMap.get(rowKey);
    }

    // Call this every time we perform a removal.
    void maintainEmptyInvariant() {
      if (backingRowMap() != null && backingRowMap.isEmpty()) {
        backingMap.remove(rowKey);
        backingRowMap = null;
      }
    }

    
    public boolean containsKey(Object key) {
      Map<C, V> backingRowMap = backingRowMap();
      return (key != null && backingRowMap != null)
          && Maps.safeContainsKey(backingRowMap, key);
    }

    
    public V get(Object key) {
      Map<C, V> backingRowMap = backingRowMap();
      return (key != null && backingRowMap != null)
          ? Maps.safeGet(backingRowMap, key)
          : null;
    }

    
    public V put(C key, V value) {
      checkNotNull(key);
      checkNotNull(value);
      if (backingRowMap != null && !backingRowMap.isEmpty()) {
        return backingRowMap.put(key, value);
      }
      return StandardTable.this.put(rowKey, key, value);
    }

    
    public V remove(Object key) {
      try {
        Map<C, V> backingRowMap = backingRowMap();
        if (backingRowMap == null) {
          return null;
        }
        V result = backingRowMap.remove(key);
        maintainEmptyInvariant();
        return result;
      } catch (ClassCastException e) {
        return null;
      }
    }

    
    public void clear() {
      Map<C, V> backingRowMap = backingRowMap();
      if (backingRowMap != null) {
        backingRowMap.clear();
      }
      maintainEmptyInvariant();
    }

    Set<C> keySet;

    
    public Set<C> keySet() {
      Set<C> result = keySet;
      if (result == null) {
        return keySet = new Maps.KeySet<C, V>() {
          
          Map<C, V> map() {
            return Row.this;
          }
        };
      }
      return result;
    }

    Set<Entry<C, V>> entrySet;

    
    public Set<Entry<C, V>> entrySet() {
      Set<Entry<C, V>> result = entrySet;
      if (result == null) {
        return entrySet = new RowEntrySet();
      }
      return result;
    }

    private class RowEntrySet extends Maps.EntrySet<C, V> {
      
      Map<C, V> map() {
        return Row.this;
      }

      
      public int size() {
        Map<C, V> map = backingRowMap();
        return (map == null) ? 0 : map.size();
      }

      
      public Iterator<Entry<C, V>> iterator() {
        final Map<C, V> map = backingRowMap();
        if (map == null) {
          return Iterators.emptyModifiableIterator();
        }
        final Iterator<Entry<C, V>> iterator = map.entrySet().iterator();
        return new Iterator<Entry<C, V>>() {
           public boolean hasNext() {
            return iterator.hasNext();
          }
           public Entry<C, V> next() {
            final Entry<C, V> entry = iterator.next();
            return new ForwardingMapEntry<C, V>() {
               protected Entry<C, V> delegate() {
                return entry;
              }
               public V setValue(V value) {
                return super.setValue(checkNotNull(value));
              }
              
              public boolean equals(Object object) {
                // TODO(user): identify why this affects GWT tests
                return standardEquals(object);
              }
            };
          }

          
          public void remove() {
            iterator.remove();
            maintainEmptyInvariant();
          }
        };
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>The returned map's views have iterators that don't support
   * {@code remove()}.
   */
   public Map<R, V> column(C columnKey) {
    return new Column(columnKey);
  }

  private class Column extends Maps.ImprovedAbstractMap<R, V> {
    final C columnKey;

    Column(C columnKey) {
      this.columnKey = checkNotNull(columnKey);
    }

     public V put(R key, V value) {
      return StandardTable.this.put(key, columnKey, value);
    }

     public V get(Object key) {
      return StandardTable.this.get(key, columnKey);
    }

     public boolean containsKey(Object key) {
      return StandardTable.this.contains(key, columnKey);
    }

     public V remove(Object key) {
      return StandardTable.this.remove(key, columnKey);
    }

     public Set<Entry<R, V>> createEntrySet() {
      return new EntrySet();
    }

    Values columnValues;

     public Collection<V> values() {
      Values result = columnValues;
      return (result == null) ? columnValues = new Values() : result;
    }

    /**
     * Removes all {@code Column} mappings whose row key and value satisfy the
     * given predicate.
     */
    boolean removePredicate(Predicate<? super Entry<R, V>> predicate) {
      boolean changed = false;
      Iterator<Entry<R, Map<C, V>>> iterator
          = backingMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<R, Map<C, V>> entry = iterator.next();
        Map<C, V> map = entry.getValue();
        V value = map.get(columnKey);
        if (value != null
            && predicate.apply(
                new ImmutableEntry<R, V>(entry.getKey(), value))) {
          map.remove(columnKey);
          changed = true;
          if (map.isEmpty()) {
            iterator.remove();
          }
        }
      }
      return changed;
    }

    class EntrySet extends Sets.ImprovedAbstractSet<Entry<R, V>> {
       public Iterator<Entry<R, V>> iterator() {
        return new EntrySetIterator();
      }

       public int size() {
        int size = 0;
        for (Map<C, V> map : backingMap.values()) {
          if (map.containsKey(columnKey)) {
            size++;
          }
        }
        return size;
      }

       public boolean isEmpty() {
        return !containsColumn(columnKey);
      }

       public void clear() {
        Predicate<Entry<R, V>> predicate = Predicates.alwaysTrue();
        removePredicate(predicate);
      }

       public boolean contains(Object o) {
        if (o instanceof Entry) {
          Entry<?, ?> entry = (Entry<?, ?>) o;
          return containsMapping(entry.getKey(), columnKey, entry.getValue());
        }
        return false;
      }

       public boolean remove(Object obj) {
        if (obj instanceof Entry) {
          Entry<?, ?> entry = (Entry<?, ?>) obj;
          return removeMapping(entry.getKey(), columnKey, entry.getValue());
        }
        return false;
      }

       public boolean retainAll(Collection<?> c) {
        return removePredicate(Predicates.not(Predicates.in(c)));
      }
    }

    class EntrySetIterator extends AbstractIterator<Entry<R, V>> {
      final Iterator<Entry<R, Map<C, V>>> iterator
          = backingMap.entrySet().iterator();
       protected Entry<R, V> computeNext() {
        while (iterator.hasNext()) {
          final Entry<R, Map<C, V>> entry = iterator.next();
          if (entry.getValue().containsKey(columnKey)) {
            return new AbstractMapEntry<R, V>() {
               public R getKey() {
                return entry.getKey();
              }
               public V getValue() {
                return entry.getValue().get(columnKey);
              }
               public V setValue(V value) {
                return entry.getValue().put(columnKey, checkNotNull(value));
              }
            };
          }
        }
        return endOfData();
      }
    }

    KeySet keySet;

     public Set<R> keySet() {
      KeySet result = keySet;
      return result == null ? keySet = new KeySet() : result;
    }

    class KeySet extends Sets.ImprovedAbstractSet<R> {
       public Iterator<R> iterator() {
        return Maps.keyIterator(Column.this.entrySet().iterator());
      }

       public int size() {
        return entrySet().size();
      }

       public boolean isEmpty() {
        return !containsColumn(columnKey);
      }

       public boolean contains(Object obj) {
        return StandardTable.this.contains(obj, columnKey);
      }

       public boolean remove(Object obj) {
        return StandardTable.this.remove(obj, columnKey) != null;
      }

       public void clear() {
        entrySet().clear();
      }

       public boolean retainAll(final Collection<?> c) {
        checkNotNull(c);
        Predicate<Entry<R, V>> predicate = new Predicate<Entry<R, V>>() {
          
          public boolean apply(Entry<R, V> entry) {
            return !c.contains(entry.getKey());
          }
        };
        return removePredicate(predicate);
      }
    }

    class Values extends AbstractCollection<V> {
       public Iterator<V> iterator() {
        return Maps.valueIterator(Column.this.entrySet().iterator());
      }

       public int size() {
        return entrySet().size();
      }

       public boolean isEmpty() {
        return !containsColumn(columnKey);
      }

       public void clear() {
        entrySet().clear();
      }

       public boolean remove(Object obj) {
        if (obj == null) {
          return false;
        }
        Iterator<Map<C, V>> iterator = backingMap.values().iterator();
        while (iterator.hasNext()) {
          Map<C, V> map = iterator.next();
          if (map.entrySet().remove(
              new ImmutableEntry<C, Object>(columnKey, obj))) {
            if (map.isEmpty()) {
              iterator.remove();
            }
            return true;
          }
        }
        return false;
      }

       public boolean removeAll(final Collection<?> c) {
        checkNotNull(c);
        Predicate<Entry<R, V>> predicate = new Predicate<Entry<R, V>>() {
          
          public boolean apply(Entry<R, V> entry) {
            return c.contains(entry.getValue());
          }
        };
        return removePredicate(predicate);
      }

       public boolean retainAll(final Collection<?> c) {
        checkNotNull(c);
        Predicate<Entry<R, V>> predicate = new Predicate<Entry<R, V>>() {
          
          public boolean apply(Entry<R, V> entry) {
            return !c.contains(entry.getValue());
          }
        };
        return removePredicate(predicate);
      }
    }
  }

  private transient RowKeySet rowKeySet;

   public Set<R> rowKeySet() {
    Set<R> result = rowKeySet;
    return (result == null) ? rowKeySet = new RowKeySet() : result;
  }

  class RowKeySet extends TableSet<R> {
     public Iterator<R> iterator() {
      return Maps.keyIterator(rowMap().entrySet().iterator());
    }

     public int size() {
      return backingMap.size();
    }

     public boolean contains(Object obj) {
      return containsRow(obj);
    }

     public boolean remove(Object obj) {
      return (obj != null) && backingMap.remove(obj) != null;
    }
  }

  private transient Set<C> columnKeySet;

  /**
   * {@inheritDoc}
   *
   * <p>The returned set has an iterator that does not support {@code remove()}.
   *
   * <p>The set's iterator traverses the columns of the first row, the
   * columns of the second row, etc., skipping any columns that have
   * appeared previously.
   */
  
  public Set<C> columnKeySet() {
    Set<C> result = columnKeySet;
    return (result == null) ? columnKeySet = new ColumnKeySet() : result;
  }

  private class ColumnKeySet extends TableSet<C> {
     public Iterator<C> iterator() {
      return createColumnKeyIterator();
    }

     public int size() {
      return Iterators.size(iterator());
    }

     public boolean remove(Object obj) {
      if (obj == null) {
        return false;
      }
      boolean changed = false;
      Iterator<Map<C, V>> iterator = backingMap.values().iterator();
      while (iterator.hasNext()) {
        Map<C, V> map = iterator.next();
        if (map.keySet().remove(obj)) {
          changed = true;
          if (map.isEmpty()) {
            iterator.remove();
          }
        }
      }
      return changed;
    }

     public boolean removeAll(Collection<?> c) {
      checkNotNull(c);
      boolean changed = false;
      Iterator<Map<C, V>> iterator = backingMap.values().iterator();
      while (iterator.hasNext()) {
        Map<C, V> map = iterator.next();
        // map.keySet().removeAll(c) can throw a NPE when map is a TreeMap with
        // natural ordering and c contains a null.
        if (Iterators.removeAll(map.keySet().iterator(), c)) {
          changed = true;
          if (map.isEmpty()) {
            iterator.remove();
          }
        }
      }
      return changed;
    }

     public boolean retainAll(Collection<?> c) {
      checkNotNull(c);
      boolean changed = false;
      Iterator<Map<C, V>> iterator = backingMap.values().iterator();
      while (iterator.hasNext()) {
        Map<C, V> map = iterator.next();
        if (map.keySet().retainAll(c)) {
          changed = true;
          if (map.isEmpty()) {
            iterator.remove();
          }
        }
      }
      return changed;
    }

     public boolean contains(Object obj) {
      if (obj == null) {
        return false;
      }
      for (Map<C, V> map : backingMap.values()) {
        if (map.containsKey(obj)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Creates an iterator that returns each column value with duplicates
   * omitted.
   */
  Iterator<C> createColumnKeyIterator() {
    return new ColumnKeyIterator();
  }

  private class ColumnKeyIterator extends AbstractIterator<C> {
    // Use the same map type to support TreeMaps with comparators that aren't
    // consistent with equals().
    final Map<C, V> seen = factory.get();
    final Iterator<Map<C, V>> mapIterator = backingMap.values().iterator();
    Iterator<Entry<C, V>> entryIterator = Iterators.emptyIterator();

     protected C computeNext() {
      while (true) {
        if (entryIterator.hasNext()) {
          Entry<C, V> entry = entryIterator.next();
          if (!seen.containsKey(entry.getKey())) {
            seen.put(entry.getKey(), entry.getValue());
            return entry.getKey();
          }
        } else if (mapIterator.hasNext()) {
          entryIterator = mapIterator.next().entrySet().iterator();
        } else {
          return endOfData();
        }
      }
    }
  }

  private transient Values values;

  /**
   * {@inheritDoc}
   *
   * <p>The collection's iterator traverses the values for the first row,
   * the values for the second row, and so on.
   */
   public Collection<V> values() {
    Values result = values;
    return (result == null) ? values = new Values() : result;
  }

  private class Values extends TableCollection<V> {
     public Iterator<V> iterator() {
      return new TransformedIterator<Cell<R, C, V>, V>(cellSet().iterator()) {
        
        V transform(Cell<R, C, V> cell) {
          return cell.getValue();
        }
      };
    }

     public int size() {
      return StandardTable.this.size();
    }
  }

  private transient RowMap rowMap;

   public Map<R, Map<C, V>> rowMap() {
    RowMap result = rowMap;
    return (result == null) ? rowMap = new RowMap() : result;
  }

  class RowMap extends Maps.ImprovedAbstractMap<R, Map<C, V>> {
     public boolean containsKey(Object key) {
      return containsRow(key);
    }

    // performing cast only when key is in backing map and has the correct type
    @SuppressWarnings("unchecked")
     public Map<C, V> get(Object key) {
      return containsRow(key) ? row((R) key) : null;
    }

     public Set<R> keySet() {
      return rowKeySet();
    }

     public Map<C, V> remove(Object key) {
      return (key == null) ? null : backingMap.remove(key);
    }

     protected Set<Entry<R, Map<C, V>>> createEntrySet() {
      return new EntrySet();
    }

    class EntrySet extends TableSet<Entry<R, Map<C, V>>> {
       public Iterator<Entry<R, Map<C, V>>> iterator() {
        return new TransformedIterator<R, Entry<R, Map<C, V>>>(
            backingMap.keySet().iterator()) {
          
          Entry<R, Map<C, V>> transform(R rowKey) {
            return new ImmutableEntry<R, Map<C, V>>(rowKey, row(rowKey));
          }
        };
      }

       public int size() {
        return backingMap.size();
      }

       public boolean contains(Object obj) {
        if (obj instanceof Entry) {
          Entry<?, ?> entry = (Entry<?, ?>) obj;
          return entry.getKey() != null
              && entry.getValue() instanceof Map
              && Collections2.safeContains(backingMap.entrySet(), entry);
        }
        return false;
      }

       public boolean remove(Object obj) {
        if (obj instanceof Entry) {
          Entry<?, ?> entry = (Entry<?, ?>) obj;
          return entry.getKey() != null
              && entry.getValue() instanceof Map
              && backingMap.entrySet().remove(entry);
        }
        return false;
      }
    }
  }

  private transient ColumnMap columnMap;

   public Map<C, Map<R, V>> columnMap() {
    ColumnMap result = columnMap;
    return (result == null) ? columnMap = new ColumnMap() : result;
  }

  private class ColumnMap extends Maps.ImprovedAbstractMap<C, Map<R, V>> {
    // The cast to C occurs only when the key is in the map, implying that it
    // has the correct type.
    @SuppressWarnings("unchecked")
     public Map<R, V> get(Object key) {
      return containsColumn(key) ? column((C) key) : null;
    }

     public boolean containsKey(Object key) {
      return containsColumn(key);
    }

     public Map<R, V> remove(Object key) {
      return containsColumn(key) ? removeColumn(key) : null;
    }

     public Set<Entry<C, Map<R, V>>> createEntrySet() {
      return new ColumnMapEntrySet();
    }

     public Set<C> keySet() {
      return columnKeySet();
    }

    ColumnMapValues columnMapValues;

     public Collection<Map<R, V>> values() {
      ColumnMapValues result = columnMapValues;
      return
          (result == null) ? columnMapValues = new ColumnMapValues() : result;
    }

    class ColumnMapEntrySet extends TableSet<Entry<C, Map<R, V>>> {
       public Iterator<Entry<C, Map<R, V>>> iterator() {
        return new TransformedIterator<C, Entry<C, Map<R, V>>>(
            columnKeySet().iterator()) {
          
          Entry<C, Map<R, V>> transform(C columnKey) {
            return new ImmutableEntry<C, Map<R, V>>(
                columnKey, column(columnKey));
          }
        };
      }

       public int size() {
        return columnKeySet().size();
      }

       public boolean contains(Object obj) {
        if (obj instanceof Entry) {
          Entry<?, ?> entry = (Entry<?, ?>) obj;
          if (containsColumn(entry.getKey())) {
            // The cast to C occurs only when the key is in the map, implying
            // that it has the correct type.
            @SuppressWarnings("unchecked")
            C columnKey = (C) entry.getKey();
            return get(columnKey).equals(entry.getValue());
          }
        }
        return false;
      }

       public boolean remove(Object obj) {
        if (contains(obj)) {
          Entry<?, ?> entry = (Entry<?, ?>) obj;
          removeColumn(entry.getKey());
          return true;
        }
        return false;
      }

       public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object obj : c) {
          changed |= remove(obj);
        }
        return changed;
      }

       public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (C columnKey : Lists.newArrayList(columnKeySet().iterator())) {
          if (!c.contains(new ImmutableEntry<C, Map<R, V>>(
              columnKey, column(columnKey)))) {
            removeColumn(columnKey);
            changed = true;
          }
        }
        return changed;
      }
    }

    private class ColumnMapValues extends TableCollection<Map<R, V>> {
       public Iterator<Map<R, V>> iterator() {
        return Maps.valueIterator(ColumnMap.this.entrySet().iterator());
      }

       public boolean remove(Object obj) {
        for (Entry<C, Map<R, V>> entry : ColumnMap.this.entrySet()) {
          if (entry.getValue().equals(obj)) {
            removeColumn(entry.getKey());
            return true;
          }
        }
        return false;
      }

       public boolean removeAll(Collection<?> c) {
        checkNotNull(c);
        boolean changed = false;
        for (C columnKey : Lists.newArrayList(columnKeySet().iterator())) {
          if (c.contains(column(columnKey))) {
            removeColumn(columnKey);
            changed = true;
          }
        }
        return changed;
      }

       public boolean retainAll(Collection<?> c) {
        checkNotNull(c);
        boolean changed = false;
        for (C columnKey : Lists.newArrayList(columnKeySet().iterator())) {
          if (!c.contains(column(columnKey))) {
            removeColumn(columnKey);
            changed = true;
          }
        }
        return changed;
      }

       public int size() {
        return columnKeySet().size();
      }
    }
  }

  private static final long serialVersionUID = 0;
}
