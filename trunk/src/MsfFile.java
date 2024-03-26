import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.xerial.snappy.Snappy;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class MsfFile
{
    public final int version;

    public MsfFile(int version)
    {
        this.version = version;
    }

    public TreeMap<String, MrfFile> fileindex = new TreeMap<String, MrfFile>();

    public static MsfFile read(String clientdir, int version)
    {
        if (version == 1) {
            EciesCryptoPP.privateKey = EciesCryptoPP.privateKeyVer1;
        } else if (version == 2) {
            EciesCryptoPP.privateKey = EciesCryptoPP.privateKeyVer2;
        }
        MsfFile mf = new MsfFile(version);
        File f = new File(clientdir + "/" + "fileindex.msf");
        FileInputStream fis = null;
        byte[] filemsfdata = new byte[0];
        try
        {
            fis = new FileInputStream(f);
            if (version == 2) {
                fis.skip(3); //skip trash bytes
            }
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

        String[] mrfFileNames = new String[0];
        if (version == 2) {
            int mrfFileCount = msf.getShort();
            mrfFileNames = new String[mrfFileCount];
            for (int i = 0; i < mrfFileCount; i++) {
                int mrfFileNameLen = msf.getShort() - 1;
                byte xorKey = msf.get();
                byte[] mrfFileNameb = new byte[mrfFileNameLen];
                msf.get(mrfFileNameb);
                for (int j = 0; j < mrfFileNameLen; j++) {
                    mrfFileNameb[j] = (byte) (mrfFileNameb[j] ^ xorKey);
                }
                mrfFileNames[i] = new String(mrfFileNameb, Charset.forName("EUC-KR"));
            }
        }

        while(msf.hasRemaining())
        {
            MsfEntry me = MsfEntry.readEntry(msf, version);
            if (version == 2) {
                me.setMrfName(mrfFileNames[me.mrfFileIndex]);
            }
            MrfFile mrfindex = mf.fileindex.get(me.mrfFileName);
            if (mrfindex == null)
            {
                mrfindex = new MrfFile(me.mrfFileName);
                mf.fileindex.put(me.mrfFileName, mrfindex);
            }
            mrfindex.addFile(me);
        }
        return mf;
    }
    
    public boolean save(String clientdir)
    {
        if (version == 1) {
            EciesCryptoPP.publicKey = EciesCryptoPP.publicKeyVer1;
        } else if (version == 2) {
            EciesCryptoPP.publicKey = EciesCryptoPP.publicKeyVer2;
        }

        int msfsize = getSize();
        if (version == 2) {
            msfsize += 2;
            for (String mrfFileName: fileindex.keySet()) {
                msfsize += 3 + mrfFileName.getBytes(Charset.forName("EUC-KR")).length;
            }
        }

        ByteBuffer msf = ByteBuffer.wrap(new byte[msfsize]).order(ByteOrder.LITTLE_ENDIAN);

        if (version == 2) {
            msf.putShort((short) fileindex.size());
            for (String mrfFileName: fileindex.keySet()) {
                byte[] mrfFileNameb = mrfFileName.getBytes(Charset.forName("EUC-KR"));
                msf.putShort((short) (mrfFileNameb.length + 1));
                byte xorKey = (byte) (new Random().nextInt() & 0xFF);
                msf.put(xorKey);
                for(int i = 0; i < mrfFileNameb.length; i++) {
                    mrfFileNameb[i] = (byte) (mrfFileNameb[i] ^ xorKey);
                }
                msf.put(mrfFileNameb);
            }
        }

        short index = 0;
        for (String mrfname: fileindex.keySet())
        {
            MrfFile mrfindex = fileindex.get(mrfname);
            for (Integer offset: mrfindex.getFiles().keySet())
            {
                mrfindex.getFiles().get(offset).mrfFileIndex = index;
                mrfindex.getFiles().get(offset).writeEntry(msf);
            }
            index++;
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

        //encrypt msf
        finalmsfdata = EciesCryptoPP.encrypt(finalmsfdata);

        File f = new File(clientdir + "/" + "fileindex.msf");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(f);
            if (version == 2) {
                fos.write(0); // write trash bytes
                fos.write(0); // write trash bytes
                fos.write(0); // write trash bytes
            }
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
        for (MrfFile mrfindex: fileindex.values())
            for (MsfEntry me: mrfindex.getFiles().values())
                size += me.getSize();
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
