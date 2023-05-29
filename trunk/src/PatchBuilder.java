import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
import lzma.sdk.lzma.Encoder;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class PatchBuilder
{
    static byte MPATCHHEADER[] = "MAIET PATCH FILE v2.1\0\0\0\0".getBytes();
    
    public static void makePatch(String newdir, String olddir)
    {
        // first load fileindex
        System.out.println("Loading old fileindex...");
        MsfFile mfold = MsfFile.read(olddir);
        if (mfold == null)
        {
            System.out.println("Old fileindex read/parse error!");
            return;
        }
        System.out.println("Done.");
        
        System.out.println("Loading new fileindex...");
        MsfFile mfnew = MsfFile.read(newdir);
        if (mfnew == null)
        {
            System.out.println("New fileindex read/parse error!");
            return;
        }
        System.out.println("Done.");
        
        File f = new File("./patch.mpf");
        //delete file if exist
        if (f.exists())
            f.delete();
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile(f, "rw");
            //skip size
            raf.write(FileUtils.getIntToByte(0));
            //write header
            raf.write(MPATCHHEADER);
            
            for (String mrfname: mfnew.fileindex.keySet())
            {
                System.out.println("Checking " + mrfname + "...");
                if (FileUtils.calcMD5File(newdir + "/" + mrfname).equals(FileUtils.calcMD5File(olddir + "/" + mrfname)))
                {
                    System.out.println("Same.");
                }
                else
                {
                    System.out.println("Processing");
                    TreeMap<Integer, MsfEntry> toremove = new TreeMap<Integer, MsfEntry>();
                    TreeMap<Integer, MsfEntry> toinsert = new TreeMap<Integer, MsfEntry>();
                    ArrayList<String> newfiles = new ArrayList<String>();
                    ArrayList<String> oldfiles = new ArrayList<String>();
                    ArrayList<String> tnewfiles = new ArrayList<String>();
                    ArrayList<String> toldfiles = new ArrayList<String>();
                    TreeMap<Integer, MsfEntry> melnew = mfnew.fileindex.get(mrfname);
                    TreeMap<Integer, MsfEntry> melold = mfold.fileindex.get(mrfname);
                    for (Integer offset: melnew.keySet())
                    {
                        newfiles.add(melnew.get(offset).fileName);
                        tnewfiles.add(melnew.get(offset).fileName);
                    }
                    if (melold != null)
                        for (Integer offset: melold.keySet())
                        {
                            oldfiles.add(melold.get(offset).fileName);
                            toldfiles.add(melold.get(offset).fileName);
                        }
                    //create unique list with remove and add files
                    for (String str: tnewfiles)
                        oldfiles.remove(str);
                    for (String str: toldfiles)
                        newfiles.remove(str);
                    
                    //clean lists from unique
                    for (String str: oldfiles)
                        toldfiles.remove(str);
                    for (String str: newfiles)
                        tnewfiles.remove(str);
                    
                    // add to list elements with wrong position
                    while(true)
                    {
                        String ename1 = null;
                        String ename2 = null;
                        for(int i = 0; i < tnewfiles.size(); i++)
                        {
                            if (!toldfiles.get(i).equals(tnewfiles.get(i)))
                            {
                                if (toldfiles.get(i).equals(tnewfiles.get(i + 1)))
                                {
                                    ename1 = tnewfiles.get(i);
                                }
                                else if (toldfiles.get(i + 1).equals(tnewfiles.get(i)))
                                {
                                    ename1 = toldfiles.get(i);
                                }
                                else
                                {
                                    ename1 = tnewfiles.get(i);
                                    ename2 = toldfiles.get(i);
                                }
                                break;
                            }
                        }
                        if (ename1 == null && ename2 == null)
                            break;
                        
                        if (ename1 != null)
                        {
                            tnewfiles.remove(ename1);
                            toldfiles.remove(ename1);
                            oldfiles.add(ename1);
                            newfiles.add(ename1);
                        }
                        if (ename2 != null)
                        {
                            tnewfiles.remove(ename2);
                            toldfiles.remove(ename2);
                            oldfiles.add(ename2);
                            newfiles.add(ename2);
                        }
                    }
                    
                    
                    // check for changes
                    for (int i = 0; i < tnewfiles.size(); i++)
                    {
                        MsfEntry me = null;
                        for (MsfEntry te: melnew.values())
                            if (te.fileName.equals(tnewfiles.get(i)))
                            {
                                me = te;
                                break;
                            }
                            
                        if (melold != null)
                            for (MsfEntry te: melold.values())
                                if (te.fileName.equals(toldfiles.get(i)))
                                {
                                    if (me.size != te.size || me.xorkey != te.xorkey || !FileUtils.calcMD5File(me.getOriginalFileData(newdir)).equals(FileUtils.calcMD5File(te.getOriginalFileData(olddir))))
                                    {
                                        oldfiles.add(toldfiles.get(i));
                                        newfiles.add(tnewfiles.get(i));
                                    }
                                    break;
                                }
                    }
                    
                    // adding final elements
                    for (Integer offset: melnew.keySet())
                    {
                        if (newfiles.contains(melnew.get(offset).fileName))
                            toinsert.put(offset, melnew.get(offset));
                    }
                    if (melold != null)
                        for (Integer offset: melold.keySet())
                        {
                            if (oldfiles.contains(melold.get(offset).fileName))
                                toremove.put(offset, melold.get(offset));
                        }
                    
                    File mrffile = new File(newdir + "/" + mrfname);
                    int cursizepos = (int) raf.getFilePointer();
                    // skip size
                    raf.write(FileUtils.getIntToByte(0));
                    // write type
                    raf.writeByte(4);
                    // write new size
                    raf.write(FileUtils.getIntToByte((int) mrffile.length()));
                    // write new md5
                    raf.write(FileUtils.calcMD5File(mrffile));
                    // write name
                    raf.write(FileUtils.getIntToByte(mrfname.replace("\\", "/").getBytes(Charset.forName("UTF-8")).length));
                    raf.write(mrfname.replace("\\", "/").getBytes(Charset.forName("UTF-8")));
                    
                    // first remove from end
                    NavigableMap<Integer, MsfEntry> reversetoremove = toremove.descendingMap();
                    for (Integer offset: reversetoremove.keySet())
                    {
                        //write miniblock size
                        raf.write(FileUtils.getIntToByte(13));
                        // write type
                        raf.writeByte(1);
                        // write offset
                        raf.write(FileUtils.getIntToByte(offset));
                        // write remove size
                        raf.write(FileUtils.getIntToByte(reversetoremove.get(offset).zsize));
                    }
                    
                    // second add from start
                    for (Integer offset: toinsert.keySet())
                    {
                        // compress
                        MsfEntry me = toinsert.get(offset);
                        byte[] data = me.getOriginalFileData(newdir);
                        ByteArrayInputStream bai = new ByteArrayInputStream(data);

                        ByteArrayOutputStream bas = new ByteArrayOutputStream(data.length * 2);

                        Encoder encoder = new Encoder();

                        encoder.setLcLpPb(3, 0, 2);
                        encoder.setDictionarySize(0x10000);
                        encoder.code(bai, bas, -1, -1, null);
                        
                        data = bas.toByteArray();
                        
                        // write miniblock size
                        raf.write(FileUtils.getIntToByte(4 + 1 + 4 + 4 + 4 + data.length));
                        // write type
                        raf.writeByte(2);
                        // write offset
                        raf.write(FileUtils.getIntToByte(offset));
                        // write unpacked size
                        raf.write(FileUtils.getIntToByte(me.zsize));
                        // write packed size
                        raf.write(FileUtils.getIntToByte(data.length));
                        // write packet data
                        raf.write(data);
                    }
                    
                    // write final block size
                    int curpoint = (int) raf.getFilePointer();
                    raf.seek(cursizepos);
                    raf.write(FileUtils.getIntToByte(curpoint - cursizepos));
                    raf.seek(curpoint);
                    
                    System.out.println("Done.");
                }
            }
            
            
            
            // check cur dir files
            ArrayList<String> filesnew = new ArrayList<String>();
            ArrayList<String> filesold = new ArrayList<String>();
            ArrayList<String> filesadd = new ArrayList<String>();
            ArrayList<String> filesremove = new ArrayList<String>();
            ArrayList<String> filesreplace = new ArrayList<String>();
            // list new files
            for (File fl: new File(newdir).listFiles())
            {
                if (!fl.isDirectory())
                {
                    filesnew.add(fl.getName());
                    filesadd.add(fl.getName());
                }
            }
            
            // list old files
            for (File fl: new File(olddir).listFiles())
            {
                if (!fl.isDirectory())
                {
                    filesold.add(fl.getName());
                    filesremove.add(fl.getName());
                }
            }
            
            // make add files
            for (String of: filesold)
                filesadd.remove(of);
            // make remove files
            for (String of: filesnew)
                filesremove.remove(of);
            // make replace files
            for (String of: filesadd)
                filesnew.remove(of);
            for (String of: filesnew)
            {
                if (!FileUtils.calcMD5File(newdir + "/" + of).equals(FileUtils.calcMD5File(olddir + "/" + of)))
                    filesreplace.add(of);
            }
            
            //add files
            System.out.println("Process add files...");
            for (String of: filesadd)
            {
                System.out.println(of);
                File ffile = new File(newdir + "/" + of);
                int cursizepos = (int) raf.getFilePointer();
                // skip size
                raf.write(FileUtils.getIntToByte(0));
                // write type
                raf.writeByte(1);
                // write new size
                raf.write(FileUtils.getIntToByte((int) ffile.length()));
                // write new md5
                raf.write(FileUtils.calcMD5File(ffile));
                // write name
                raf.write(FileUtils.getIntToByte(of.getBytes(Charset.forName("UTF-8")).length));
                raf.write(of.getBytes(Charset.forName("UTF-8")));
                
                byte[] data = new byte[(int) ffile.length()];
                FileInputStream fis = new FileInputStream(ffile);
                fis.read(data);
                fis.close();
                
                ByteArrayInputStream bai = new ByteArrayInputStream(data);

                ByteArrayOutputStream bas = new ByteArrayOutputStream(data.length * 2);

                Encoder encoder = new Encoder();

                encoder.setLcLpPb(3, 0, 2);
                encoder.setDictionarySize(0x10000);
                encoder.code(bai, bas, -1, -1, null);
                        
                data = bas.toByteArray();
                
                // write compressed size
                raf.write(FileUtils.getIntToByte(data.length));
                //write data
                raf.write(data);
                
                // write final block size
                int curpoint = (int) raf.getFilePointer();
                raf.seek(cursizepos);
                raf.write(FileUtils.getIntToByte(curpoint - cursizepos));
                raf.seek(curpoint);
            }
            System.out.println("Done.");
            
            //remove files
            System.out.println("Process remove files...");
            for (String of: filesremove)
            {
                System.out.println(of);
                File ffile = new File(olddir + "/" + of);
                int cursizepos = (int) raf.getFilePointer();
                // skip size
                raf.write(FileUtils.getIntToByte(0));
                // write type
                raf.writeByte(2);
                // write new size
                raf.write(FileUtils.getIntToByte((int) ffile.length()));
                // write new md5
                raf.write(FileUtils.calcMD5File(ffile));
                // write name
                raf.write(FileUtils.getIntToByte(of.getBytes(Charset.forName("UTF-8")).length));
                raf.write(of.getBytes(Charset.forName("UTF-8")));
                                
                // write final block size
                int curpoint = (int) raf.getFilePointer();
                raf.seek(cursizepos);
                raf.write(FileUtils.getIntToByte(curpoint - cursizepos));
                raf.seek(curpoint);
            }
            System.out.println("Done.");
            
            //replace files
            System.out.println("Process replace files...");
            for (String of: filesreplace)
            {
                System.out.println(of);
                File ffile = new File(newdir + "/" + of);
                int cursizepos = (int) raf.getFilePointer();
                // skip size
                raf.write(FileUtils.getIntToByte(0));
                // write type
                raf.writeByte(3);
                // write new size
                raf.write(FileUtils.getIntToByte((int) ffile.length()));
                // write new md5
                raf.write(FileUtils.calcMD5File(ffile));
                // write name
                raf.write(FileUtils.getIntToByte(of.getBytes(Charset.forName("UTF-8")).length));
                raf.write(of.getBytes(Charset.forName("UTF-8")));
                
                int offset = 0;
                byte[] data = new byte[(int) ffile.length() + 4 + 1 + 4 + 4 + 1 + 4 + 4];
                byte[] tmp = FileUtils.getIntToByte((int) ffile.length());
                // new file size
                System.arraycopy(tmp, 0, data, offset, 4);
                offset += 4;
                // remove
                data[offset++] = 0;
                // start pos
                tmp = FileUtils.getIntToByte(0);
                System.arraycopy(tmp, 0, data, offset, 4);
                offset += 4;
                //endpos
                File tfile = new File(olddir + "/" + of);
                tmp = FileUtils.getIntToByte((int) tfile.length());
                System.arraycopy(tmp, 0, data, offset, 4);
                offset += 4;
                //add
                data[offset++] = 1;
                // start pos
                tmp = FileUtils.getIntToByte(0);
                System.arraycopy(tmp, 0, data, offset, 4);
                offset += 4;
                //endpos
                tmp = FileUtils.getIntToByte((int) ffile.length());
                System.arraycopy(tmp, 0, data, offset, 4);
                offset += 4;
                
                FileInputStream fis = new FileInputStream(ffile);
                fis.read(data, offset, (int) ffile.length());
                fis.close();
                
                int size = data.length;
                
                ByteArrayInputStream bai = new ByteArrayInputStream(data);

                ByteArrayOutputStream bas = new ByteArrayOutputStream(data.length * 2);

                Encoder encoder = new Encoder();

                encoder.setLcLpPb(3, 0, 2);
                encoder.setDictionarySize(0x10000);
                encoder.code(bai, bas, -1, -1, null);
                        
                data = bas.toByteArray();
                
                
                // write uncompressed size
                raf.write(FileUtils.getIntToByte(size));
                // write compressed size
                raf.write(FileUtils.getIntToByte(data.length));
                //write data
                raf.write(data);
                
                // write final block size
                int curpoint = (int) raf.getFilePointer();
                raf.seek(cursizepos);
                raf.write(FileUtils.getIntToByte(curpoint - cursizepos));
                raf.seek(curpoint);
            }
            System.out.println("Done.");
            
            // write final size
            int curpoint = (int) raf.getFilePointer();
            raf.seek(0);
            raf.write(FileUtils.getIntToByte(curpoint - MPATCHHEADER.length - 4));
        }
        catch(Exception e)
        {
            e.printStackTrace();
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
}
