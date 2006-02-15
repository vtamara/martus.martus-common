package org.martus.common.crypto;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.bouncycastle.jce.provider.JCERSAPublicKey;

public class MartusKeyPairLoader
{

	public MartusKeyPair readMartusKeyPair(DataInputStream in) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException 
	{
		nextHandle = INITIAL_HANDLE;
		
		int magic = in.readShort();
		throwIfNotEqual(ObjectStreamConstants.STREAM_MAGIC, magic);
		int streamVersion = in.readShort();
		throwIfNotEqual(ObjectStreamConstants.STREAM_VERSION, streamVersion);
					
		// KeyPair
		{
			readObjectClassHeader(in, "java.security.KeyPair", -7565189502268009837L);
			
			int classDescFlags = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, classDescFlags);
			int fieldCount = in.readShort();
			String[] expectedFields = {"privateKey", "publicKey"};
			String[] expectedClassNames = {"Ljava/security/PrivateKey;", "Ljava/security/PublicKey;"};
			throwIfNotEqual(expectedFields.length, fieldCount);
			for(int field=0; field < fieldCount; ++field)
			{
				readObjectFieldDescription(in, expectedClassNames[field], expectedFields[field]);
			}
			
			readClassFooter(in);
		}
		
		// JCERSAPrivateCrtKey
		{
			readObjectClassHeader(in, "org.bouncycastle.jce.provider.JCERSAPrivateCrtKey", 7834723820638524718L);
			
			int classDescFlagsForPrivate = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, classDescFlagsForPrivate);
			int fieldCountForPrivate = in.readShort();
			String[] expectedFieldsForPrivate = {
					"crtCoefficient", "primeExponentP", "primeExponentQ",
					"primeP", "primeQ", "publicExponent"};
			throwIfNotEqual(expectedFieldsForPrivate.length, fieldCountForPrivate);

			bigIntStringHandle = readObjectFieldDescription(in, "Ljava/math/BigInteger;", expectedFieldsForPrivate[0]);
			for(int field=1; field < fieldCountForPrivate; ++field)
			{
				throwIfNotEqual(expectedFieldsForPrivate[field], readBigIntegerFieldReference(in));
			}
			
