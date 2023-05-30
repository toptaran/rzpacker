import java.io.File;
import java.io.FileOutputStream;
import java.util.TreeMap;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class MrfFile
{
    private String name;
    private TreeMap<Integer, MsfEntry> files;
    private int size;
    
    public MrfFile(String name)
    {
        this.name = name;
        this.files = new TreeMap<Integer, MsfEntry>();
        this.size = 0;
    }
    
    public void addFile(MsfEntry me)
    {
        MsfEntry tme = files.put(me.offset, me);
        if (tme != null)
        {
            System.out.println("Error entries has same position: " + tme.fileName + " " + me.fileName);
        }
        size += me.zsize;
    }
    
    public void removeFile(MsfEntry me)
    {
        MsfEntry tme = files.remove(me.offset);
        if (tme == null)
        {
            System.out.println("Error entries has same position: " + tme.fileName + " " + me.fileName);
        }
        size -= me.zsize;
    }

    public void save(String folder) {
        File f = new File(folder + "/" + name);
        FileOutputStream fos = null;
        TreeMap<Integer, MsfEntry> mrffiles = files;
        try
        {
            fos = new FileOutputStream(f);
            for (Integer offset: mrffiles.keySet())
            {
                MsfEntry me = mrffiles.get(offset);
                me.offset = (int) fos.getChannel().position();
                fos.write(me.filedata);
                me.filedata = null;
            }
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
    }
    
    public int getSize()
    {
        return size;
    }
    
    public int getCount()
    {
        return files.size();
    }
    
    public TreeMap<Integer, MsfEntry> getFiles()
    {
        return files;
    }
}
