import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class EciesCryptoPP
{
    public static final int VER_2012_KEY_SIZE = 160;
    public static final int VER_2014_KEY_SIZE = 512;
    public static byte[] privateKey;
    public static byte[] publicKey;
    public static byte[] privateKeyVer1;
    public static byte[] publicKeyVer1;
    public static byte[] privateKeyVer2;
    public static byte[] publicKeyVer2;
    public static byte[] PWEKey;
    public static byte[] PWEKeyUpdaterConf;
    public static byte[] PWEKeyBuildver;
    public static byte[] PWEKeyFileHash;
    public static byte[] PWEKeyLauncherHz;
    public static byte[] PmangKey;
    public static byte[] NAKeyMethod2;
    public static byte[] NAKeyMethod2UpdaterConf;
    static
    {
        System.loadLibrary(getLibName());
        privateKeyVer1 = loadPrivateKey("Private.key");
        publicKeyVer1 = loadPublicKey("Public.key");
        privateKeyVer2 = loadPrivateKey("PrivateVer2.key");
        publicKeyVer2 = loadPublicKey("PublicVer2.key");
        privateKey = privateKeyVer1;
        publicKey = publicKeyVer1;
        loadPWEKey();
    }
    
    public static byte[] decrypt(byte[] encdata)
    {
        return decrypt(privateKey, privateKey.length, encdata, encdata.length);
    }
    
    public static byte[] decryptPWE(byte[] encdata)
    {
        return decrypt(PWEKey, PWEKey.length, encdata, encdata.length);
    }
    
    public static byte[] decryptPWEUpdaterConf(byte[] encdata)
    {
        return decrypt(PWEKeyUpdaterConf, PWEKeyUpdaterConf.length, encdata, encdata.length);
    }
    
    public static byte[] decryptPWEBuildver(byte[] encdata)
    {
        return decrypt(PWEKeyBuildver, PWEKeyBuildver.length, encdata, encdata.length);
    }
    
    public static byte[] encrypt(byte[] decdata)
    {
        return encrypt(publicKey, publicKey.length, decdata, decdata.length);
    }

    public static String generateKeys(int version)
    {
        if (version == 1) {
            return genkeys(VER_2012_KEY_SIZE);
        } else if (version == 2) {
            return genkeys(VER_2014_KEY_SIZE);
        }
        return "";
    }

    public static boolean generateAndSaveKeys(int version)
    {
        String[] keys = generateKeys(version).split(";");
        String privKey = keys[1];
        String pubKey = keys[0];

        String privateFileName = "Private.key";
        String publicFileName = "Public.key";
        if (version == 2) {
            privateFileName = "PrivateVer2.key";
            publicFileName = "PublicVer2.key";
        }
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(new File(privateFileName));
            fos.write(privKey.getBytes());
            fos.close();
        }
        catch(Exception e)
        {
            if (fos != null)
            {
            try
            {
                fos.close();
            }
            catch(Exception ex)
            {}
            }
                return false;
        }
        
        fos = null;
        try
        {
            fos = new FileOutputStream(new File(publicFileName));
            fos.write(pubKey.getBytes());
            fos.close();
        }
        catch(Exception e)
        {
            if (fos != null)
            {
            try
            {
                fos.close();
            }
            catch(Exception ex)
            {}
            }
                return false;
        }
        return true;
    }
    
    public static String getLibName()
    {
        if (System.getProperty("os.arch").contains("64"))
            return "EciesCryptoPP_x64";
        return "EciesCryptoPP";
    }
    
    private static byte[] loadPrivateKey(String fileName)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(new File(fileName));
            byte[] keystr = new byte[fis.available()];
            fis.read(keystr, 0, fis.available());
            fis.close();
            String key = new String(keystr);
            return hexStringToByteArray(key);
        }
        catch(Exception e)
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            return new byte[]{};
        }
    }
    
    private static byte[] loadPublicKey(String fileName)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(new File(fileName));
            byte[] keystr = new byte[fis.available()];
            fis.read(keystr, 0, fis.available());
            fis.close();
            String key = new String(keystr);
            return hexStringToByteArray(key);
        }
        catch(Exception e)
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch(Exception ex)
                {}
            }
            return new byte[]{};
        }
    }
    
    private static void loadPWEKey()
    {
        PWEKey = new byte[Keys.Z3_KEY_RAIDERZ_KR_NA.length];
        for (int i = 0; i < Keys.Z3_KEY_RAIDERZ_KR_NA.length; i++)
            PWEKey[i] = (byte) Keys.Z3_KEY_RAIDERZ_KR_NA[i];

        PWEKeyUpdaterConf = new byte[Keys.Z3_KEY_RAIDERZ_KR_NA_UPDATERCONF.length];
        for (int i = 0; i < Keys.Z3_KEY_RAIDERZ_KR_NA_UPDATERCONF.length; i++)
            PWEKeyUpdaterConf[i] = (byte) Keys.Z3_KEY_RAIDERZ_KR_NA_UPDATERCONF[i];

        PWEKeyBuildver = new byte[Keys.Z3_KEY_RAIDERZ_KR_NA_BUILDVER.length];
        for (int i = 0; i < Keys.Z3_KEY_RAIDERZ_KR_NA_BUILDVER.length; i++)
            PWEKeyBuildver[i] = (byte) Keys.Z3_KEY_RAIDERZ_KR_NA_BUILDVER[i];

        PWEKeyFileHash = new byte[Keys.Z3_KEY_RAIDERZ_KR_NA_FILEHASH.length];
        for (int i = 0; i < Keys.Z3_KEY_RAIDERZ_KR_NA_FILEHASH.length; i++)
            PWEKeyFileHash[i] = (byte) Keys.Z3_KEY_RAIDERZ_KR_NA_FILEHASH[i];
        
        PWEKeyLauncherHz = new byte[Keys.Z3_KEY_RAIDERZ_KR_NA_LAUNCHERHZ.length];
        for (int i = 0; i < Keys.Z3_KEY_RAIDERZ_KR_NA_LAUNCHERHZ.length; i++)
            PWEKeyLauncherHz[i] = (byte) Keys.Z3_KEY_RAIDERZ_KR_NA_LAUNCHERHZ[i];
        
        PmangKey = new byte[Keys.Z3_KEY_RAIDERZ_KR.length];
        for (int i = 0; i < Keys.Z3_KEY_RAIDERZ_KR.length; i++)
            PmangKey[i] = (byte) Keys.Z3_KEY_RAIDERZ_KR[i];

        NAKeyMethod2 = new byte[Keys.Z3_KEY_NARAIDERZ_METHOD2.length];
        for (int i = 0; i < Keys.Z3_KEY_NARAIDERZ_METHOD2.length; i++)
            NAKeyMethod2[i] = (byte) Keys.Z3_KEY_NARAIDERZ_METHOD2[i];

        NAKeyMethod2UpdaterConf = new byte[Keys.Z3_KEY_NARAIDERZ_METHOD2_UPDATERCONF.length];
        for (int i = 0; i < Keys.Z3_KEY_NARAIDERZ_METHOD2_UPDATERCONF.length; i++)
            NAKeyMethod2UpdaterConf[i] = (byte) Keys.Z3_KEY_NARAIDERZ_METHOD2_UPDATERCONF[i];
    }
    
    private static byte[] hexStringToByteArray(String s)
    {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++)
        {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
    
    public static native byte[] decrypt(byte[] key, int keysize, byte[] encdata, int encdatasize);
    public static native byte[] encrypt(byte[] key, int keysize, byte[] decdata, int decdatasize);
    private static native String genkeys(int keysize);
}
