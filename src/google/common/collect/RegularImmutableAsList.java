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

import google.common.annotations.GwtCompatible;

/**
 * An {@link google.common.collect.ImmutableAsList} implementation specialized for when the delegate collection is
 * already backed by an {@code ImmutableList} or array.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
@SuppressWarnings("serial") // uses writeReplace, not default serialization
class RegularImmutableAsList<E> extends ImmutableAsList<E> {
  private final ImmutableCollection<E> delegate;
  private final ImmutableList<? extends E> delegateList;

  RegularImmutableAsList(ImmutableCollection<E> delegate, ImmutableList<? extends E> delegateList) {
    this.delegate = delegate;
    this.delegateList = delegateList;
  }

  RegularImmutableAsList(ImmutableCollection<E> delegate, Object[] array) {
    this(delegate, ImmutableList.<E>asImmutableList(array));
  }

  
  ImmutableCollection<E> delegateCollection() {
    return delegate;
  }

  ImmutableList<? extends E> delegateList() {
    return delegateList;
  }

  @SuppressWarnings("unchecked")  // safe covariant cast!
  
  public UnmodifiableListIterator<E> listIterator(int index) {
    return (UnmodifiableListIterator<E>) delegateList.listIterator(index);
  }

  
  public Object[] toArray() {
    return delegateList.toArray();
  }

  
  public <T> T[] toArray(T[] other) {
    return delegateList.toArray(other);
  }

  
  public int indexOf(Object object) {
    return delegateList.indexOf(object);
  }

  
  public int lastIndexOf(Object object) {
    return delegateList.lastIndexOf(object);
  }

  
  public boolean equals(Object obj) {
    return delegateList.equals(obj);
  }

  
  public int hashCode() {
    return delegateList.hashCode();
  }

  
  public E get(int index) {
    return delegateList.get(index);
  }
}
