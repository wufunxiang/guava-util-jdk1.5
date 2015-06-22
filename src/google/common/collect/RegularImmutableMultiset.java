/*
 * Copyright (C) 2011 The Guava Authors
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

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Implementation of {@link google.common.collect.ImmutableMultiset} with one or more elements.
 *
 * @author Jared Levy
 * @author Louis Wasserman
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("serial")
// uses writeReplace(), not default serialization
class RegularImmutableMultiset<E> extends ImmutableMultiset<E> {
  private final transient ImmutableMap<E, Integer> map;
  private final transient int size;

  RegularImmutableMultiset(ImmutableMap<E, Integer> map, int size) {
    this.map = map;
    this.size = size;
  }

  
  boolean isPartialView() {
    return map.isPartialView();
  }

  
  public int count(@Nullable Object element) {
    Integer value = map.get(element);
    return (value == null) ? 0 : value;
  }

  
  public int size() {
    return size;
  }

  
  public boolean contains(@Nullable Object element) {
    return map.containsKey(element);
  }

  
  public ImmutableSet<E> elementSet() {
    return map.keySet();
  }

  private static <E> Entry<E> entryFromMapEntry(Map.Entry<E, Integer> entry) {
    return Multisets.immutableEntry(entry.getKey(), entry.getValue());
  }

  
  ImmutableSet<Entry<E>> createEntrySet() {
    return new EntrySet();
  }

  private class EntrySet extends ImmutableMultiset<E>.EntrySet {
    
    public int size() {
      return map.size();
    }

    
    public UnmodifiableIterator<Entry<E>> iterator() {
      return asList().iterator();
    }

    
    ImmutableList<Entry<E>> createAsList() {
      final ImmutableList<Map.Entry<E, Integer>> entryList = map.entrySet().asList();
      return new ImmutableAsList<Entry<E>>() {
        
        public Entry<E> get(int index) {
          return entryFromMapEntry(entryList.get(index));
        }

        
        ImmutableCollection<Entry<E>> delegateCollection() {
          return EntrySet.this;
        }
      };
    }
  }

  
  public int hashCode() {
    return map.hashCode();
  }
}
