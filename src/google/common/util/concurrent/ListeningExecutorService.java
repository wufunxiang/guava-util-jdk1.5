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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * An {@link java.util.concurrent.ExecutorService} that returns {@link google.common.util.concurrent.ListenableFuture} instances. To create an instance
 * from an existing {@link java.util.concurrent.ExecutorService}, call
 * {@link MoreExecutors#listeningDecorator(java.util.concurrent.ExecutorService)}.
 *
 * @author Chris Povirk
 * @since 10.0
 */
public interface ListeningExecutorService extends ExecutorService {
  /**
   * @return a {@code ListenableFuture} representing pending completion of the task
   * @throws java.util.concurrent.RejectedExecutionException {@inheritDoc}
   */
  
  <T> ListenableFuture<T> submit(Callable<T> task);

  /**
   * @return a {@code ListenableFuture} representing pending completion of the task
   * @throws java.util.concurrent.RejectedExecutionException {@inheritDoc}
   */
  
  ListenableFuture<?> submit(Runnable task);

  /**
   * @return a {@code ListenableFuture} representing pending completion of the task
   * @throws java.util.concurrent.RejectedExecutionException {@inheritDoc}
   */
  
  <T> ListenableFuture<T> submit(Runnable task, T result);

  /**
   * {@inheritDoc}
   *
   * <p>All elements in the returned list must be {@link google.common.util.concurrent.ListenableFuture} instances.
   *
   * @return A list of {@code ListenableFuture} instances representing the tasks, in the same
   *         sequential order as produced by the iterator for the given task list, each of which has
   *         completed.
   * @throws java.util.concurrent.RejectedExecutionException {@inheritDoc}
   * @throws NullPointerException if any task is null
   */
  
  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException;

  /**
   * {@inheritDoc}
   *
   * <p>All elements in the returned list must be {@link google.common.util.concurrent.ListenableFuture} instances.
   *
   * @return a list of {@code ListenableFuture} instances representing the tasks, in the same
   *         sequential order as produced by the iterator for the given task list. If the operation
   *         did not time out, each task will have completed. If it did time out, some of these
   *         tasks will not have completed.
   * @throws java.util.concurrent.RejectedExecutionException {@inheritDoc}
   * @throws NullPointerException if any task is null
   */
  
  <T> List<Future<T>> invokeAll(
          Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException;
}
