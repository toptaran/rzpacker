/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class EciesCryptoPP */

#ifndef _Included_EciesCryptoPP
#define _Included_EciesCryptoPP
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     EciesCryptoPP
 * Method:    decrypt
 * Signature: ([BI[BI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_EciesCryptoPP_decrypt
  (JNIEnv *, jclass, jbyteArray, jint, jbyteArray, jint);

/*
 * Class:     EciesCryptoPP
 * Method:    encrypt
 * Signature: ([BI[BI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_EciesCryptoPP_encrypt
  (JNIEnv *, jclass, jbyteArray, jint, jbyteArray, jint);

/*
 * Class:     EciesCryptoPP
 * Method:    genkeys
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_EciesCryptoPP_genkeys
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
