/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package google.common.hash;

import static google.common.base.Preconditions.checkPositionIndexes;
import static google.common.base.Preconditions.checkState;

import google.common.primitives.Chars;
import google.common.primitives.Ints;
import google.common.primitives.Longs;
import google.common.primitives.Shorts;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * {@link google.common.hash.HashFunction} adapter for {@link java.security.MessageDigest}s.
 *
 * @author Kevin Bourrillion
 * @author Dimitris Andreou
 */
final class MessageDigestHashFunction extends AbstractStreamingHashFunction {
  private final String algorithmName;
  private final int bits;

  MessageDigestHashFunction(String algorithmName) {
    this.algorithmName = algorithmName;
    this.bits = getMessageDigest(algorithmName).getDigestLength() * 8;
  }

  public int bits() {
    return bits;
  }

  private static MessageDigest getMessageDigest(String algorithmName) {
    try {
      return MessageDigest.getInstance(algorithmName);
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

   public Hasher newHasher() {
    return new MessageDigestHasher(getMessageDigest(algorithmName));
  }

  private static class MessageDigestHasher implements Hasher {
    private final MessageDigest digest;
    private final ByteBuffer scratch; // lazy convenience
    private boolean done;

    private MessageDigestHasher(MessageDigest digest) {
      this.digest = digest;
      this.scratch = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    }

     public Hasher putByte(byte b) {
      checkNotDone();
      digest.update(b);
      return this;
    }

     public Hasher putBytes(byte[] bytes) {
      checkNotDone();
      digest.update(bytes);
      return this;
    }

     public Hasher putBytes(byte[] bytes, int off, int len) {
      checkNotDone();
      checkPositionIndexes(off, off + len, bytes.length);
      digest.update(bytes, off, len);
      return this;
    }

     public Hasher putShort(short s) {
      checkNotDone();
      scratch.putShort(s);
      digest.update(scratch.array(), 0, Shorts.BYTES);
      scratch.clear();
      return this;
    }

     public Hasher putInt(int i) {
      checkNotDone();
      scratch.putInt(i);
      digest.update(scratch.array(), 0, Ints.BYTES);
      scratch.clear();
      return this;
    }

     public Hasher putLong(long l) {
      checkNotDone();
      scratch.putLong(l);
      digest.update(scratch.array(), 0, Longs.BYTES);
      scratch.clear();
      return this;
    }

     public Hasher putFloat(float f) {
      checkNotDone();
      scratch.putFloat(f);
      digest.update(scratch.array(), 0, 4);
      scratch.clear();
      return this;
    }

     public Hasher putDouble(double d) {
      checkNotDone();
      scratch.putDouble(d);
      digest.update(scratch.array(), 0, 8);
      scratch.clear();
      return this;
    }

     public Hasher putBoolean(boolean b) {
      return putByte(b ? (byte) 1 : (byte) 0);
    }

     public Hasher putChar(char c) {
      checkNotDone();
      scratch.putChar(c);
      digest.update(scratch.array(), 0, Chars.BYTES);
      scratch.clear();
      return this;
    }

     public Hasher putString(CharSequence charSequence) {
      for (int i = 0; i < charSequence.length(); i++) {
        putChar(charSequence.charAt(i));
      }
      return this;
    }

     public Hasher putString(CharSequence charSequence, Charset charset) {
      return putBytes(charSequence.toString().getBytes(charset));
    }

     public <T> Hasher putObject(T instance, Funnel<? super T> funnel) {
      checkNotDone();
      funnel.funnel(instance, this);
      return this;
    }

    private void checkNotDone() {
      checkState(!done, "Cannot use Hasher after calling #hash() on it");
    }

    public HashCode hash() {
      done = true;
      return HashCodes.fromBytesNoCopy(digest.digest());
    }
  }
}
