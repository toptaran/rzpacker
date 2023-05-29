import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.xerial.snappy.Snappy;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class MsfFile
{
    public TreeMap<String, TreeMap<Integer, MsfEntry>> fileindex;

    public static MsfFile read(String clientdir)
    {
        MsfFile mf = new MsfFile();
        File f = new File(clientdir + "/" + "fileindex.msf");
        FileInputStream fis = null;
        byte[] filemsfdata = new byte[0];
        try
        {
            fis = new FileInputStream(f);
            filemsfdata = new byte[fis.available()];
            fis.read(filemsfdata);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if (fis != null)
                try
                {
                    fis.close();
                }
                catch(Exception e)
                {
                }
        }
        
        //decript msf
        filemsfdata = EciesCryptoPP.decrypt(filemsfdata);
        if (filemsfdata.length == 0)
        {
            System.out.println("Fileindex decrypt error!");
            return null;
        }
        
        int fileindexsize = FileUtils.readInt(filemsfdata, 0);
        byte[] temp = new byte[filemsfdata.length - 4];
        System.arraycopy(filemsfdata, 4, temp, 0, temp.length);
        filemsfdata = temp;
        //unpack
        try
        {
            filemsfdata = Snappy.uncompress(filemsfdata);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        if (filemsfdata.length != fileindexsize)
        {
            System.out.println("Fileindex unpack error!");
            return null;
        }
        
        ByteBuffer msf = ByteBuffer.wrap(filemsfdata).order(ByteOrder.LITTLE_ENDIAN);
        mf.fileindex = new TreeMap<String, TreeMap<Integer, MsfEntry>>();
        while(msf.hasRemaining())
        {
            msf.get();//skeep compress method
            MsfEntry me = MsfEntry.readEntry(msf);
            TreeMap<Integer, MsfEntry> mrfindex = mf.fileindex.get(me.mrfFileName);
            if (mrfindex == null)
            {
                mrfindex = new TreeMap<Integer, MsfEntry>();
                mf.fileindex.put(me.mrfFileName, mrfindex);
            }
            MsfEntry tme = mrfindex.put(me.offset, me);
            if (tme != null)
            {
                System.out.println("Error entrys has same position: " + tme.fileName + " " + me.fileName);
            }
        }
        return mf;
    }
    
    public boolean save(String clientdir)
    {
        int msfsize = getSize();

        ByteBuffer msf = ByteBuffer.wrap(new byte[msfsize]).order(ByteOrder.LITTLE_ENDIAN);

        for (String mrfname: fileindex.keySet())
        {
            TreeMap<Integer, MsfEntry> mrfindex = fileindex.get(mrfname);
            for (Integer offset: mrfindex.keySet())
            {
                msf.put((byte)0);
                mrfindex.get(offset).writeEntry(msf);
            }
        }

        msf.rewind();
        byte[] filemsfdata = new byte[msf.remaining()];
        msf.get(filemsfdata);

        //pack
        try
        {
            filemsfdata = Snappy.compress(filemsfdata);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        
        //put filesize
        byte[] finalmsfdata = new byte[filemsfdata.length + 4];
        System.arraycopy(FileUtils.getIntToByte(msfsize), 0, finalmsfdata, 0, 4);
        System.arraycopy(filemsfdata, 0, finalmsfdata, 4, filemsfdata.length);

        //encript msf
        finalmsfdata = EciesCryptoPP.encrypt(finalmsfdata);

        File f = new File(clientdir + "/" + "fileindex.msf");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(f);
            fos.write(finalmsfdata);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            if (fos != null)
                try
                {
                    fos.close();
                }
                catch(Exception e)
                {
                }
        }

        return true;
    }
    
    public int getSize()
    {
        if (fileindex == null)
            return 0;
        int size = 0;
        for (TreeMap<Integer, MsfEntry> mrfindex: fileindex.values())
            for (MsfEntry me: mrfindex.values())
                size += 1 + me.getSize();
        return size;
    }

    static String getFileName(RandomAccessFile raf) throws IOException
    {
        int nlen = readInt(raf);
        byte[] data = new byte[nlen];
        raf.read(data);
        return new String(data);
    }

    public static int readInt(RandomAccessFile raf) throws IOException
    {
        byte[] b = new byte[4];
        raf.read(b);
        return FileUtils.readInt(b);
    }

    /*
        z3Rle
    */
    public static byte[] fsRle(byte[] data, int msfSize)
    {
        int[] result = Z3Rle.decodeSize(data);
        if (result.length == 1)
        {
            System.out.println("ERROR: Problems decoding RLE buffer size");
            return new byte[0];
        }
        if (msfSize > 0 && !(msfSize == result[0]))
        {
            System.out.println("ERROR: Unexpected MSF buffer size\n");
            return new byte[0];
        }
        
        // Skip the length of the expected size
        AtomicInteger dataOffset = new AtomicInteger(result[1]);
        
        byte[] buffer = new byte[result[0]];
        AtomicInteger buffOffset = new AtomicInteger(0);
        while(buffOffset.get() < result[0])
        {
            if(!(Z3Rle.decodeInstruction(data, dataOffset, buffer, buffOffset)))
            {
                System.out.println("ERROR: Problems decoding RLE buffer\n");
                return new byte[0];
            }
        }
        return buffer;
    }
}
