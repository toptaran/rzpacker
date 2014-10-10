import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import lzma.sdk.lzma.Encoder;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class FileHashBuilder
{
	public static void makeFileHash(String clientdir)
	{
		try
		{
			File f = new File(clientdir);
			ArrayList<FileHashItem> list = new ArrayList<FileHashItem>();
			for (File t: f.listFiles())
			{
				if (!t.isDirectory() && !t.getName().equalsIgnoreCase("filehash.msf"))
				{
					System.out.println("Process " + t.getName() + "...");
					FileHashItem fhi = new FileHashItem();
					fhi.name = t.getName().getBytes(Charset.forName("UTF-8"));
					fhi.namelen = (short) fhi.name.length;
					fhi.size = (int) t.length();
					fhi.checksumm = FileUtils.calcMD5File(t);
					list.add(fhi);
					System.out.println("Done");
				}
			}

			f = new File(clientdir + "/Data");
			for (File t: f.listFiles())
			{
				if (!t.isDirectory())
				{
					System.out.println("Process " + t.getName() + "...");
					FileHashItem fhi = new FileHashItem();
					fhi.name = ("Data/" + t.getName()).getBytes(Charset.forName("UTF-8"));
					fhi.namelen = (short) fhi.name.length;
					fhi.size = (int) t.length();
					fhi.checksumm = FileUtils.calcMD5File(t);
					list.add(fhi);
					System.out.println("Done");
				}
			}

			int calcsize = 0;
			for (FileHashItem fhi: list)
				calcsize += 2 + fhi.namelen + 4 + 16;

			ByteBuffer bb = ByteBuffer.wrap(new byte[calcsize]).order(ByteOrder.LITTLE_ENDIAN);
			for (FileHashItem fhi: list)
			{
				bb.putShort(fhi.namelen);
				bb.put(fhi.name);
				bb.putInt(fhi.size);
				bb.put(fhi.checksumm);
			}

			bb.flip();

			byte[] data = bb.array();

			data = EciesCryptoPP.encrypt(data);
			int size = data.length;
			ByteArrayInputStream bai = new ByteArrayInputStream(data);

			ByteArrayOutputStream bas = new ByteArrayOutputStream(data.length * 2);

			Encoder encoder = new Encoder();

			encoder.setLcLpPb(3, 0, 2);
			encoder.setDictionarySize(0x10000);
			encoder.code(bai, bas, -1, -1, null);

			FileOutputStream fos = new FileOutputStream(new File(clientdir + "/filehash.msf"));
			fos.write(FileUtils.getIntToByte(size));
			fos.write(bas.toByteArray());
			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static class FileHashItem
	{
		short namelen;
		byte[] name;
		int size;
		byte[] checksumm;
	}
}
