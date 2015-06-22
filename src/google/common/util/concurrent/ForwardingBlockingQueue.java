/*
 * Copyright (C) 2010 The Guava Authors
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

package google.common.util.concurrent;

import google.common.collect.ForwardingQueue;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A {@link java.util.concurrent.BlockingQueue} which forwards all its method calls to another
 * {@link java.util.concurrent.BlockingQueue}. Subclasses should override one or more methods to
 * modify the behavior of the backing collection as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @author Raimundo Mirisola
 *
 * @param <E> the type of elements held in this collection
 * @since 4.0
 */
public abstract class ForwardingBlockingQueue<E> extends ForwardingQueue<E>
    implements BlockingQueue<E> {

  /** Constructor for use by subclasses. */
  protected ForwardingBlockingQueue() {}

   protected abstract BlockingQueue<E> delegate();

   public int drainTo(
      Collection<? super E> c, int maxElements) {
    return delegate().drainTo(c, maxElements);
  }

   public int drainTo(Collection<? super E> c) {
    return delegate().drainTo(c);
  }

   public boolean offer(E e, long timeout, TimeUnit unit)
      throws InterruptedException {
    return delegate().offer(e, timeout, unit);
  }

   public E poll(long timeout, TimeUnit unit)
      throws InterruptedException {
    return delegate().poll(timeout, unit);
  }

   public void put(E e) throws InterruptedException {
    delegate().put(e);
  }

   public int remainingCapacity() {
    return delegate().remainingCapacity();
  }

   public E take() throws InterruptedException {
    return delegate().take();
  }
}