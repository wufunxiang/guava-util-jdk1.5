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

package google.common.io;

import java.io.DataInput;

/**
 * An extension of {@code DataInput} for reading from in-memory byte arrays; its
 * methods offer identical functionality but do not throw {@link java.io.IOException}.
 *
 * <p><b>Warning:<b> The caller is responsible for not attempting to read past
 * the end of the array. If any method encounters the end of the array
 * prematurely, it throws {@link IllegalStateException} to signify <i>programmer
 * error</i>. This behavior is a technical violation of the supertype's
 * contract, which specifies a checked exception.
 *
 * @author Kevin Bourrillion
 * @since 1.0
 */
public interface ByteArrayDataInput extends DataInput {
   void readFully(byte b[]);

   void readFully(byte b[], int off, int len);

   int skipBytes(int n);

   boolean readBoolean();

   byte readByte();

   int readUnsignedByte();

   short readShort();

   int readUnsignedShort();

   char readChar();

   int readInt();

   long readLong();

   float readFloat();

   double readDouble();

   String readLine();

   String readUTF();
}