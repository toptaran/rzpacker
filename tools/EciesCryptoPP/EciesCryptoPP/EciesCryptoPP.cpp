// EciesCryptoPP.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include <jni.h>
#include <stdio.h>

#include "EciesCryptoPP.h"


JNIEXPORT jbyteArray JNICALL Java_EciesCryptoPP_decrypt (JNIEnv *env, jclass thisObj, jbyteArray key, jint keysize, jbyteArray encdata, jint encdatasize) {
	jbyte* keyb = env->GetByteArrayElements(key, 0);
	StringSource keyStr( (unsigned char *)keyb, keysize, true );
	
	AutoSeededRandomPool rng;
	ECIES<ECP>::Decryptor ellipticalDec(keyStr);
	
	jbyte* encdatab = env->GetByteArrayElements(encdata, 0);
	jint decsize = (jint)ellipticalDec.MaxPlaintextLength(encdatasize);
	jbyte* decdata = new jbyte[decsize];
	DecodingResult dr = ellipticalDec.Decrypt(rng, (const byte *)encdatab, encdatasize, (byte *)decdata);
	
	env->ReleaseByteArrayElements(key, keyb, 0);
	env->ReleaseByteArrayElements(encdata, encdatab, 0);

	if (dr.isValidCoding && dr.messageLength > 0) {
		jbyteArray ret = env->NewByteArray((jsize)dr.messageLength);
		env->SetByteArrayRegion(ret, 0, (jsize)dr.messageLength, decdata);

		delete decdata;
		return ret;
	}

	jbyteArray ret = env->NewByteArray(0);
	delete decdata;
	return ret;
}

JNIEXPORT jbyteArray JNICALL Java_EciesCryptoPP_encrypt (JNIEnv *env, jclass thisObj, jbyteArray key, jint keysize, jbyteArray decdata, jint decdatasize) {
	jbyte* keyb = env->GetByteArrayElements(key, 0);
	StringSource keyStr( (unsigned char *)keyb, keysize, true );
	
	AutoSeededRandomPool rng;
	ECIES<ECP>::Encryptor ellipticalEnc(keyStr);
	
	jbyte* decdatab = env->GetByteArrayElements(decdata, 0);
	jint encsize = (jint)ellipticalEnc.CiphertextLength(decdatasize);
	jbyte* encdata = new jbyte[encsize];
	
	ellipticalEnc.Encrypt(rng, (const byte *)decdatab, decdatasize, (byte *)encdata);
	
	env->ReleaseByteArrayElements(key, keyb, 0);
	env->ReleaseByteArrayElements(decdata, decdatab, 0);

	jbyteArray ret = env->NewByteArray(encsize);
	env->SetByteArrayRegion(ret, 0, encsize, encdata);

	delete encdata;
	return ret;
}

JNIEXPORT jstring JNICALL Java_EciesCryptoPP_genkeys (JNIEnv *env, jclass thisObj) {
	string privkey, pubkey;
	HexEncoder privhex(new StringSink(privkey));
	HexEncoder pubhex(new StringSink(pubkey));

	AutoSeededRandomPool rng;
	ECIES<ECP>::Decryptor decryptor(rng, ASN1::brainpoolP160r1());
	ECIES<ECP>::Encryptor encryptor(decryptor);

	encryptor.GetKey().Save(privhex);
	decryptor.GetKey().Save(pubhex);

	string result = privkey + ";" + pubkey;

	return env->NewStringUTF(result.c_str());
}