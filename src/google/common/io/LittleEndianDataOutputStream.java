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

package google.common.io;

import google.common.annotations.Beta;
import google.common.base.Preconditions;
import google.common.primitives.Longs;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An implementation of {@link java.io.DataOutput} that uses little-endian byte ordering
 * for writing {@code char}, {@code short}, {@code int}, {@code float}, {@code
 * double}, and {@code long} values.
 * <p>
 * <b>Note:</b> This class intentionally violates the specification of its
 * supertype {@code DataOutput}, which explicitly requires big-endian byte
 * order.
 *
 * @author Chris Nokleberg
 * @author Keith Bottner
 * @since 8.0
 */
@Beta
public class LittleEndianDataOutputStream extends FilterOutputStream
    implements DataOutput {

  /**
   * Creates a {@code LittleEndianDataOutputStream} that wraps the given stream.
   *
   * @param out the stream to delegate to
   */
  public LittleEndianDataOutputStream(OutputStream out) {
    super(new DataOutputStream(Preconditions.checkNotNull(out)));
  }

   public void write(byte[] b, int off, int len) throws IOException {
    // Override slow FilterOutputStream impl
    out.write(b, off, len);
  }

   public void writeBoolean(boolean v) throws IOException {
    ((DataOutputStream) out).writeBoolean(v);
  }

   public void writeByte(int v) throws IOException {
    ((DataOutputStream) out).writeByte(v);
  }

  /**
   * @deprecated The semantics of {@code writeBytes(String s)} are considered
   *             dangerous. Please use {@link #writeUTF(String s)},
   *             {@link #writeChars(String s)} or another write method instead.
   */
  @Deprecated
   public void writeBytes(String s) throws IOException {
    ((DataOutputStream) out).writeBytes(s);
  }

  /**
   * Writes a char as specified by {@link java.io.DataOutputStream#writeChar(int)},
   * except using little-endian byte order.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
   public void writeChar(int v) throws IOException {
    writeShort(v);
  }

  /**
   * Writes a {@code String} as specified by
   * {@link java.io.DataOutputStream#writeChars(String)}, except each character is
   * written using little-endian byte order.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
   public void writeChars(String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      writeChar(s.charAt(i));
    }
  }

  /**
   * Writes a {@code double} as specified by
   * {@link java.io.DataOutputStream#writeDouble(double)}, except using little-endian
   * byte order.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
   public void writeDouble(double v) throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }

  /**
   * Writes a {@code float} as specified by
   * {@link java.io.DataOutputStream#writeFloat(float)}, except using little-endian byte
   * order.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
   public void writeFloat(float v) throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  /**
   * Writes an {@code int} as specified by
   * {@link java.io.DataOutputStream#writeInt(int)}, except using little-endian byte
   * order.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
   public void writeInt(int v) throws IOException {
    out.write(0xFF & v);
    out.write(0xFF & (v >> 8));
    out.write(0xFF & (v >> 16));
    out.write(0xFF & (v >> 24));
  }

  /**
   * Writes a {@code long} as specified by
   * {@link java.io.DataOutputStream#writeLong(long)}, except using little-endian byte
   * order.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
   public void writeLong(long v) throws IOException {
    byte[] bytes = Longs.toByteArray(Long.reverseBytes(v));
    write(bytes, 0, bytes.length);
  }

  /**
   * Writes a {@code short} as specified by
   * {@link java.io.DataOutputStream#writeShort(int)}, except using little-endian byte
   * order.
   *
   * @throws java.io.IOException if an I/O error occurs
   */
   public void writeShort(int v) throws IOException {
    out.write(0xFF & v);
    out.write(0xFF & (v >> 8));
  }

   public void writeUTF(String str) throws IOException {
    ((DataOutputStream) out).writeUTF(str);
  }
}