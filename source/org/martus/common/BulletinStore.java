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

package org.martus.common;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.Base64.InvalidBase64Exception;


public class BulletinStore
{
	public BulletinStore()
	{
	}

	public void doAfterSigninInitialization(File dataRootDirectory, Database db) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		dir = dataRootDirectory;
		database = db;
		database.initialize();
	}

	public void setSignatureGenerator(MartusCrypto securityToUse)
	{
		security = securityToUse;
	}
	
	public MartusCrypto getSignatureGenerator()
	{
		return security;
	}
	
	public MartusCrypto getSignatureVerifier()
	{
		return security;
	}
	
	public ReadableDatabase getDatabase()
	{
		return database;
	}
	
	protected Database getWriteableDatabase()
	{
		return database;
	}
	
	public void setDatabase(Database toUse)
	{
		database = toUse;
	}
	
	public File getStoreRootDir()
	{
		return dir;
	}

	public String getAccountId()
	{
		return security.getPublicKeyString();
	}

	public int getBulletinCount()
	{
		return scanForLeafKeys().size();
	}

	public Vector getAllBulletinUids()
	{
		Vector uids = new Vector();
		Vector keys = scanForLeafKeys();
		for(int i=0; i < keys.size(); ++i)
			uids.add( ((DatabaseKey)keys.get(i)).getUniversalId());
		return uids;
	}

	public boolean doesBulletinRevisionExist(DatabaseKey key)
	{
		return getDatabase().doesRecordExist(key);
	}
	
	public boolean hasNewerRevision(UniversalId uid)
	{
		Vector leafKeys = scanForLeafKeys();
		for(int i=0; i < leafKeys.size(); ++i)
		{
			DatabaseKey key = (DatabaseKey)leafKeys.get(i);
			if(!uid.getAccountId().equals(key.getAccountId()))
				continue;
			
			if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				continue;
			try
			{
				BulletinHeaderPacket bhp = loadBulletinHeaderPacket(getDatabase(), key, getSignatureVerifier());
				if(bhp.getHistory().contains(uid.getLocalId()))
					return true;
			}
			catch(Exception nothingWeCanDoAboutIt)
			{
			}
		}

		return false;
	}

	public void deleteAllData() throws Exception
	{
		deleteAllBulletins();
	}

	public void deleteAllBulletins() throws Exception
	{
		database.deleteAllData();
	}

	public void importZipFileToStoreWithSameUids(File inputFile) throws IOException, MartusCrypto.CryptoException, Packet.InvalidPacketException, Packet.SignatureVerificationException
	{
		ZipFile zip = new ZipFile(inputFile);
		try
		{
			BulletinZipUtilities.importBulletinPacketsFromZipFileToDatabase(getWriteableDatabase(), null, zip, getSignatureVerifier());
		}
		catch (Database.RecordHiddenException shouldBeImpossible)
		{
			shouldBeImpossible.printStackTrace();
			throw new IOException(shouldBeImpossible.toString());
		}
		catch(WrongAccountException shouldBeImpossible)
		{
			throw new Packet.InvalidPacketException("Wrong account???");
		}
		finally
		{
			zip.close();
		}
	}
	
	public Vector scanForLeafKeys()
	{
		LeafScanner scanner = new LeafScanner(getDatabase(), getSignatureVerifier());
		visitAllBulletinRevisions(scanner);
		return scanner.getLeafKeys();
	}
	
	public void visitAllBulletins(Database.PacketVisitor visitor)
	{
		Vector leafKeys = scanForLeafKeys();
		for(int i=0; i < leafKeys.size(); ++i)
			visitor.visit((DatabaseKey)leafKeys.get(i));
	}

	public Vector getUidsOfAllBulletinRevisions()
	{
		class UidCollector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				uidList.add(key.getUniversalId());
			}
			Vector uidList = new Vector();
		}
	
		UidCollector uidCollector = new UidCollector();
		visitAllBulletinRevisions(uidCollector);
		return uidCollector.uidList;
	}

	public void visitAllBulletinRevisions(Database.PacketVisitor visitorToUse)
	{
		class BulletinKeyFilter implements Database.PacketVisitor
		{
			BulletinKeyFilter(ReadableDatabase db, Database.PacketVisitor visitorToUse2)
			{
				visitor = visitorToUse2;
				db.visitAllRecords(this);
			}
	
			public void visit(DatabaseKey key)
			{
				if(BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				{
					++count;
					visitor.visit(key);
				}
			}
			ReadableDatabase.PacketVisitor visitor;
			int count;
		}
	
		new BulletinKeyFilter(getDatabase(), visitorToUse);
	}

	public synchronized void removeBulletinFromStore(Bulletin b) throws IOException
	{
		MartusCrypto crypto = getSignatureVerifier();
		BulletinHistory history = b.getHistory();
		try
		{
			for(int i = 0; i < history.size(); ++i)
			{
				String localIdOfAncestor = history.get(i);
				UniversalId uidOfAncestor = UniversalId.createFromAccountAndLocalId(b.getAccount(), localIdOfAncestor);
				deleteBulletinRevision(DatabaseKey.createSealedKey(uidOfAncestor));
			}

			BulletinHeaderPacket bhpMain = b.getBulletinHeaderPacket();
			deleteBulletinRevisionFromDatabase(bhpMain, getWriteableDatabase(), crypto);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			throw new IOException("Unable to delete bulletin");
		}
	}

	public void deleteBulletinRevision(DatabaseKey keyToDelete) throws IOException, CryptoException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, UnsupportedEncodingException, NoKeyPairException
	{
		BulletinHeaderPacket bhp = loadBulletinHeaderPacket(getDatabase(), keyToDelete, getSignatureVerifier());
		deleteBulletinRevisionFromDatabase(bhp, getWriteableDatabase(), getSignatureVerifier());
	}

	public static void deleteBulletinRevisionFromDatabase(BulletinHeaderPacket bhp, Database db, MartusCrypto crypto)
		throws
			IOException,
			MartusCrypto.CryptoException,
			UnsupportedEncodingException,
			Packet.InvalidPacketException,
			Packet.WrongPacketTypeException,
			Packet.SignatureVerificationException,
			MartusCrypto.DecryptionException,
			MartusCrypto.NoKeyPairException
	{
		DatabaseKey[] keys = BulletinZipUtilities.getAllPacketKeys(bhp);
		for (int i = 0; i < keys.length; i++)
		{
			db.discardRecord(keys[i]);
		}
	}

	public static BulletinHeaderPacket loadBulletinHeaderPacket(ReadableDatabase db, DatabaseKey key, MartusCrypto security)
	throws
		IOException,
		CryptoException,
		InvalidPacketException,
		WrongPacketTypeException,
		SignatureVerificationException,
		DecryptionException
	{
		InputStreamWithSeek in = db.openInputStream(key, security);
		try
		{
			BulletinHeaderPacket bhp = new BulletinHeaderPacket();
			bhp.loadFromXml(in, security);
			return bhp;
		}
		finally
		{
			in.close();
		}
	}

	public void hidePackets(Vector packetsIdsToHide, LoggerInterface logger) throws InvalidBase64Exception
	{
		Database db = getWriteableDatabase();
		for(int i = 0; i < packetsIdsToHide.size(); ++i)
		{
			UniversalId uId = (UniversalId)(packetsIdsToHide.get(i));
			db.hide(uId);
			String publicCode = MartusCrypto.getFormattedPublicCode(uId.getAccountId());
			logger.log("Deleting " + publicCode + ": " + uId.getLocalId());
		
		}
	}

	private MartusCrypto security;
	private File dir;
	private Database database;
}

