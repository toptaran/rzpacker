import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class FileUtils
{
    public static boolean removeFolder(File folder)
    {
        if (folder.isDirectory())
        {
            for (File f: folder.listFiles())
                if (!removeFolder(f))
                    return false;
        }
        if (!folder.delete())
            return false;
        return true;
    }

    private static final int basexorkey = 1436278478;
    private static final byte[] missbytes = {0x00, (byte)0xde, (byte)0x9b, 0x55};
    public static boolean unscramble(byte[] srcBuffer, int xorkey)
    {
        // Unscramble the 32-bit blocks
        int dBlocks = srcBuffer.length >> 2;
        int pos = 0;
        while(dBlocks > 0)
        {
            int tmp = readInt(srcBuffer, pos); // read 32-bit value
        
            xorkey += tmp;
            tmp ^= xorkey;
        
            System.arraycopy(getIntToByte((int)tmp), 0, srcBuffer, pos, 4); // write 32-bit value
            pos += 4;
            --dBlocks;
        }
    
        // Unscramble the remaining data (1-3 bytes)
        byte bAlign = (byte)(srcBuffer.length & 3);
        if(bAlign > 0)
        {
            int lastBlock;
            int i;

            lastBlock = 0;
            i = 0;

            // read remaining 8/16/24-bit value
            while( i < bAlign )
            {
                int tmp = srcBuffer[pos + i] & 0xFF;
                tmp <<= i << 3;
                lastBlock |= tmp;
                ++i;
            }

            xorkey += lastBlock;
            lastBlock ^= xorkey;
        
            // write remaining 8/16/24-bit value
            i = 0;
            while( i < bAlign )
            {
                srcBuffer[pos + i] = (byte)lastBlock;
                lastBlock >>= 8;
                ++i;
            }
        }
        if (xorkey != basexorkey)
            return false;
        return true;
    }
    
    public static int scramble(byte[] srcBuffer)
    {
        int xorkey = basexorkey;
        int dBlocks = srcBuffer.length >> 2;
        // scramble the remaining data (1-3 bytes)
        byte bAlign = (byte)(srcBuffer.length & 3);
        int pos = dBlocks * 4;
        if(bAlign > 0)
        {
            int lastBlock;
            int i;

            lastBlock = 0;
            i = 0;

            // read remaining 8/16/24-bit value
            while( i < bAlign )
            {
                int tmp = srcBuffer[pos + i] & 0xFF;
                tmp <<= i << 3;
                lastBlock |= tmp;
                ++i;
            }
            
            while( i < 4 )
            {
                int tmp = missbytes[i] & 0xFF;
                tmp <<= i << 3;
                lastBlock |= tmp;
                ++i;
            }

            lastBlock ^= xorkey;
            xorkey -= lastBlock;
                    
            // write remaining 8/16/24-bit value
            i = 0;
            while(i < bAlign)
            {
                srcBuffer[pos + i] = (byte)lastBlock;
                lastBlock >>= 8;
                ++i;
            }
        }
        
        // scramble the 32-bit blocks
        //xorkey = basexorkey;
        pos -= 4;
        while(dBlocks > 0)
        {
            int tmp = readInt(srcBuffer, pos); // read 32-bit value
        
            tmp ^= xorkey;
            xorkey -= tmp;
                    
            System.arraycopy(getIntToByte(tmp), 0, srcBuffer, pos, 4); // write 32-bit value
            pos -= 4;
            --dBlocks;
        }
        return xorkey;
    }
    
    public static void parsedir(ArrayList<String> list, String dir, String from)
    {
        try
        {
            for (File f : new File(dir).listFiles())
            {
                if (f.isDirectory())
                {
                    String res = f.getName();
                    res = new StringBuilder().append(f.getParent()).append("/").append(res).toString();
                    parsedir(list, res, from);
                }
                else
                {
                    String res = f.getName();
                    res = new StringBuilder().append(f.getParent().replace("\\", "/")).append("/").append(res).toString().replace(from, "");
                    list.add(res);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static int readInt(byte[] buf, int pos)
    {
        int ch1 = buf[pos + 0] & 0xFF;
        int ch2 = buf[pos + 1] & 0xFF;
        int ch3 = buf[pos + 2] & 0xFF;
        int ch4 = buf[pos + 3] & 0xFF;
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }
    
    public static int readInt(byte[] buf) throws EOFException
    {
        int ch1 = buf[0] & 0xFF;
        int ch2 = buf[1] & 0xFF;
        int ch3 = buf[2] & 0xFF;
        int ch4 = buf[3] & 0xFF;
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }

    public static int readUShort(RandomAccessFile raf) throws IOException
    {
        byte[] b = new byte[2];
        raf.read(b);
        return readUShort(b);
    }

    public static int readUShort(byte[] buf) throws EOFException
    {
        int ch1 = buf[0] & 0xFF;
        int ch2 = buf[1] & 0xFF;
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return ((ch2 << 8) + ch1) & 0xFFFF;
    }

    public static byte[] getIntToByte(int number)
    {
        byte[] buf = new byte[4];
        buf[0] = (byte) (number & 0xFF);
        buf[1] = (byte) ((number >>> 8) & 0xFF);
        buf[2] = (byte) ((number >>> 16) & 0xFF);
        buf[3] = (byte) ((number >>> 24) & 0xFF);
        return buf;
    }
    
    public static String calcMD5File(String file)
    {
        String md5hash = "";
        try
        {
            InputStream fis = new FileInputStream(new File(file));
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do
            {
                numRead = fis.read(buffer);
                if (numRead > 0)
                    complete.update(buffer, 0, numRead);
            }
            while (numRead != -1);
            fis.close();
            byte[] b = complete.digest();
            for (int i = 0; i < b.length; i++)
                md5hash = md5hash + Integer.toString((b[i] & 0xFF) + 256, 16).substring(1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return md5hash;
    }
    
    public static String calcMD5File(byte[] data)
    {
        String md5hash = "";
        try
        {
            MessageDigest complete = MessageDigest.getInstance("MD5");
            complete.update(data);
            byte[] b = complete.digest();
            for (int i = 0; i < b.length; i++)
                md5hash = md5hash + Integer.toString((b[i] & 0xFF) + 256, 16).substring(1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return md5hash;
    }
    
    public static byte[] calcMD5File(File file)
    {
        try
        {
            InputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do
            {
                numRead = fis.read(buffer);
                if (numRead > 0)
                    complete.update(buffer, 0, numRead);
            }
            while (numRead != -1);
            fis.close();
            return complete.digest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new byte[0];
    }
    
    public static byte[] decompress(byte[] array)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(array.length);
        Inflater decompressor = new Inflater(false);
        decompressor.setInput(array);
        byte[] buf = new byte[1024];
        while (!decompressor.finished())
        {
            try
            {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            catch (DataFormatException e)
            {
            }
        }
        try
        {
            bos.close();
        }
        catch (IOException e)
        {
        }
        return bos.toByteArray();
    }
}
