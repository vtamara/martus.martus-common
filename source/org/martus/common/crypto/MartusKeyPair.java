/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.common.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class MartusKeyPair
{
	public abstract PrivateKey getPrivateKey();

	public abstract PublicKey getPublicKey();

	public abstract String getPublicKeyString();

	public abstract void clear();

	public abstract boolean hasKeyPair();

	public abstract boolean isKeyPairValid();

	public abstract void createRSA(int publicKeyBits)
			throws Exception;

	public abstract byte[] getKeyPairData() throws IOException;

	public abstract void setFromData(byte[] data) throws Exception;

	public abstract byte[] encryptBytes(byte[] bytesToEncrypt,
			String recipientPublicKeyX509) throws Exception;

	public abstract byte[] decryptBytes(byte[] bytesToDecrypt) throws Exception;

	public abstract byte[] getDigestOfPartOfPrivateKey() throws Exception;

	public static byte[] getKeyPairData(KeyPair jceKeyPairToWrite) throws IOException
	{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(data);
		objectOutputStream.writeObject(jceKeyPairToWrite);
		return data.toByteArray();
	}

}