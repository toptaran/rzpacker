import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

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
    public short mrfFileIndex;
    public byte comressMethod;
    private int version;

    public static final byte FILEINDEX_ENTRY_COMPRESSED = 0;
    public static final byte FILEINDEX_ENTRY_UNCOMPRESSED = 2;
    public static final byte FILEINDEX_ENTRY_COMPRESSED_METHOD2 = (byte) 0xFC;
    public static final byte FILEINDEX_ENTRY_UNCOMPRESSED_METHOD2 = (byte) 0xFD;
    
    public byte[] filedata; //used for file rewrites

    public final static byte[] EMPTY_DATA = new byte[0];
    
    public MsfEntry(int version)
    {
        this.version = version;
        if (version == 1) {
            comressMethod = FILEINDEX_ENTRY_COMPRESSED;
        } else if (version == 2) {
            comressMethod = FILEINDEX_ENTRY_COMPRESSED_METHOD2;
        }
    }
    
    public MsfEntry(String mrfname, String filename, int version)
    {
        this.version = version;
        if (version == 1) {
            comressMethod = FILEINDEX_ENTRY_COMPRESSED;
        } else if (version == 2) {
            comressMethod = FILEINDEX_ENTRY_COMPRESSED_METHOD2;
        }

        mrfFileName = mrfname;
        mrfFileNameb = mrfFileName.getBytes(Charset.forName("EUC-KR"));
        lenMRFN = (short) mrfFileNameb.length;
        
        fileName = filename;
        fileNameb = fileName.getBytes(Charset.forName("EUC-KR"));
        lenName = (short) fileNameb.length;
    }
    
    public void setMrfName(String mrfname)
    {
        mrfFileName = mrfname;
        mrfFileNameb = mrfFileName.getBytes(Charset.forName("EUC-KR"));
        lenMRFN = (short) mrfFileNameb.length;
    }
    
    public static MsfEntry readEntry(ByteBuffer buf, int version)
    {
        MsfEntry entry = new MsfEntry(version);
        if (version == 1) {
            entry.comressMethod = buf.get();
        }
        entry.size = buf.getInt();
        entry.offset = buf.getInt();
        entry.zsize = buf.getInt();
        entry.xorkey = buf.getInt();
        if (version == 1) {
            entry.lenMRFN = buf.getShort();
            entry.lenName = buf.getShort();
            entry.mrfFileNameb = new byte[entry.lenMRFN];
            buf.get(entry.mrfFileNameb);
            entry.mrfFileName = new String(entry.mrfFileNameb, Charset.forName("EUC-KR"));
            entry.fileNameb = new byte[entry.lenName];
            buf.get(entry.fileNameb);
            entry.fileName = new String(entry.fileNameb, Charset.forName("EUC-KR"));
        } else if (version == 2) {
            entry.mrfFileIndex = buf.getShort();
            entry.comressMethod = buf.get();
            entry.lenName = (short) (buf.getShort() - 1);
            byte xorKey = buf.get();
            entry.fileNameb = new byte[entry.lenName];
            buf.get(entry.fileNameb);
            for(int i = 0; i < entry.lenName; i++) {
                entry.fileNameb[i] = (byte) (entry.fileNameb[i] ^ xorKey);
            }
            entry.fileName = new String(entry.fileNameb, Charset.forName("EUC-KR"));
        }
        return entry;
    }
    
    public MsfEntry writeEntry(ByteBuffer buf)
    {
        if (version == 1) {
            buf.put(comressMethod);
        }
        buf.putInt(size);
        buf.putInt(offset);
        buf.putInt(zsize);
        buf.putInt(xorkey);
        if (version == 1) {
            buf.putShort(lenMRFN);
            buf.putShort(lenName);
            buf.put(mrfFileNameb);
            buf.put(fileNameb);
        } else if (version == 2) {
            buf.putShort(mrfFileIndex);
            buf.put(comressMethod);
            buf.putShort((short) (lenName + 1));
            byte xorKey = (byte) (new Random().nextInt() & 0xFF);
            buf.put(xorKey);
            byte[] encFileNameb = new byte[lenName];
            for(int i = 0; i < lenName; i++) {
                encFileNameb[i] = (byte) (fileNameb[i] ^ xorKey);
            }
            buf.put(encFileNameb);
        }
        return this;
    }
    
    public int getSize()
    {
        if (version == 1) {
            return 21 + lenMRFN + lenName;
        } else if (version == 2) {
            return 22 + lenName;
        }
        return 0;
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

            if (comressMethod == FILEINDEX_ENTRY_COMPRESSED || comressMethod == FILEINDEX_ENTRY_COMPRESSED_METHOD2) {
                FileUtils.unscramble(unc, xorkey);

                //unpack
                try {
                    return Snappy.uncompress(unc);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new byte[0];
                }
            } else {
                return unc;
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
            if (comressMethod == FILEINDEX_ENTRY_COMPRESSED || comressMethod == FILEINDEX_ENTRY_COMPRESSED_METHOD2) {
                data = Snappy.compress(data);
            }
            zsize = data.length;
            if (comressMethod == FILEINDEX_ENTRY_COMPRESSED || comressMethod == FILEINDEX_ENTRY_COMPRESSED_METHOD2) {
                xorkey = FileUtils.scramble(data);
            }
            return data;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
