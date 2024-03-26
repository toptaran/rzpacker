import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class RZPacker
{
    public static void main (String[] args)
    {
        System.out.println("RZPacker ver 2.0");

        int version = 1;
        ArrayList<String> newargs = new ArrayList<String>();
        for(String str: args) {
            if (str.equalsIgnoreCase("-ver1")) {
                version = 1;
            } else if (str.equalsIgnoreCase("-ver2")) {
                version = 2;
            } else {
                newargs.add(str);
            }
        }
        args = new String[newargs.size()];
        newargs.toArray(args);

        boolean showconfigs = false;
        if (args.length == 0)
        {
            File f = new File("fileindex.msf");
            if (f.exists())
                unpack(".", "./unpacked", version);
            else
                showconfigs = true;
        }
        else if (args.length == 1)
        {
            if (args[0].equalsIgnoreCase("-genkeys"))
            {
                generateKeys(version);
            }
            else
                showconfigs = true;
        }
        else if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("-patch"))
            {
                File f = new File(args[1]);
                if (f.exists() && f.isDirectory())
                    patchClient(f.getAbsolutePath().replace("\\", "/"), version);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-list"))
            {
                File f = new File(args[1]);
                if (f.exists() && f.isDirectory())
                    listFiles(f.getAbsolutePath().replace("\\", "/"), version);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-decrypt"))
            {
                File f = new File(args[1]);
                if (f.exists() && !f.isDirectory())
                    decryptFile(f);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-encrypt"))
            {
                File f = new File(args[1]);
                if (f.exists() && !f.isDirectory())
                    encryptFile(f);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-filehash"))
            {
                File f = new File(args[1]);
                if (f.exists() && f.isDirectory())
                    FileHashBuilder.makeFileHash(f.getAbsolutePath().replace("\\", "/"));
                else
                    showconfigs = true;
            }
        }
        else if (args.length == 3)
        {
            if (args[0].equalsIgnoreCase("-pack"))
            {
                File f = new File(args[1]);
                File f2 = new File(args[2]);
                if (f.exists() && f.isDirectory() && f.exists() && f.isDirectory())
                    pack(f.getAbsolutePath().replace("\\", "/"), f2.getAbsolutePath().replace("\\", "/"), version);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-replace"))
            {
                File f = new File(args[1]);
                File f2 = new File(args[2]);
                if (f.exists() && f.isDirectory() && f.exists() && f.isDirectory())
                    replace(f.getAbsolutePath().replace("\\", "/"), f2.getAbsolutePath().replace("\\", "/"), version);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-unpack"))
            {
                File f = new File(args[1]);
                File f2 = new File(args[2]);
                if (f.exists() && f.isDirectory() && (!f.exists() || f.isDirectory()))
                    unpack(f.getAbsolutePath().replace("\\", "/"), f2.getAbsolutePath().replace("\\", "/"), version);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-buildpatch"))
            {
                File f = new File(args[1]);
                File f2 = new File(args[2]);
                if (f.exists() && f.isDirectory() && (!f.exists() || f.isDirectory()))
                    PatchBuilder.makePatch(f.getAbsolutePath().replace("\\", "/"), f2.getAbsolutePath().replace("\\", "/"), version);
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-unpackpwe"))
            {
                File f = new File(args[1]);
                File f2 = new File(args[2]);
                if (f.exists() && f.isDirectory() && (!f.exists() || f.isDirectory()))
                    unpackPWE(f.getAbsolutePath().replace("\\", "/"), f2.getAbsolutePath().replace("\\", "/"));
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-unpackpmang"))
            {
                File f = new File(args[1]);
                File f2 = new File(args[2]);
                if (f.exists() && f.isDirectory() && (!f.exists() || f.isDirectory()))
                    unpackPmang(f.getAbsolutePath().replace("\\", "/"), f2.getAbsolutePath().replace("\\", "/"));
                else
                    showconfigs = true;
            }
            else if (args[0].equalsIgnoreCase("-unpackpwever2"))
            {
                File f = new File(args[1]);
                File f2 = new File(args[2]);
                if (f.exists() && f.isDirectory() && (!f.exists() || f.isDirectory()))
                    unpackPWEVer2(f.getAbsolutePath().replace("\\", "/"), f2.getAbsolutePath().replace("\\", "/"));
                else
                    showconfigs = true;
            }
            else
                showconfigs = true;
        }
        if (showconfigs)
        {
            System.out.println("USAGE:");
            System.out.println("-- version select -ver1 or -ver2(2014 year), can be added at any place, default ver1");
            System.out.println();
            System.out.println("-- pack data, clientdir - where is packed files will save, filesdir - where is dir Data present with files");
            System.out.println("-pack [clientdir] [filesdir]");
            System.out.println();
            System.out.println("-- replace data, clientdir - where is fileindex present, filesdir - where is dir Data present with new files");
            System.out.println("-replace [clientdir] [filesdir]");
            System.out.println();
            System.out.println("-- unpack data, clientdir - where is fileindex present, filesdir - where is unpack files");
            System.out.println("-unpack [clientdir] [outdir]");
            System.out.println();
            System.out.println("-- patch all *.exe, *.dat and Fileindex.msf  and change crypt to our, clientdir - where is fileindex present");
            System.out.println("-patch [clientdir]");
            System.out.println();
            System.out.println("-- list all files from Fileindex.msf");
            System.out.println("-list [clientdir]");
            System.out.println();
            System.out.println("-- decrypt file (for example: buildver.mvf, RaiderZ Launcher.dat, recovery.dat) and save it to filename.txt");
            System.out.println("-decrypt [file]");
            System.out.println();
            System.out.println("-- encrypt file (for example: buildver.mvf, RaiderZ Launcher.dat, recovery.dat) and save it to filename.dat");
            System.out.println("-encrypt [file]");
            System.out.println();
            System.out.println("-- make hash of client files (filehash.msf)");
            System.out.println("-filehash [clientdir]");
            System.out.println();
            System.out.println("-- build patch (before build patch do not forget to make hash)");
            System.out.println("-buildpatch [newclientdir] [oldclientdir]");
            System.out.println();
            System.out.println("-- generate new private and public keys");
            System.out.println("-genkeys");
        }
    }

    public static void pack(String clientdir, String filesdir, int version)
    {
        MsfFile mf = new MsfFile(version);

        System.out.println("Loading files...");
        ArrayList<String> files = new ArrayList<String>();
        FileUtils.parsedir(files, filesdir, filesdir + "/");
        System.out.println("Done.");

        new File(clientdir + "/Data/").mkdirs();
        System.out.println("Process pack...");
        ArrayList<String> filesf = new ArrayList<String>();
        TreeMap<String, TreeMap<String, String>> mrfreplace = new TreeMap<String, TreeMap<String, String>>();

        System.out.println("Process adding...");
        mrfreplace = new TreeMap<String, TreeMap<String, String>>();
        //settting marks for mrf files
        for (String file: files)
        {
            String filemrf = "";
            String filename = "";
            if (version == 1) {
                filemrf = file.substring(0, file.indexOf("/", file.indexOf("/") + 1));
                filename = file.substring(file.indexOf("/", file.indexOf("/") + 1) + 1);
            } else if (version == 2) {
                if (file.toLowerCase().startsWith("data/")) {
                    filemrf = file.substring(0, file.indexOf("/", file.indexOf("/") + 1));
                } else {
                    filemrf = "Data/" + file.substring(0, file.indexOf("/"));
                }
                filemrf = filemrf.replace("/", "\\");
                filename = file.replace("/", "\\");
            }

            String mrfnametemp = "";
            for (String mrfname: mf.fileindex.keySet())
            {
                if (mrfname.startsWith(filemrf))
                    mrfnametemp = mrfname;
            }
            if (mrfnametemp.length() == 0)
                mrfnametemp = filemrf + ".mrf";
            TreeMap<String, String> fls = mrfreplace.get(mrfnametemp);
            if (fls == null)
            {
                fls = new TreeMap<String, String>();
                mrfreplace.put(mrfnametemp, fls);
            }
            fls.put(filename, file);
        }

        if (mrfreplace.isEmpty())
        {
            System.out.println("No one file found to add");
        }
        else
        {
            System.out.println("Files will be add:");
            for (String mrfname: mrfreplace.keySet())
            {
                System.out.println(mrfname);
                TreeMap<String, String> fls = mrfreplace.get(mrfname);
                for (String rpfile: fls.keySet())
                    System.out.println("  "+rpfile);
            }

            for (String mrfname: mrfreplace.keySet())
            {
                System.out.println("Write " + mrfname + "...");
                TreeMap<String, String> fls = mrfreplace.get(mrfname);

                if (!mf.fileindex.containsKey(mrfname))
                    mf.fileindex.put(mrfname, new MrfFile(mrfname));

                MrfFile mrfindex = mf.fileindex.get(mrfname);
                int offsetmax = 0;
                for (MsfEntry me: mrfindex.getFiles().values())
                {
                    if (me.filedata == null)
                    {
                        me.filedata = me.getOriginalFileData(clientdir);
                        if (offsetmax < me.offset)
                            offsetmax = me.offset;
                    }
                }
                for (String rpfile: fls.keySet())
                {
                    offsetmax++;
                    MsfEntry me = new MsfEntry(mrfname, rpfile, version);
                    me.filedata = me.putFileData(filesdir + "/" + fls.get(rpfile));

                    if (mrfindex.getSize() + me.zsize < 200*1024*1024)
                    {
                        me.offset = offsetmax;
                        mrfindex.addFile(me);
                    }
                    else
                    {
                        System.out.println("Done.");
                        System.out.println("Save " + mrfname + "...");
                        mrfindex.save(clientdir);
                        System.out.println("Done.");

                        String newmrf = mrfname.substring(0, mrfname.lastIndexOf("."));
                        int newnumber = 1;
                        if (!mrfname.endsWith(".mrf"))
                        {
                            newnumber = Integer.parseInt(mrfname.substring(mrfname.indexOf(".")+1))+1;
                        }
                        newmrf = newmrf + String.format(".%03d", newnumber);

                        mrfname = newmrf;
                        System.out.println("moved to " + mrfname);

                        System.out.println("Write " + mrfname + "...");
                        mf.fileindex.put(mrfname, new MrfFile(mrfname));
                        mrfindex = mf.fileindex.get(mrfname);
                        offsetmax = 0;

                        me.setMrfName(mrfname);
                        me.offset = offsetmax;
                        mrfindex.addFile(me);
                    }
                }

                System.out.println("Done.");
                System.out.println("Save " + mrfname + "...");
                mrfindex.save(clientdir);
                System.out.println("Done.");
            }
        }

        System.out.println("Saving fileindex...");
        if (!mf.save(clientdir))
        {
            System.out.println("Fileindex save error!");
            return;
        }
        System.out.println("Done.");
    }

    public static void replace(String clientdir, String filesdir, int version)
    {
        System.out.println("Loading fileindex...");
        MsfFile mf = MsfFile.read(clientdir, version);
        if (mf == null)
        {
            System.out.println("Fileindex read/parse error!");
            return;
        }
        System.out.println("Done.");
        
        System.out.println("Loading files...");
        ArrayList<String> files = new ArrayList<String>();
        FileUtils.parsedir(files, filesdir, filesdir + "/");
        System.out.println("Done.");
        
        System.out.println("Process replacing...");
        ArrayList<String> filesf = new ArrayList<String>();
        TreeMap<String, TreeMap<String, String>> mrfreplace = new TreeMap<String, TreeMap<String, String>>();
        //settting marks for mrf files
        for (String file: files)
        {
            String filemrf = "";
            String filename = "";
            if (version == 1) {
                filemrf = file.substring(0, file.indexOf("/", file.indexOf("/") + 1));
                filename = file.substring(file.indexOf("/", file.indexOf("/") + 1) + 1);
            } else if (version == 2) {
                if (file.toLowerCase().startsWith("data/")) {
                    filemrf = file.substring(0, file.indexOf("/", file.indexOf("/") + 1));
                } else {
                    filemrf = "Data/" + file.substring(0, file.indexOf("/"));
                }
                filemrf = filemrf.replace("/", "\\");
                filename = file.replace("/", "\\");
            }
            for (String mrfname: mf.fileindex.keySet())
            {
                boolean founded = false;
                if (mrfname.startsWith(filemrf))
                {
                    TreeMap<Integer, MsfEntry> mrfindex = mf.fileindex.get(mrfname).getFiles();
                    for (MsfEntry me: mrfindex.values())
                    {
                        if (me.fileName.equalsIgnoreCase(filename))
                        {
                            founded = true;
                            // setmark
                            me.filedata = new byte[0];
                            TreeMap<String, String> fls = mrfreplace.get(mrfname);
                            if (fls == null)
                            {
                                fls = new TreeMap<String, String>();
                                mrfreplace.put(mrfname, fls);
                            }
                            fls.put(me.fileName, file);
                            filesf.add(file);
                            break;
                        }
                    }
                }
                if (founded)
                    break;
            }
        }
        
        for (String file: filesf)
        {
            files.remove(file);
        }

        if (mrfreplace.isEmpty())
        {
            System.out.println("No one file found to replace");
        }
        else
        {
            System.out.println("Files will be replaced:");
            for (String mrfname: mrfreplace.keySet())
            {
                System.out.println(mrfname);
                TreeMap<String, String> fls = mrfreplace.get(mrfname);
                for (String rpfile: fls.keySet())
                    System.out.println("  "+rpfile);
            }

            for (String mrfname: mrfreplace.keySet())
            {
                System.out.println("Read " + mrfname + "...");
                TreeMap<String, String> fls = mrfreplace.get(mrfname);
                MrfFile mrfindex = mf.fileindex.get(mrfname);
                for (MsfEntry me: mrfindex.getFiles().values())
                {
                    if (me.filedata == null)
                    {
                        me.filedata = me.getOriginalFileData(clientdir);
                    }
                    else
                    {
                        for (String rpfile: fls.keySet())
                        {
                            if (rpfile.equals(me.fileName))
                            {
                                me.filedata = me.putFileData(filesdir + "/" + fls.get(rpfile));
                            }
                        }
                    }
                }
                System.out.println("Done.");
                System.out.println("Write " + mrfname + "...");
                mrfindex.save(clientdir);
                System.out.println("Done.");
            }
        }

        System.out.println("Process adding...");
        mrfreplace = new TreeMap<String, TreeMap<String, String>>();
        //settting marks for mrf files
        for (String file: files)
        {
            String filemrf = "";
            String filename = "";
            if (version == 1) {
                filemrf = file.substring(0, file.indexOf("/", file.indexOf("/") + 1));
                filename = file.substring(file.indexOf("/", file.indexOf("/") + 1) + 1);
            } else if (version == 2) {
                if (file.toLowerCase().startsWith("data/")) {
                    filemrf = file.substring(0, file.indexOf("/", file.indexOf("/") + 1));
                } else {
                    filemrf = "Data/" + file.substring(0, file.indexOf("/"));
                }
                filemrf = filemrf.replace("/", "\\");
                filename = file.replace("/", "\\");
            }
            String mrfnametemp = "";
            int maxmrfnum = 0;
            for (String mrfname: mf.fileindex.keySet())
            {
                if (mrfname.startsWith(filemrf))
                {
                    int newnumber = 0;
                    if (!mrfname.endsWith(".mrf"))
                    {
                        newnumber = Integer.parseInt(mrfname.substring(mrfname.indexOf(".")+1));
                    }
                    if (maxmrfnum < newnumber)
                    {
                        maxmrfnum = newnumber;
                        mrfnametemp = filemrf + String.format(".%03d", newnumber);
                    }
                }
            }
            if (mrfnametemp.length() == 0)
                mrfnametemp = filemrf + ".mrf";
            TreeMap<String, String> fls = mrfreplace.get(mrfnametemp);
            if (fls == null)
            {
                fls = new TreeMap<String, String>();
                mrfreplace.put(mrfnametemp, fls);
            }
            fls.put(filename, file);
        }

        if (mrfreplace.isEmpty())
        {
            System.out.println("No one file found to add");
        }
        else
        {
            System.out.println("Files will be add:");
            for (String mrfname: mrfreplace.keySet())
            {
                System.out.println(mrfname);
                TreeMap<String, String> fls = mrfreplace.get(mrfname);
                for (String rpfile: fls.keySet())
                    System.out.println("  "+rpfile);
            }

            for (String mrfname: mrfreplace.keySet())
            {
                System.out.println("Write " + mrfname + "...");
                TreeMap<String, String> fls = mrfreplace.get(mrfname);
                
                if (!mf.fileindex.containsKey(mrfname))
                    mf.fileindex.put(mrfname, new MrfFile(mrfname));
                
                MrfFile mrfindex = mf.fileindex.get(mrfname);
                int offsetmax = 0;
                for (MsfEntry me: mrfindex.getFiles().values())
                {
                    if (me.filedata == null)
                    {
                        me.filedata = me.getOriginalFileData(clientdir);
                        if (offsetmax < me.offset)
                            offsetmax = me.offset;
                    }
                }
                for (String rpfile: fls.keySet())
                {
                    offsetmax++;
                    MsfEntry me = new MsfEntry(mrfname, rpfile, version);
                    me.filedata = me.putFileData(filesdir + "/" + fls.get(rpfile));
                    if (mrfindex.getSize() + me.zsize < 200*1024*1024)
                    {
                        me.offset = offsetmax;
                        mrfindex.addFile(me);
                    }
                    else
                    {
                        System.out.println("Done.");
                        System.out.println("Save " + mrfname + "...");
                        mrfindex.save(clientdir);
                        System.out.println("Done.");

                        String newmrf = mrfname.substring(0, mrfname.lastIndexOf("."));
                        int newnumber = 1;
                        if (!mrfname.endsWith(".mrf"))
                        {
                            newnumber = Integer.parseInt(mrfname.substring(mrfname.indexOf(".")+1))+1;
                        }
                        newmrf = newmrf + String.format(".%03d", newnumber);

                        mrfname = newmrf;
                        System.out.println("moved to " + mrfname);

                        System.out.println("Write " + mrfname + "...");
                        mf.fileindex.put(mrfname, new MrfFile(mrfname));
                        mrfindex = mf.fileindex.get(mrfname);
                        offsetmax = 0;

                        me.setMrfName(mrfname);
                        me.offset = offsetmax;
                        mrfindex.addFile(me);
                    }
                }
                
                System.out.println("Done.");
                System.out.println("Save " + mrfname + "...");
                mrfindex.save(clientdir);
                System.out.println("Done.");
            }
        }
        
        System.out.println("Saving fileindex...");
        if (!mf.save(clientdir))
        {
            System.out.println("Fileindex save error!");
            return;
        }
        System.out.println("Done.");
    }
    
    public static void unpack(String clientdir, String outdir, int version)
    {
        File f = new File(outdir);
        if (!f.exists())
            f.mkdirs();
        System.out.println("Loading fileindex...");
        MsfFile mf = MsfFile.read(clientdir, version);
        if (mf == null)
        {
            System.out.println("Fileindex read/parse error!");
            return;
        }
        System.out.println("Done.");
        
        //calculate filecount
        int count = 0;
        for (MrfFile mrfindex: mf.fileindex.values())
            count += mrfindex.getCount();
        System.out.println("Unpacking " + count + " files...");
        int curcount = 0;
        for (String mrfname: mf.fileindex.keySet())
        {
            String mrffolder = outdir;
            if (version == 1) {
                mrffolder += "/" + mrfname.substring(0, mrfname.lastIndexOf("."));
            }
            TreeMap<Integer, MsfEntry> mrfindex = mf.fileindex.get(mrfname).getFiles();
            for (MsfEntry me: mrfindex.values())
            {
                String fname = me.fileName.replace("\\", "/");
                if (fname.indexOf("/") > 0)
                    new File(mrffolder + "/" + fname.substring(0, fname.lastIndexOf("/"))).mkdirs();
                else
                    new File(mrffolder + "/").mkdirs();
                File file = new File(mrffolder + "/" + fname);
                FileOutputStream fos = null;
                try
                {
                    fos = new FileOutputStream(file);
                    fos.write(me.getFileData(clientdir));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
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
                curcount++;
                if (curcount % 100 == 0)
                    System.out.println("Processed [" + curcount + "/" + count + "] files.");
            }
        }
        System.out.println("Done.");
    }
    
    public static void unpackPWE(String clientdir, String outdir)
    {
        EciesCryptoPP.privateKey = EciesCryptoPP.PWEKey;
        unpack(clientdir, outdir, 1);
    }
    
    public static void unpackPmang(String clientdir, String outdir)
    {
        EciesCryptoPP.privateKey = EciesCryptoPP.PmangKey;
        unpack(clientdir, outdir, 1);
    }

    public static void unpackPWEVer2(String clientdir, String outdir)
    {
        EciesCryptoPP.privateKey = EciesCryptoPP.NAKeyMethod2;
        unpack(clientdir, outdir, 2);
    }
    
    public static void patchClient(String clientdir, int version)
    {
        changeEncryption(clientdir + "/" + "buildver.mvf", EciesCryptoPP.PWEKeyBuildver, EciesCryptoPP.publicKey, version);
        if (version == 1) {
            changeEncryption(clientdir + "/" + "fileindex.msf", EciesCryptoPP.PWEKey, EciesCryptoPP.publicKey, version);
            changeEncryption(clientdir + "/" + "RaiderZ Launcher.dat", EciesCryptoPP.PWEKeyUpdaterConf, EciesCryptoPP.publicKey, version);
            changeEncryption(clientdir + "/" + "recovery.dat", EciesCryptoPP.PWEKeyUpdaterConf, EciesCryptoPP.publicKey, version);

            replaceKey(clientdir + "/" + "Raiderz.exe", EciesCryptoPP.PWEKey, EciesCryptoPP.privateKey);

            replaceKey(clientdir + "/" + "Raiderz Launcher.exe", EciesCryptoPP.PWEKeyUpdaterConf, EciesCryptoPP.privateKey);
            replaceKey(clientdir + "/" + "recovery.exe", EciesCryptoPP.PWEKeyUpdaterConf, EciesCryptoPP.privateKey);
        } else if (version == 2) {
            changeEncryption(clientdir + "/" + "fileindex.msf", EciesCryptoPP.NAKeyMethod2, EciesCryptoPP.publicKeyVer2, version);
            changeEncryption(clientdir + "/" + "RaiderZ Launcher.dat", EciesCryptoPP.NAKeyMethod2UpdaterConf, EciesCryptoPP.publicKey, version);
            changeEncryption(clientdir + "/" + "recovery.dat", EciesCryptoPP.NAKeyMethod2UpdaterConf, EciesCryptoPP.publicKey, version);

            replaceKey(clientdir + "/" + "Raiderz Launcher.exe", EciesCryptoPP.NAKeyMethod2UpdaterConf, EciesCryptoPP.privateKey);
            replaceKey(clientdir + "/" + "recovery.exe", EciesCryptoPP.NAKeyMethod2UpdaterConf, EciesCryptoPP.privateKey);

            System.out.println("Patch Raiderz.exe file...");
            RandomAccessFile raf = null;
            try
            {
                raf = new RandomAccessFile(clientdir + "/" + "Raiderz.exe", "rw");
                raf.seek(0x4370);
                short[] patchShort = new short[]{
                        0x83, 0xEC, 0x0C, 0xB8, 0x03, 0x02, 0x00, 0x00, 0x8B, 0x54, 0x24, 0x14, 0x89, 0x02, 0xB8, 0x60,
                        0x10, 0x40, 0x00, 0x8B, 0x54, 0x24, 0x10, 0x89, 0x02, 0xB0, 0x01, 0x83, 0xC4, 0x0C, 0xC3, 0xCC
                };
                byte[] patch = new byte[patchShort.length];
                for (int i = 0; i < patchShort.length; i++)
                    patch[i] = (byte) patchShort[i];
                raf.write(patch);
                raf.seek(0x460);
                raf.write(EciesCryptoPP.privateKeyVer2);
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
            System.out.println("Done.");
        }

        replaceKey(clientdir + "/" + "Raiderz Launcher.exe", EciesCryptoPP.PWEKeyBuildver, EciesCryptoPP.privateKey);
        replaceKey(clientdir + "/" + "recovery.exe", EciesCryptoPP.PWEKeyBuildver, EciesCryptoPP.privateKey);
        
        replaceKey(clientdir + "/" + "Raiderz Launcher.exe", EciesCryptoPP.PWEKeyLauncherHz, EciesCryptoPP.privateKey);
        replaceKey(clientdir + "/" + "recovery.exe", EciesCryptoPP.PWEKeyFileHash, EciesCryptoPP.privateKey);
    }
    
    public static boolean changeEncryption(String file, byte[] oldKey, byte[] newKey, int version)
    {
        File f = new File(file);
        if (!f.exists())
            return false;
            boolean allisok = true;
            System.out.println("Read " + f.getName() + " file...");
            FileInputStream fis = null;
            byte[] data = new byte[0];
            try
            {
                fis = new FileInputStream(f);
                if (version == 2 && file.endsWith("fileindex.msf")) {
                    fis.skip(3);//skip trash
                }
                data = new byte[fis.available()];
                fis.read(data);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Read error!");
                allisok = false;
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

            if (allisok)
            {
                System.out.println("Decrypt...");
                data = EciesCryptoPP.decrypt(oldKey, oldKey.length, data, data.length);
                System.out.println("Encrypt...");
                data = EciesCryptoPP.encrypt(newKey, newKey.length, data, data.length);
            }

            System.out.println("Save file...");
            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(f);
                if (version == 2 && file.endsWith("fileindex.msf")) {
                    fos.write(0);//write trash
                    fos.write(0);//write trash
                    fos.write(0);//write trash
                }
                fos.write(data);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Save error!");
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
            System.out.println("Done.");
            return true;
    }
    
    public static boolean replaceKey(String file, byte[] oldkey, byte[] newkey)
    {
        File f = new File(file);
        if (!f.exists())
            return false;
            System.out.println("Patch " + f.getName() + " file...");
            System.out.println("Read file...");
            FileInputStream fis = null;
            byte[] exedata = new byte[0];
            boolean allisok = true;
            try
            {
                fis = new FileInputStream(f);
                exedata = new byte[fis.available()];
                fis.read(exedata);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Read error!");
                allisok = false;
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
            
            if (!allisok)
            {
                return false;
            }

            System.out.println("Searching key...");
            int i = 0;
            for (;i < exedata.length; i++)
            {
                if (exedata[i] == oldkey[0])
                {
                    boolean founded = true;
                    for (int j = 0; j < oldkey.length; j++)
                    {
                        if (exedata[i + j] != oldkey[j])
                        {
                            founded = false;
                            break;
                        }
                    }
                    if (founded)
                        break;
                }
            }
            if (i == exedata.length)
            {
                System.out.println("Key not found!");
                return false;
            }
            System.out.println("Patching...");
            System.arraycopy(newkey, 0, exedata, i, newkey.length);

            System.out.println("Save exe file...");
            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(f);
                fos.write(exedata);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Save error!");
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
            System.out.println("Done.");
            return true;
    }
    
    public static void listFiles(String clientdir, int version)
    {
        System.out.println("Loading fileindex...");
        MsfFile mf = MsfFile.read(clientdir, version);
        if (mf == null)
        {
            System.out.println("Fileindex read/parse error!");
            return;
        }
        System.out.println("Done.");
        
        StringBuilder sb = new StringBuilder();
        sb.append("mrffilename").append(" ").append("filename").append(" ").append("size").append(" ").append("checksumm").append("\r\n");
        int count = 0;
        for (String mrfname: mf.fileindex.keySet())
        {
            TreeMap<Integer, MsfEntry> mrfindex = mf.fileindex.get(mrfname).getFiles();
            for (MsfEntry me: mrfindex.values())
            {
                sb.append(me.mrfFileName).append(" ").append(me.fileName).append(" ").append(me.size).append(" ").append(me.xorkey).append("\r\n");
                count++;
            }
        }
        System.out.println("Processed [" + count + "] files.");
        
        System.out.println("Saving to fileindex.txt...");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(new File(clientdir + "/fileindex.txt"));
            fos.write(sb.toString().getBytes(Charset.forName("UTF-8")));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Save error!");
            return;
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
        System.out.println("Done.");
    }
    
    public static boolean decryptFile(File f)
    {
            boolean allisok = true;
            System.out.println("Read " + f.getName() + " file...");
            FileInputStream fis = null;
            byte[] data = new byte[0];
            try
            {
                fis = new FileInputStream(f);
                data = new byte[fis.available()];
                fis.read(data);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Read error!");
                allisok = false;
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

            if (allisok)
            {
                System.out.println("Decrypt...");
                data = EciesCryptoPP.decrypt(data);
            }

            System.out.println("Save file to " + f.getName() + ".txt ...");
            f = new File(f.getAbsolutePath() + ".txt");
            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(f);
                fos.write(data);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Save error!");
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
            System.out.println("Done.");
            return true;
    }
    
    public static boolean encryptFile(File f)
    {
            boolean allisok = true;
            System.out.println("Read " + f.getName() + " file...");
            FileInputStream fis = null;
            byte[] data = new byte[0];
            try
            {
                fis = new FileInputStream(f);
                data = new byte[fis.available()];
                fis.read(data);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Read error!");
                allisok = false;
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

            if (allisok)
            {
                System.out.println("Encrypt...");
                data = EciesCryptoPP.encrypt(data);
            }

            System.out.println("Save file to " + f.getName() + ".dat ...");
            f = new File(f.getAbsolutePath() + ".dat");
            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(f);
                fos.write(data);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                System.out.println("Save error!");
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
            System.out.println("Done.");
            return true;
    }
        
    public static void generateKeys(int version)
    {
        if (EciesCryptoPP.generateAndSaveKeys(1)) {
            if (version == 1 || (version == 2 && EciesCryptoPP.generateAndSaveKeys(2))) {
                System.out.println("Keys successfully generated and saved!");
            } else {
                System.out.println("Ver2 save error!");
            }
        } else {
            System.out.println("Save error!");
        }
    }
}