class LeafScanner implements Database.PacketVisitor
{
	public LeafScanner(ReadableDatabase databaseToScan, MartusCrypto cryptoToUse)
	{
		db = databaseToScan;
		crypto = cryptoToUse;
		leafKeys = new Vector();
		nonLeafUids = new Vector();
	}
	
	public Vector getLeafKeys()
	{
		return leafKeys;
	}
	
	public void visit(DatabaseKey key)
	{
		try
		{
			UniversalId maybeLeaf = key.getUniversalId();
			if(!nonLeafUids.contains(maybeLeaf))
				leafKeys.add(key);
			
			BulletinHeaderPacket bhp = BulletinStore.loadBulletinHeaderPacket(db, key, crypto);
			BulletinHistory history = bhp.getHistory();
			for(int i=0; i < history.size(); ++i)
			{
				String thisLocalId = history.get(i);
				UniversalId uidOfNonLeaf = UniversalId.createFromAccountAndLocalId(bhp.getAccountId(), thisLocalId);
				leafKeys.remove(DatabaseKey.createSealedKey(uidOfNonLeaf));
				leafKeys.remove(DatabaseKey.createDraftKey(uidOfNonLeaf));
				nonLeafUids.add(uidOfNonLeaf);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	ReadableDatabase db;
	MartusCrypto crypto;
	Vector leafKeys;
	Vector nonLeafUids;
}