			int endDataFlagForPrivate = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, endDataFlagForPrivate);
			
			// Super Class
			int superClassFlagForPrivate = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_CLASSDESC, superClassFlagForPrivate);
			String classNameForPrivateSuper = in.readUTF();
			throwIfNotEqual("org.bouncycastle.jce.provider.JCERSAPrivateKey", classNameForPrivateSuper);
			long uidForPrivateKeyClassSuper = in.readLong();
			throwIfNotEqual(-5605421053708761770L, uidForPrivateKeyClassSuper);
			// new handle
			nextHandle++;
			
			int classDescFlagsForPrivateSuper = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD, classDescFlagsForPrivateSuper);
			int fieldCountForPrivateSuper = in.readShort();
			throwIfNotEqual(4, fieldCountForPrivateSuper);
			
			// Private Key field 1
			throwIfNotEqual("modulus", readBigIntegerFieldReference(in));
						
			// Private Key field 2
			readObjectFieldDescription(in, "Ljava/util/Hashtable;", "pkcs12Attributes");
			
			// Private Key field 3
			readObjectFieldDescription(in, "Ljava/util/Vector;", "pkcs12Ordering");
			
			// Private Key field 4
			throwIfNotEqual("privateExponent", readBigIntegerFieldReference(in));
			
			readClassFooter(in);
		}			

		// BigInteger
		{
			bigIntClassHandle = readObjectClassHeader(in, "java.math.BigInteger", -8287574255936472291L);
						
			int classDescFlags = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD, classDescFlags);
			int fieldCount = in.readShort();
			throwIfNotEqual(6, fieldCount);
			// Big Integer field 1
			throwIfNotEqual("bitCount", readIntFieldDescription(in));

			// Big Integer field 2
			throwIfNotEqual("bitLength", readIntFieldDescription(in));
			
			// Big Integer field 3
			throwIfNotEqual("firstNonzeroByteNum", readIntFieldDescription(in));
			
			// Big Integer field 4
			throwIfNotEqual("lowestSetBit", readIntFieldDescription(in));
			
			// Big Integer field 5
			throwIfNotEqual("signum", readIntFieldDescription(in));
			
			// Big Integer field 6
			throwIfNotEqual("magnitude", readByteArrayFieldDescription(in));
			
			int endDataFlag = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, endDataFlag);
			int superClassFlag = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_CLASSDESC, superClassFlag);
			String superClassName = in.readUTF();
			throwIfNotEqual("java.lang.Number", superClassName);
			long superUid = in.readLong();
			throwIfNotEqual(-8742448824652078965L, superUid);
			// new handle
			nextHandle++;
			
			int superClassDescFlags = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, superClassDescFlags);
			int superFieldCount = in.readShort();
			throwIfNotEqual(0, superFieldCount);
			
			modulusObjectHandle = readClassFooter(in);
			
			//BigInt Modulus Data
			{				
				int bitCount = in.readInt();
				throwIfNotEqual(-1, bitCount);
				int bitLength = in.readInt();
				throwIfNotEqual(-1, bitLength);
				int firstNonZeroByteNum = in.readInt();
				throwIfNotEqual(-2, firstNonZeroByteNum);
				int lowestSetBit = in.readInt();
				throwIfNotEqual(-2, lowestSetBit);
				int signum = in.readInt();
				throwIfNotEqual(1, signum);
								
				byteArrayClassHandle = readArrayClassHeader(in, "[B", -5984413125824719648L);
				
				int magnitudeClassDescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, magnitudeClassDescFlags);
				//int superClassDescFlags = in.readByte();
				int magnitudeFieldCount = in.readShort();
				throwIfNotEqual(0, magnitudeFieldCount);
				
				readClassFooter(in);
				
				int arrayLength = in.readInt();

				byte[] magnitude = new byte[arrayLength];
				in.read(magnitude);
				
				int arrayEndDataFlag = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, arrayEndDataFlag);
				
				modulus = new BigInteger(signum, magnitude);
				
			}
			
			// Hashtable
			{
				readObjectClassHeader(in, "java.util.Hashtable", 1421746759512286392L);
				
				int hashTableClassDescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD, hashTableClassDescFlags);
				short hashTableFieldCount = in.readShort();
				throwIfNotEqual(2, hashTableFieldCount);
				
				// Hash Table field 1
				throwIfNotEqual("loadFactor", readFloatFieldDescription(in));
				
				// Hash Table field 2
				throwIfNotEqual("threshold", readIntFieldDescription(in));

				readClassFooter(in);
				
				readEmptyHashTableData(in);
			}
			
			//Vector
			{
				readObjectClassHeader(in, "java.util.Vector", -2767605614048989439L);
				
				int hashTableClassDescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE | ObjectStreamConstants.SC_WRITE_METHOD, hashTableClassDescFlags);
				short vectorFieldCount = in.readShort();
				throwIfNotEqual(3, vectorFieldCount);
				
				// Vector field 1
				throwIfNotEqual("capacityIncrement", readIntFieldDescription(in));
				
				// Vector field 2
				throwIfNotEqual("elementCount", readIntFieldDescription(in));
				 
				// Vector field 3
				throwIfNotEqual("elementData", readArrayFieldDecription(in));
				
				readClassFooter(in);
				
				int vectorField1Data = in.readInt();
				throwIfNotEqual(0, vectorField1Data);
				
				int vectorField2Data = in.readInt();
				throwIfNotEqual(0, vectorField2Data);
				
				readArrayClassHeader(in, "[Ljava.lang.Object;", -8012369246846506644L);
				
				int vectorField3DescFlags = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, vectorField3DescFlags);
				short vectorField3Count = in.readShort();
				throwIfNotEqual(0, vectorField3Count);
				
				readClassFooter(in);
				
				int arrayLength = in.readInt();
				for(int b = 0; b < arrayLength; ++b)
				{
					byte nullObjectMarker = in.readByte();
					throwIfNotEqual(ObjectStreamConstants.TC_NULL, nullObjectMarker);
				}
				int arrayEndDataFlag = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, arrayEndDataFlag);
			}
			
			//BigInt privateExponent (Private Key Field4)  
			publicExponentObjectHandle = readBigIntegerObjectHeader(in);
			privateExponent = readBigIntegerData(in);
			
			int EndPrivateKeyFlag = in.readByte();
			throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, EndPrivateKeyFlag);
			
			// BigInt crtCoefficient (PrivateCRTKey Field 1)
			readBigIntegerObjectHeader(in);
			crtCoefficient = readBigIntegerData(in);

			// BigInt primeExponentP (PrivateCRTKey Field 2)
			readBigIntegerObjectHeader(in);			
			primeExponentP = readBigIntegerData(in);
			
			// BigInt primeExponentQ (PrivateCRTKey Field 3)
			readBigIntegerObjectHeader(in);			
			primeExponentQ = readBigIntegerData(in);
					
			// BigInt primeP (PrivateCRTKey Field4) 
			readBigIntegerObjectHeader(in);		
			primeP = readBigIntegerData(in);
			
			// BigInt primeQ (PrivateCRTKey Field5)
			readBigIntegerObjectHeader(in);		
			primeQ = readBigIntegerData(in);
			
			// BigInt publicExponent (PrivateCRTKey Field6)
			publicExponentObjectHandle = readBigIntegerObjectHeader(in);
			publicExponent = readBigIntegerData(in);				
			
			// Public Key Description
			{
				readObjectClassHeader(in, "org.bouncycastle.jce.provider.JCERSAPublicKey", 2675817738516720772L);
				
				int classDescFlagsForPublic = in.readByte();
				throwIfNotEqual(ObjectStreamConstants.SC_SERIALIZABLE, classDescFlagsForPublic);
				int fieldCountForPublic = in.readShort();
				throwIfNotEqual(2, fieldCountForPublic);
				String[] expectedFieldsForPublic = {"modulus", "publicExponent"};
				
				for(int i = 0; i < expectedFieldsForPublic.length; ++i)
				{
					String fieldName = readBigIntegerFieldReference(in);
					throwIfNotEqual(expectedFieldsForPublic[i], fieldName);
				}
				
				readClassFooter(in);
				
			}
			
			//Public Key Data
			{
				// BigInt modulus (Field 1) 
				int refModulusObjectHandle = readObjectReference(in);
				throwIfNotEqual(modulusObjectHandle, refModulusObjectHandle);

				//BigInt publicExponent Reference (Field 2)
				int refPublicExponentObjectHandle = readObjectReference(in);
				throwIfNotEqual(publicExponentObjectHandle, refPublicExponentObjectHandle);
			}
		}
		
		// Reconstitute Keypair
		RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulus, publicExponent);
		RSAPrivateCrtKeySpec privateSpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
		KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
		JCERSAPublicKey publicKey = (JCERSAPublicKey)factory.generatePublic(publicSpec);
		JCERSAPrivateCrtKey privateCRTKey = (JCERSAPrivateCrtKey)factory.generatePrivate(privateSpec);
		
		KeyPair keyPair = new KeyPair(publicKey, privateCRTKey);
		gotKeyPair = new MartusJceKeyPair(keyPair);

		return gotKeyPair;
	}

	private int readClassFooter(DataInputStream in) throws IOException {
		int superEndDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, superEndDataFlag);
		int superNoSuperFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_NULL, superNoSuperFlag);
		// new handle		
		return nextHandle++;
	}

	private int readObjectClassHeader(DataInputStream in, String className, long serialUid) throws IOException {
		return readClassHeader(in, ObjectStreamConstants.TC_OBJECT, className, serialUid);
	}
	
	private int readArrayClassHeader(DataInputStream in, String className, long serialUid) throws IOException {
		return readClassHeader(in, ObjectStreamConstants.TC_ARRAY, className, serialUid);
	}

	private int readClassHeader(DataInputStream in, byte classType, String className, long serialUid) throws IOException {
		int objectForPublic = in.readByte();
		throwIfNotEqual(classType, objectForPublic);
		int classFlagForPublic = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_CLASSDESC, classFlagForPublic);
		String classNameForPublic = in.readUTF();
		throwIfNotEqual(className, classNameForPublic);
		long uidForPublicKeyClass = in.readLong();
		throwIfNotEqual(serialUid, uidForPublicKeyClass);
		// new handle
		return nextHandle++;
	}

	private int readObjectFieldDescription(DataInputStream in, String expectedClassName, String expectedFieldName) throws IOException {
		byte typeCode = in.readByte();
		throwIfNotEqual('L', typeCode);
		String fieldName = in.readUTF();
		throwIfNotEqual(expectedFieldName, fieldName);
		int refFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_STRING, refFlag);
		String fieldClassName = in.readUTF();
		throwIfNotEqual(expectedClassName, fieldClassName);
		// new handle
		return nextHandle++;
	}

	private String readByteArrayFieldDescription(DataInputStream in) throws IOException {
		byte typeCode = in.readByte();
		throwIfNotEqual('[', typeCode);
		String fieldName = in.readUTF();
		int refFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_STRING, refFlag);
		String fieldClassName = in.readUTF();
		throwIfNotEqual("[B", fieldClassName);
		// new handle
		nextHandle++;
		return fieldName;
	}

	private String readFloatFieldDescription(DataInputStream in) throws IOException {
		byte typeCode = in.readByte();
		throwIfNotEqual('F', typeCode);
		String fieldName = in.readUTF();
		return fieldName;
	}

	private void readEmptyHashTableData(DataInputStream in) throws IOException {
		float loadFactor = in.readFloat();
		throwIfNotEqual("loadfactor wrong?", 0.75, loadFactor, 0.1f);
		
		int threshold = in.readInt();
		throwIfNotEqual("threshold wrong?", 8, threshold);
		
		byte hashTableBlockDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_BLOCKDATA, hashTableBlockDataFlag);
		
		byte blockDataByteCount = in.readByte();
		throwIfNotEqual("wrong block data byte count?", 8, blockDataByteCount);
		// originalLength
		in.readInt(); 
		int elements = in.readInt();
		throwIfNotEqual("Hashtable not empty?", 0, elements);
		
		int hashTableEndDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, hashTableEndDataFlag);
	}

	private String readArrayFieldDecription(DataInputStream in) throws IOException {
		byte typeCode = in.readByte();
		throwIfNotEqual('[', typeCode);
		String fieldName = in.readUTF();
		int vecString = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_STRING, vecString);
		String fieldClassName = in.readUTF();
		throwIfNotEqual("[Ljava/lang/Object;", fieldClassName);
		nextHandle++;
		return fieldName;
	}

	private String readIntFieldDescription(DataInputStream in) throws IOException {
		byte typeCode = in.readByte();
		throwIfNotEqual('I', typeCode);
		String fieldName = in.readUTF();
		return fieldName;
	}

	private int readBigIntegerObjectHeader(DataInputStream in) throws IOException {
		int publicExponentObjectFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_OBJECT, publicExponentObjectFlag);
		int refFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, refFlag);
		int refBigIntClassHandle = in.readInt();
		throwIfNotEqual(bigIntClassHandle, refBigIntClassHandle);
		int thisHandle = nextHandle++;
		return thisHandle;
	}

	private BigInteger readBigIntegerData(DataInputStream in) throws IOException {
		int bitCount = in.readInt();
		throwIfNotEqual(-1, bitCount);
		int bitLength = in.readInt();
		throwIfNotEqual(-1, bitLength);
		int firstNonZeroByteNum = in.readInt();
		throwIfNotEqual(-2, firstNonZeroByteNum);
		int lowestSetBit = in.readInt();
		throwIfNotEqual(-2, lowestSetBit);
		int signum = in.readInt();
		throwIfNotEqual(1, signum);

		byte typeCode = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ARRAY, typeCode);
		byte typeCodeRefFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, typeCodeRefFlag);
		int refbyteArrayClassHandle = in.readInt();
		throwIfNotEqual(byteArrayClassHandle, refbyteArrayClassHandle);
		nextHandle++;
		
		int arrayLength = in.readInt();
		
		byte[] magnitude = new byte[arrayLength];
		in.read(magnitude);
		
		int arrayEndDataFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_ENDBLOCKDATA, arrayEndDataFlag);

		BigInteger gotBigInteger = new BigInteger(signum, magnitude);
		return gotBigInteger;
	}

	private String readBigIntegerFieldReference(DataInputStream in) throws IOException {
		byte typeCode = in.readByte();
		String fieldName = in.readUTF();
		int refFlag = in.readByte();
		int refBigIntStringHandle = in.readInt();
		throwIfNotEqual('L', typeCode);
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, refFlag);
		throwIfNotEqual(bigIntStringHandle, refBigIntStringHandle);
		return fieldName;
	}

	private int readObjectReference(DataInputStream in) throws IOException {
		int modulusRefFlag = in.readByte();
		throwIfNotEqual(ObjectStreamConstants.TC_REFERENCE, modulusRefFlag);
		int refModulusObjectHandle = in.readInt();
		return refModulusObjectHandle;
	}
	
	void throwIfNotEqual(String text, Object expected, Object actual)
	{
		if(!expected.equals(actual))
			throw new RuntimeException(text + "expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(Object expected, Object actual)
	{
		if(!expected.equals(actual))
			throw new RuntimeException("expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(String text, int expected, int actual)
	{
		if(expected != actual)
			throw new RuntimeException(text + "expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(long expected, long actual)
	{
		if(expected != actual)
			throw new RuntimeException("expected " + expected + " but was " + actual);
	}
	
	void throwIfNotEqual(String text, double expected, double actual, double tolerance)
	{
		if(expected < actual - tolerance || expected > actual + tolerance)
			throw new RuntimeException(text + "expected " + expected + " but was " + actual);
	}
	
	private static final int INITIAL_HANDLE = 8257536;	
	
	int nextHandle;	
	int bigIntStringHandle;
	int bigIntClassHandle;
	int byteArrayClassHandle;
	int modulusObjectHandle;
	int publicExponentObjectHandle;
	
	BigInteger modulus;
	BigInteger publicExponent;
	BigInteger crtCoefficient;
	BigInteger primeExponentP;
	BigInteger primeExponentQ;
	BigInteger primeP;
	BigInteger primeQ;
	BigInteger privateExponent;

	MartusKeyPair gotKeyPair;
}
