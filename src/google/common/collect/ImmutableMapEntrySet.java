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

import javax.annotation.Nullable;

/**
 * {@code entrySet()} implementation for {@link google.common.collect.ImmutableMap}.
 *
 * @author Jesse Wilson
 * @author Kevin Bourrillion
 */
@GwtCompatible(emulated = true)
abstract class ImmutableMapEntrySet<K, V> extends ImmutableSet<Entry<K, V>> {
  ImmutableMapEntrySet() {}

  abstract ImmutableMap<K, V> map();

  
  public int size() {
    return map().size();
  }

  
  public boolean contains(@Nullable Object object) {
    if (object instanceof Entry) {
      Entry<?, ?> entry = (Entry<?, ?>) object;
      V value = map().get(entry.getKey());
      return value != null && value.equals(entry.getValue());
    }
    return false;
  }

  
  boolean isPartialView() {
    return map().isPartialView();
  }

  @GwtIncompatible("serialization")
  
  Object writeReplace() {
    return new EntrySetSerializedForm<K, V>(map());
  }

  @GwtIncompatible("serialization")
  private static class EntrySetSerializedForm<K, V> implements Serializable {
    final ImmutableMap<K, V> map;
    EntrySetSerializedForm(ImmutableMap<K, V> map) {
      this.map = map;
    }
    Object readResolve() {
      return map.entrySet();
    }
    private static final long serialVersionUID = 0;
  }
}
