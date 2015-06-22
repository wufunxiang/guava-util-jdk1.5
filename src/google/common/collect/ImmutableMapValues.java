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
import google.common.annotations.GwtIncompatible;

import java.io.Serializable;
import java.util.Map.Entry;

/**
 * {@code values()} implementation for {@link google.common.collect.ImmutableMap}.
 *
 * @author Jesse Wilson
 * @author Kevin Bourrillion
 */
@GwtCompatible(emulated = true)
abstract class ImmutableMapValues<K, V> extends ImmutableCollection<V> {
  ImmutableMapValues() {}

  abstract ImmutableMap<K, V> map();

  
  public int size() {
    return map().size();
  }

  
  public UnmodifiableIterator<V> iterator() {
    return Maps.valueIterator(map().entrySet().iterator());
  }

  
  public boolean contains(Object object) {
    return map().containsValue(object);
  }

  
  boolean isPartialView() {
    return true;
  }

  
  ImmutableList<V> createAsList() {
    final ImmutableList<Entry<K, V>> entryList = map().entrySet().asList();
    return new ImmutableAsList<V>() {
      
      public V get(int index) {
        return entryList.get(index).getValue();
      }

      
      ImmutableCollection<V> delegateCollection() {
        return ImmutableMapValues.this;
      }
    };
  }

  @GwtIncompatible("serialization")
   Object writeReplace() {
    return new SerializedForm<V>(map());
  }

  @GwtIncompatible("serialization")
  private static class SerializedForm<V> implements Serializable {
    final ImmutableMap<?, V> map;
    SerializedForm(ImmutableMap<?, V> map) {
      this.map = map;
    }
    Object readResolve() {
      return map.values();
    }
    private static final long serialVersionUID = 0;
  }
}
