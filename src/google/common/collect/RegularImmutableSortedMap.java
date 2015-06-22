/*
 * Copyright (C) 2012 The Guava Authors
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

import javax.annotation.Nullable;

/**
 * An implementation of an immutable sorted map with one or more entries.
 *
 * @author Louis Wasserman
 */
@SuppressWarnings("serial") // uses writeReplace, not default serialization
final class RegularImmutableSortedMap<K, V> extends ImmutableSortedMap<K, V> {
  private final transient RegularImmutableSortedSet<K> keySet;
  private final transient ImmutableList<V> valueList;

  RegularImmutableSortedMap(RegularImmutableSortedSet<K> keySet, ImmutableList<V> valueList) {
    this.keySet = keySet;
    this.valueList = valueList;
  }

  RegularImmutableSortedMap(
      RegularImmutableSortedSet<K> keySet,
      ImmutableList<V> valueList,
      ImmutableSortedMap<K, V> descendingMap) {
    super(descendingMap);
    this.keySet = keySet;
    this.valueList = valueList;
  }

  
  ImmutableSet<Entry<K, V>> createEntrySet() {
    return new EntrySet();
  }

  private class EntrySet extends ImmutableMapEntrySet<K, V> {
    
    public UnmodifiableIterator<Entry<K, V>> iterator() {
      return asList().iterator();
    }

    
    ImmutableList<Entry<K, V>> createAsList() {
      return new ImmutableAsList<Entry<K, V>>() {
        // avoid additional indirection
        private final ImmutableList<K> keyList = keySet().asList();
        private final ImmutableList<V> valueList = values().asList();

        
        public Entry<K, V> get(int index) {
          return Maps.immutableEntry(keyList.get(index), valueList.get(index));
        }

        
        ImmutableCollection<Entry<K, V>> delegateCollection() {
          return EntrySet.this;
        }
      };
    }

    
    ImmutableMap<K, V> map() {
      return RegularImmutableSortedMap.this;
    }
  }

  
  public ImmutableSortedSet<K> keySet() {
    return keySet;
  }

  
  public ImmutableCollection<V> values() {
    return valueList;
  }

  
  public V get(@Nullable Object key) {
    int index = keySet.indexOf(key);
    return (index == -1) ? null : valueList.get(index);
  }

  private ImmutableSortedMap<K, V> getSubMap(int fromIndex, int toIndex) {
    if (fromIndex == 0 && toIndex == size()) {
      return this;
    } else if (fromIndex == toIndex) {
      return emptyMap(comparator());
    } else {
      return from(
          keySet.getSubSet(fromIndex, toIndex),
          valueList.subList(fromIndex, toIndex));
    }
  }

  
  public ImmutableSortedMap<K, V> headMap(K toKey, boolean inclusive) {
    return getSubMap(0, keySet.headIndex(checkNotNull(toKey), inclusive));
  }

  
  public ImmutableSortedMap<K, V> tailMap(K fromKey, boolean inclusive) {
    return getSubMap(keySet.tailIndex(checkNotNull(fromKey), inclusive), size());
  }

  
  ImmutableSortedMap<K, V> createDescendingMap() {
    return new RegularImmutableSortedMap<K, V>(
        (RegularImmutableSortedSet<K>) keySet.descendingSet(),
        valueList.reverse(),
        this);
  }

}
