/*
 * Copyright (C) 2009 The Guava Authors
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
import google.common.base.Preconditions;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Implementation of {@link google.common.collect.ImmutableList} with exactly one element.
 *
 * @author Hayward Chan
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial") // uses writeReplace(), not default serialization
final class SingletonImmutableList<E> extends ImmutableList<E> {

  final transient E element;

  SingletonImmutableList(E element) {
    this.element = checkNotNull(element);
  }

  
  public E get(int index) {
    Preconditions.checkElementIndex(index, 1);
    return element;
  }

   public int indexOf(@Nullable Object object) {
    return element.equals(object) ? 0 : -1;
  }

   public UnmodifiableIterator<E> iterator() {
    return Iterators.singletonIterator(element);
  }

   public int lastIndexOf(@Nullable Object object) {
    return indexOf(object);
  }

  
  public int size() {
    return 1;
  }

   public ImmutableList<E> subList(int fromIndex, int toIndex) {
    Preconditions.checkPositionIndexes(fromIndex, toIndex, 1);
    return (fromIndex == toIndex) ? ImmutableList.<E>of() : this;
  }

   public ImmutableList<E> reverse() {
    return this;
  }

   public boolean contains(@Nullable Object object) {
    return element.equals(object);
  }

   public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof List) {
      List<?> that = (List<?>) object;
      return that.size() == 1 && element.equals(that.get(0));
    }
    return false;
  }

   public int hashCode() {
    // not caching hash code since it could change if the element is mutable
    // in a way that modifies its hash code.
    return 31 + element.hashCode();
  }

   public String toString() {
    String elementToString = element.toString();
    return new StringBuilder(elementToString.length() + 2)
        .append('[')
        .append(elementToString)
        .append(']')
        .toString();
  }

   public boolean isEmpty() {
    return false;
  }

   boolean isPartialView() {
    return false;
  }

   public Object[] toArray() {
    return new Object[] { element };
  }

   public <T> T[] toArray(T[] array) {
    if (array.length == 0) {
      array = ObjectArrays.newArray(array, 1);
    } else if (array.length > 1) {
      array[1] = null;
    }
    // Writes will produce ArrayStoreException when the toArray() doc requires.
    Object[] objectArray = array;
    objectArray[0] = element;
    return array;
  }
}
