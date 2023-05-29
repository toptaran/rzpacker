import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author TARAN a.k.a toptaran
 */
public class Z3Rle
{
    public static int[] decodeSize(byte data[])
    {
        int value = 0;     // Reset returned size value
        int length = 0;    // Length (in bytes) of size data

        int bitSHL = 0;    // Reset number of bits to shift left
        
        boolean cont = true;
        int curpoint = 0;
        while(cont)
        {
            // Check shift value (indicates invalid starting position)
            if( bitSHL > 32 )
                return new int[]{0};
            cont = ((data[curpoint] >> 7) & 1) == 1 ? true : false;   // 8th bit check for end
            value |= ((data[curpoint] & ~(1 << 7)) & 0xFF) << bitSHL; // for size use only 7 bit
            
            bitSHL += 7;    // Bits of data per 'codedSizeByte'
            ++curpoint;            // Move along buffer
        }
        // Calculate the actual length of data
        length = curpoint;
        
        return new int[]{value, length};
    }
    
    public static boolean decodeInstruction(
        byte[] data,              // Source buffer
        AtomicInteger dataOffset, // Source buffer offset
        byte[] buff,              // Destination buffer
        AtomicInteger bufOffset   // Destination buffer offset
    )
    {
        int cmdMarker;
        int instruction, instructionExSize, buf32;

        // Read the command marker from the buffer
        cmdMarker = data[dataOffset.getAndIncrement()] & 0xFF;
    
        // Lookup the instruction
        instruction = z3RleInstructions[cmdMarker];

        // Length of additional bytes (5-bits of instruction)
        instructionExSize = instruction >> 11;

        // 5 sizes are supported in the client (0,1,2,3,4)
        if(instructionExSize > 4)
            return false;

        buf32 = 0;

        // Read these additional bytes
        for(int i = 0; i < instructionExSize; i++, dataOffset.getAndIncrement())
        {
            // Read another byte into buf32
            int tmp = data[dataOffset.get()] & 0xFF;
            tmp <<= (i * 8);
            buf32 |= tmp;
        }

        // Check command marker for method
        if((cmdMarker & 3) > 0)
        {
            // Using the destination buffer as the source (copying existing data)
            int srcOffset, msgLength;

            // Data can be up to 2,047 bytes from current position
            srcOffset = (instruction & 0x700) + buf32;
            msgLength = (instruction & 0xFF);

            // Check for invalid modulus and invalid source offset (corrupted data)
            if((srcOffset == 0) || (srcOffset > bufOffset.get()))
            {
                return false;
            }

            // Copy data from existing buffer
            for(int i = 0; i < msgLength; ++i )
                buff[bufOffset.get() + i] = buff[bufOffset.get() - (srcOffset - (i % srcOffset))];

            bufOffset.addAndGet(msgLength);
        }
        else
        {
            // Using the source buffer as the source (inserting new data)
            int msgLength;

            msgLength = ( instruction & 0xFF ) + buf32;

            // Check data source (source buffer) has enough data
            if( dataOffset.get() + msgLength > data.length )
            {
                return false;
            }

            // Copy data from buffer
            System.arraycopy(data, dataOffset.get(), buff, bufOffset.get(), msgLength);
            dataOffset.addAndGet(msgLength);
        
            bufOffset.addAndGet(msgLength);
        }
        return true;
    }
    
    public static final short[] z3RleInstructions =
    {
        0x0001, 0x0804, 0x1001, 0x2001, 0x0002, 0x0805, 0x1002, 0x2002,
        0x0003, 0x0806, 0x1003, 0x2003, 0x0004, 0x0807, 0x1004, 0x2004,
        0x0005, 0x0808, 0x1005, 0x2005, 0x0006, 0x0809, 0x1006, 0x2006,
        0x0007, 0x080A, 0x1007, 0x2007, 0x0008, 0x080B, 0x1008, 0x2008,
        0x0009, 0x0904, 0x1009, 0x2009, 0x000A, 0x0905, 0x100A, 0x200A,
        0x000B, 0x0906, 0x100B, 0x200B, 0x000C, 0x0907, 0x100C, 0x200C,
        0x000D, 0x0908, 0x100D, 0x200D, 0x000E, 0x0909, 0x100E, 0x200E,
        0x000F, 0x090A, 0x100F, 0x200F, 0x0010, 0x090B, 0x1010, 0x2010,
        0x0011, 0x0A04, 0x1011, 0x2011, 0x0012, 0x0A05, 0x1012, 0x2012,
        0x0013, 0x0A06, 0x1013, 0x2013, 0x0014, 0x0A07, 0x1014, 0x2014,
        0x0015, 0x0A08, 0x1015, 0x2015, 0x0016, 0x0A09, 0x1016, 0x2016,
        0x0017, 0x0A0A, 0x1017, 0x2017, 0x0018, 0x0A0B, 0x1018, 0x2018,
        0x0019, 0x0B04, 0x1019, 0x2019, 0x001A, 0x0B05, 0x101A, 0x201A,
        0x001B, 0x0B06, 0x101B, 0x201B, 0x001C, 0x0B07, 0x101C, 0x201C,
        0x001D, 0x0B08, 0x101D, 0x201D, 0x001E, 0x0B09, 0x101E, 0x201E,
        0x001F, 0x0B0A, 0x101F, 0x201F, 0x0020, 0x0B0B, 0x1020, 0x2020,
        0x0021, 0x0C04, 0x1021, 0x2021, 0x0022, 0x0C05, 0x1022, 0x2022,
        0x0023, 0x0C06, 0x1023, 0x2023, 0x0024, 0x0C07, 0x1024, 0x2024,
        0x0025, 0x0C08, 0x1025, 0x2025, 0x0026, 0x0C09, 0x1026, 0x2026,
        0x0027, 0x0C0A, 0x1027, 0x2027, 0x0028, 0x0C0B, 0x1028, 0x2028,
        0x0029, 0x0D04, 0x1029, 0x2029, 0x002A, 0x0D05, 0x102A, 0x202A,
        0x002B, 0x0D06, 0x102B, 0x202B, 0x002C, 0x0D07, 0x102C, 0x202C,
        0x002D, 0x0D08, 0x102D, 0x202D, 0x002E, 0x0D09, 0x102E, 0x202E,
        0x002F, 0x0D0A, 0x102F, 0x202F, 0x0030, 0x0D0B, 0x1030, 0x2030,
        0x0031, 0x0E04, 0x1031, 0x2031, 0x0032, 0x0E05, 0x1032, 0x2032,
        0x0033, 0x0E06, 0x1033, 0x2033, 0x0034, 0x0E07, 0x1034, 0x2034,
        0x0035, 0x0E08, 0x1035, 0x2035, 0x0036, 0x0E09, 0x1036, 0x2036,
        0x0037, 0x0E0A, 0x1037, 0x2037, 0x0038, 0x0E0B, 0x1038, 0x2038,
        0x0039, 0x0F04, 0x1039, 0x2039, 0x003A, 0x0F05, 0x103A, 0x203A,
        0x003B, 0x0F06, 0x103B, 0x203B, 0x003C, 0x0F07, 0x103C, 0x203C,
        0x0801, 0x0F08, 0x103D, 0x203D, 0x1001, 0x0F09, 0x103E, 0x203E,
        0x1801, 0x0F0A, 0x103F, 0x203F, 0x2001, 0x0F0B, 0x1040, 0x2040
    };
}
