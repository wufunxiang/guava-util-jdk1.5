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

package google.common.cache;

import google.common.annotations.Beta;

/**
 * The reason why a cached entry was removed.
 *
 * @author Charles Fry
 * @since 10.0
 */
@Beta
public enum RemovalCause {
  /**
   * The entry was manually removed by the user. This can result from the user invoking
   * {@link google.common.cache.Cache#invalidate}, {@link google.common.cache.Cache#invalidateAll(Iterable)}, {@link google.common.cache.Cache#invalidateAll()},
   * {@link java.util.Map#remove}, {@link java.util.concurrent.ConcurrentMap#remove}, or {@link java.util.Iterator#remove}.
   */
  EXPLICIT {
    @Override
    boolean wasEvicted() {
      return false;
    }
  },

  /**
   * The entry itself was not actually removed, but its value was replaced by the user. This can
   * result from the user invoking {@link google.common.cache.Cache#put}, {@link google.common.cache.LoadingCache#refresh}, {@link java.util.Map#put},
   * {@link java.util.Map#putAll}, {@link java.util.concurrent.ConcurrentMap#replace(Object, Object)}, or
   * {@link java.util.concurrent.ConcurrentMap#replace(Object, Object, Object)}.
   */
  REPLACED {
    @Override
    boolean wasEvicted() {
      return false;
    }
  },

  /**
   * The entry was removed automatically because its key or value was garbage-collected. This
   * can occur when using {@link google.common.cache.CacheBuilder#weakKeys}, {@link google.common.cache.CacheBuilder#weakValues}, or
   * {@link google.common.cache.CacheBuilder#softValues}.
   */
  COLLECTED {
    @Override
    boolean wasEvicted() {
      return true;
    }
  },

  /**
   * The entry's expiration timestamp has passed. This can occur when using
   * {@link google.common.cache.CacheBuilder#expireAfterWrite} or {@link google.common.cache.CacheBuilder#expireAfterAccess}.
   */
  EXPIRED {
    @Override
    boolean wasEvicted() {
      return true;
    }
  },

  /**
   * The entry was evicted due to size constraints. This can occur when using
   * {@link google.common.cache.CacheBuilder#maximumSize} or {@link google.common.cache.CacheBuilder#maximumWeight}.
   */
  SIZE {
    @Override
    boolean wasEvicted() {
      return true;
    }
  };

  /**
   * Returns {@code true} if there was an automatic removal due to eviction (the cause is neither
   * {@link #EXPLICIT} nor {@link #REPLACED}).
   */
  abstract boolean wasEvicted();
}
