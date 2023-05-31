// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once

#include "targetver.h"
#include <jni.h>

#define WIN32_LEAN_AND_MEAN             // Exclude rarely-used stuff from Windows headers
// Windows Header Files:
#include <windows.h>

#include <stdio.h>
#include <string>
#include <vector>

// CryptoLib project headers
#include "filters.h"
#include "osrng.h"
#include "eccrypto.h"
#include "hex.h"
#include "oids.h"

using namespace CryptoPP;
using std::string;
using std::vector;




// TODO: reference additional headers your program requires here
