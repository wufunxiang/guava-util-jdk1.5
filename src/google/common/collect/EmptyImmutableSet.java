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

import google.common.annotations.GwtCompatible;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * An empty immutable set.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(serializable = true, emulated = true)
final class EmptyImmutableSet extends ImmutableSet<Object> {
  static final EmptyImmutableSet INSTANCE = new EmptyImmutableSet();

  private EmptyImmutableSet() {}

  
  public int size() {
    return 0;
  }

   public boolean isEmpty() {
    return true;
  }

   public boolean contains(@Nullable Object target) {
    return false;
  }

   public boolean containsAll(Collection<?> targets) {
    return targets.isEmpty();
  }

   public UnmodifiableIterator<Object> iterator() {
    return Iterators.emptyIterator();
  }

   boolean isPartialView() {
    return false;
  }

   public Object[] toArray() {
    return ObjectArrays.EMPTY_ARRAY;
  }

   public <T> T[] toArray(T[] a) {
    return asList().toArray(a);
  }

  
  public ImmutableList<Object> asList() {
    return ImmutableList.of();
  }

   public boolean equals(@Nullable Object object) {
    if (object instanceof Set) {
      Set<?> that = (Set<?>) object;
      return that.isEmpty();
    }
    return false;
  }

   public final int hashCode() {
    return 0;
  }

   boolean isHashCodeFast() {
    return true;
  }

   public String toString() {
    return "[]";
  }

  Object readResolve() {
    return INSTANCE; // preserve singleton property
  }

  private static final long serialVersionUID = 0;
}
