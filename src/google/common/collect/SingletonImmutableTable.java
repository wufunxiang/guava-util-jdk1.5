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
import google.common.base.Objects;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * An implementation of {@link google.common.collect.ImmutableTable} that holds a single cell.
 *
 * @author Gregory Kick
 */
@GwtCompatible
final class SingletonImmutableTable<R, C, V> extends ImmutableTable<R, C, V> {
  private final R singleRowKey;
  private final C singleColumnKey;
  private final V singleValue;

  SingletonImmutableTable(R rowKey, C columnKey, V value) {
    this.singleRowKey = checkNotNull(rowKey);
    this.singleColumnKey = checkNotNull(columnKey);
    this.singleValue = checkNotNull(value);
  }

  SingletonImmutableTable(Cell<R, C, V> cell) {
    this(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
  }

   public ImmutableSet<Cell<R, C, V>> cellSet() {
    return ImmutableSet.of(
        Tables.immutableCell(singleRowKey, singleColumnKey, singleValue));
  }

   public ImmutableMap<R, V> column(C columnKey) {
    checkNotNull(columnKey);
    return containsColumn(columnKey)
        ? ImmutableMap.of(singleRowKey, singleValue)
        : ImmutableMap.<R, V>of();
  }

   public ImmutableSet<C> columnKeySet() {
    return ImmutableSet.of(singleColumnKey);
  }

   public ImmutableMap<C, Map<R, V>> columnMap() {
    return ImmutableMap.of(singleColumnKey,
        (Map<R, V>) ImmutableMap.of(singleRowKey, singleValue));
  }

   public boolean contains(@Nullable Object rowKey,
      @Nullable Object columnKey) {
    return containsRow(rowKey) && containsColumn(columnKey);
  }

   public boolean containsColumn(@Nullable Object columnKey) {
    return Objects.equal(this.singleColumnKey, columnKey);
  }

   public boolean containsRow(@Nullable Object rowKey) {
    return Objects.equal(this.singleRowKey, rowKey);
  }

   public boolean containsValue(@Nullable Object value) {
    return Objects.equal(this.singleValue, value);
  }

   public V get(@Nullable Object rowKey, @Nullable Object columnKey) {
    return contains(rowKey, columnKey) ? singleValue : null;
  }

   public boolean isEmpty() {
    return false;
  }

   public ImmutableMap<C, V> row(R rowKey) {
    checkNotNull(rowKey);
    return containsRow(rowKey)
        ? ImmutableMap.of(singleColumnKey, singleValue)
        : ImmutableMap.<C, V>of();
  }

   public ImmutableSet<R> rowKeySet() {
    return ImmutableSet.of(singleRowKey);
  }

   public ImmutableMap<R, Map<C, V>> rowMap() {
    return ImmutableMap.of(singleRowKey,
        (Map<C, V>) ImmutableMap.of(singleColumnKey, singleValue));
  }

   public int size() {
    return 1;
  }

   public ImmutableCollection<V> values() {
    return ImmutableSet.of(singleValue);
  }

   public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Table) {
      Table<?, ?, ?> that = (Table<?, ?, ?>) obj;
      if (that.size() == 1) {
        Cell<?, ?, ?> thatCell = that.cellSet().iterator().next();
        return Objects.equal(this.singleRowKey, thatCell.getRowKey()) &&
            Objects.equal(this.singleColumnKey, thatCell.getColumnKey()) &&
            Objects.equal(this.singleValue, thatCell.getValue());
      }
    }
    return false;
  }

   public int hashCode() {
    return Objects.hashCode(singleRowKey, singleColumnKey, singleValue);
  }

   public String toString() {
    return new StringBuilder()
        .append('{')
        .append(singleRowKey)
        .append("={")
        .append(singleColumnKey)
        .append('=')
        .append(singleValue)
        .append("}}")
        .toString();
  }
}
