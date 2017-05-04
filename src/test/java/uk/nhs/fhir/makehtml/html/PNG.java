package uk.nhs.fhir.makehtml.html;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PNG {
	private static final char[] PNG_HEADER = new char[]{0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};
	private static final int HEADER_CHUNK_SIZE = 13;
	
	private List<PNGChunk> chunks = Lists.newArrayList();

	private final Dimension size;
	private final byte bitDepth;
	private final ColourType colourType;
	private final CompressionType compressionType;
	private final FilterType filterType;
	private final InterlaceType interlaceType;
	
	public PNG(Dimension size, byte bitDepth, ColourType colourType, CompressionType compressionType, FilterType filterType, InterlaceType interlaceType) {
		if (!colourType.isBitDepthAllowed(bitDepth)) {
			throw new IllegalArgumentException("Incompatible colour type/bit depth");
		}
		
		this.size = size;
		this.bitDepth = bitDepth;
		this.colourType = colourType;
		this.compressionType = compressionType;
		this.filterType = filterType;
		this.interlaceType = interlaceType;
	}

	public static PNG parsePNGBase64(String base64Input) {
		byte[] decodedBytes = Base64.decodeBase64(base64Input);
		return parsePNG(new ByteArrayInputStream(decodedBytes));
	}
	
	public static PNG parsePNG(InputStream input) {
		try {
			for (int i=0; i<PNG_HEADER.length; i++) {
				if (input.read() != (int)PNG_HEADER[i]) {
					throw new IllegalArgumentException("incorrect header bytes");
				}
			}
			
			PNG png = readPNGHeader(input);
			
			while (input.available() > 0) {
				int dataLength = readInt(input);
				Chunk type = readChunkType(input);
				System.out.println("Found a " + type.name() + " chunk with " + dataLength + " bytes of data");
				byte[] data = readNBytes(input, dataLength);
				readInt(input); //crc
				
				if (type.equals(Chunk.IDAT)) {
					Inflater decompresser = new Inflater();
				    decompresser.setInput(data, 0, dataLength);
				    int expectedDecompressedLength = png.expectedDecompressedLength();
					byte[] result = new byte[expectedDecompressedLength];
				    decompresser.inflate(result);
				    decompresser.end();
				    
				    List<FilteredScanLine> filteredScanlines = Lists.newArrayList();
				    ByteArrayInputStream bais = new ByteArrayInputStream(result);
				    for (int i=0; i<png.size.getHeight(); i++) {
				    	int filter = bais.read();
				    	
				    	List<PixelRGBA> scanLinePixels = Lists.newArrayList();
				    	for (int j=0; j<png.size.getWidth(); j++) {
				    		int red = bais.read();
				    		int green = bais.read();
				    		int blue = bais.read();
				    		int alpha = bais.read();
				    		scanLinePixels.add(new PixelRGBA(red, green, blue, alpha));
				    	}
				    	
				    	filteredScanlines.add(new FilteredScanLine(filter, scanLinePixels));
				    }
				    
				    /*
				    for (int i=0; i<filteredScanlines.size(); i++) {
				    	FilteredScanLine line = filteredScanlines.get(i);
				    	for (int j=0; j<line.getPixels().size(); j++) {
				    		PixelRGBA pixel = line.get(j);
				    		if (!pixel.isWhite()) {
				    			System.out.println("hit non white at " + i + ":" + j);
				    		}
				    	}
				    	
				    	System.out.println("found a line using filter " + line.getFilter());
				    }
				    */
				}
				
				
				
				png.chunks.add(new PNGChunk(type, data));
			}
			
			System.out.println("finished parsing successfully");
			
			return png;
			
		} catch (IOException | DataFormatException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static PNG readPNGHeader(InputStream input) throws IOException {
		//IHDR
		int hdrDataLength = readInt(input);
		if (hdrDataLength != HEADER_CHUNK_SIZE) {
			throw new IllegalArgumentException("header data reported as " + hdrDataLength);
		}
		
		Chunk hdrType = readChunkType(input);
		if (!hdrType.equals(Chunk.IHDR)) {
			throw new IllegalArgumentException("Expected first chunk IHDR (was " + hdrType.name() + ")");
		}
		
		int width = readInt(input);
		int height = readInt(input);
		Dimension size = new Dimension(width, height);
		byte bitDepth = (byte)input.read();
		byte colourType = (byte)input.read();
		byte compressionType = (byte)input.read();
		byte filterType = (byte)input.read();
		byte interlaceType = (byte)input.read();
		readInt(input); //crc

		return new PNG(size, bitDepth, 
			ColourType.forValue(colourType), 
			CompressionType.forValue(compressionType), 
			FilterType.forValue(filterType), 
			InterlaceType.forValue(interlaceType));
	}

	private static byte[] readNBytes(InputStream input, int dataLength) throws IOException {
		byte[] bytes = new byte[dataLength];
		
		for (int i=0; i<dataLength; i++) {
			bytes[i] = (byte)input.read();
		}
		
		return bytes;
	}

	private static Chunk readChunkType(InputStream input) throws IOException {
		byte[] nameArray = new byte[4];
		for (int i=0; i<4; i++) {
			nameArray[i] = (byte) input.read();
		}
		return Chunk.valueOf(new String(nameArray));
	}

	private static int readInt(InputStream input) throws IOException {
		int length = 0;
		for (int i=0; i<4; i++) {
			length *= 0x100;
			length += input.read();
		}
		
		return length;
	}
	
	public int expectedDecompressedLength() {
		int filterBytes = size.height;
		int dataBytes = size.height * size.width * bitDepth/8 * colourType.getChannels();
		return filterBytes + dataBytes;
	}
	
	public List<PNGChunk> getChunks() {
		return chunks;
	}
	
	public CompressionType getCompressionType() {
		return compressionType;
	}

	public FilterType getFilterType() {
		return filterType;
	}
	
	public InterlaceType getInterlaceType() {
		return interlaceType;
	}
}

enum Chunk {
	IHDR,
	PLTE,
	IDAT,
	IEND,
	bKGD,
	cHRM,
	gAMA,
	hIST,
	iCCP,
	iTXt,
	pHYs,
	sBIT,
	sPLT,
	sRGB,
	sTER,
	tEXt,
	tIME,
	tRNS,
	zTXt;
}

enum ColourType {
	GREYSCALE(0, Sets.newHashSet(1, 2, 4, 8, 16), 1), //G
	TRUECOLOUR(2, Sets.newHashSet(8, 16), 3), //RGB
	INDEXEDCOLOUR(3, Sets.newHashSet(1, 2, 4, 8), 1), //I
	GREYSCALE_ALPHA(4, Sets.newHashSet(8, 16), 2), //GA
	TRUECOLOUR_ALPHA(6, Sets.newHashSet(8, 16), 4); //RGBA
	
	private final int value;
	private final Set<Integer> allowedBitDepths;
	private final int channels;

	ColourType(int value, Set<Integer> allowedBitDepths, int channels) {
		this.value = value;
		this.allowedBitDepths = allowedBitDepths;
		this.channels = channels;
	}
	
	public static ColourType forValue(int value) {
		for (ColourType type : ColourType.values()) {
			if (type.value == value) {
				return type;
			}
		}
		
		throw new IllegalArgumentException("Not a valid colour type [" + value + "]");
	}
	
	public boolean isBitDepthAllowed(int bitDepth) {
		return allowedBitDepths.contains(bitDepth);
	}
	
	public int getChannels() {
		return channels;
	}
}

enum CompressionType {
	STANDARD; //deflate/inflate compression with a sliding window of at most 32768 bytes

	public static CompressionType forValue(byte value) {
		if (value == 0) {
			return STANDARD;
		}
		
		throw new IllegalArgumentException("Not a valid compression type [" + value + "]");	
	} 
}

enum FilterType {
	STANDARD; //adaptive filtering with 5 basic filter types

	public static FilterType forValue(byte value) {
		if (value == 0) {
			return STANDARD;
		}
		
		throw new IllegalArgumentException("Not a valid filter type [" + value + "]");	
	} 
}

enum InterlaceType {
	NONE(0),
	ADAM7(1);
	
	private int value;

	InterlaceType(int value) {
		this.value = value;
	}
	
	public static InterlaceType forValue(int value) {
		for (InterlaceType type : InterlaceType.values()) {
			if (type.value == value) {
				return type;
			}
		}
		
		throw new IllegalArgumentException("Not a valid interlace type [" + value + "]");
	}
}

class FilteredScanLine {
	
	private int filter;
	private List<PixelRGBA> filteredPixels;
	
	FilteredScanLine(int filter, List<PixelRGBA> filteredPixels) {
		this.filter = filter;
		this.filteredPixels = filteredPixels;
	}
	
	public int getFilter() {
		return filter;
	}
	
	public PixelRGBA get(int index) {
		if (index >= filteredPixels.size()) {
			throw new IndexOutOfBoundsException("[" + index + " - " + filteredPixels.size() + "]");
		}
		return filteredPixels.get(index);
	}
	
	public List<PixelRGBA> getPixels() {
		return filteredPixels;
	}
}

class PixelRGBA {
	private final int red;
	private final int green;
	private final int blue;
	private final int alpha;
	
	public PixelRGBA(int red, int green, int blue, int alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	
	public boolean isWhite() {
		return red == 99 && green == 99 && blue == 99 && alpha == 0;
	}

	public String toString() {
		return "RGBA[" + red + " | " + green + " | " + blue + " | " + alpha + "]";
	}
}