import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class EciesCryptoPP
{
    public static byte[] privateKey;
    public static byte[] publicKey;
    public static byte[] PWEKey;
    public static byte[] PWEKeyUpdaterConf;
    public static byte[] PWEKeyBuildver;
    public static byte[] PWEKeyFileHash;
    public static byte[] PWEKeyLauncherHz;
    public static byte[] PmangKey;
    static
    {
        System.loadLibrary(getLibName());
        loadPrivateKey();
        loadPublicKey();
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

    public static String generateKeys()
    {
        return genkeys();
    }

    public static boolean generateAndSaveKeys()
    {
        String[] keys = generateKeys().split(";");
        String privKey = keys[1];
        String pubKey = keys[0];

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(new File("Private.key"));
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
            fos = new FileOutputStream(new File("Public.key"));
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
    
    private static void loadPrivateKey()
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(new File("Private.key"));
            byte[] keystr = new byte[fis.available()];
            fis.read(keystr, 0, fis.available());
            fis.close();
            String key = new String(keystr);
            privateKey = hexStringToByteArray(key);
        }
        catch(Exception e)
        {
            privateKey = new byte[]{};
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch(Exception ex)
                {}
            }
        }
    }
    
    private static void loadPublicKey()
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(new File("Public.key"));
            byte[] keystr = new byte[fis.available()];
            fis.read(keystr, 0, fis.available());
            fis.close();
            String key = new String(keystr);
            publicKey = hexStringToByteArray(key);
        }
        catch(Exception e)
        {
            publicKey = new byte[]{};
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch(Exception ex)
                {}
            }
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
    private static native byte[] encrypt(byte[] key, int keysize, byte[] decdata, int decdatasize);
    private static native String genkeys();
}
