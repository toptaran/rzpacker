import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.xerial.snappy.Snappy;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class MsfEntry
{
    public int size;            // Uncompressed filesize
    public int offset;          // MRF file position
    public int zsize;           // Size of compressed data
    public int xorkey;          // Secret key used for file scrambling
    public short lenMRFN;       // MRF container name length
    public short lenName;       // Filename length
    public byte[] mrfFileNameb;
    public byte[] fileNameb;
    public String mrfFileName;
    public String fileName;
    
    public byte[] filedata; //used for file rewrites
    
    public MsfEntry()
    {
    }
    
    public MsfEntry(String mrfname, String filename)
    {
        mrfFileName = mrfname;
        mrfFileNameb = mrfFileName.getBytes(Charset.forName("EUC-KR"));
        lenMRFN = (short) mrfFileNameb.length;
        
        fileName = filename;
        fileNameb = fileName.getBytes(Charset.forName("EUC-KR"));
        lenName = (short) fileNameb.length;
    }
    
    public static MsfEntry readEntry(ByteBuffer buf)
    {
        MsfEntry entry = new MsfEntry();
        entry.size = buf.getInt();
        entry.offset = buf.getInt();
        entry.zsize = buf.getInt();
        entry.xorkey = buf.getInt();
        entry.lenMRFN = buf.getShort();
        entry.lenName = buf.getShort();
        entry.mrfFileNameb = new byte[entry.lenMRFN];
        buf.get(entry.mrfFileNameb);
        entry.mrfFileName = new String(entry.mrfFileNameb, Charset.forName("EUC-KR"));
        entry.fileNameb = new byte[entry.lenName];
        buf.get(entry.fileNameb);
        entry.fileName = new String(entry.fileNameb, Charset.forName("EUC-KR"));
        return entry;
    }
    
    public MsfEntry writeEntry(ByteBuffer buf)
    {
        buf.putInt(size);
        buf.putInt(offset);
        buf.putInt(zsize);
        buf.putInt(xorkey);
        buf.putShort(lenMRFN);
        buf.putShort(lenName);
        buf.put(mrfFileNameb);
        buf.put(fileNameb);
        return this;
    }
    
    public int getSize()
    {
        return 20 + lenMRFN + lenName;
    }
    
    public byte[] getFileData(String clientdir)
    {
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile(clientdir + "/" + mrfFileName, "r");
            raf.seek(offset);
            byte[] unc = new byte[zsize];
            raf.read(unc);
            
            FileUtils.unscramble(unc, xorkey);
            
            //unpack
            try
            {
                return Snappy.uncompress(unc);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return new byte[0];
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return new byte[0];
        }
        finally
        {
            if (raf != null)
                try
                {
                    raf.close();
                }
                catch(Exception e)
                {
                }
        }
    }
    
    public byte[] getOriginalFileData(String clientdir)
    {
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile(clientdir + "/" + mrfFileName, "r");
            raf.seek(offset);
            byte[] unc = new byte[zsize];
            raf.read(unc);
            return unc;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return new byte[0];
        }
        finally
        {
            if (raf != null)
                try
                {
                    raf.close();
                }
                catch(Exception e)
                {
                }
        }
    }
    
    public byte[] putFileData(String file)
    {
        try
        {
            FileInputStream fis = new FileInputStream(new File(file));
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            
            size = data.length;
            data = Snappy.compress(data);
            zsize = data.length;
            xorkey = FileUtils.scramble(data);
            return data;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
