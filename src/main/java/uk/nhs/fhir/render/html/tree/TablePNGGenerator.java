package uk.nhs.fhir.render.html.tree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Maps;

/**
 * Generates background pngs for table rows as base64.
 * @author jon
 */
public class TablePNGGenerator {
	private static final int width = 800;
	private static final byte bitDepth = 8; // 8 bits per channel
	private static final byte colourType = 6; // RGB with Alpha
	private static final byte filterType = 0; // standard
	private static final byte interlacingType = 0; // standard
	private static final byte compression = 0; // standard
	
	private static final int firstLinePixelIndex = 12;
	private static final int pixelsBetweenLines = 16;
	
	private static final Map<String, String> cache = Maps.newHashMap();
	
	/**
	 * Unique css class for any given line style. Can be used as a key for caching.
	 */
	public static String getCSSClass(Style style, boolean[] vlinesRequired) {
		StringBuilder cssClass = new StringBuilder("fhirtreebg-");
		cssClass.append(style.name().toLowerCase(Locale.UK));
		cssClass.append("-");
		for (boolean b : vlinesRequired) {
			cssClass.append(b ? "1" : "0");
		}
		
		return cssClass.toString();
	}

	public String getBase64(String key) {
		String[] tokens = key.split("\\-");
		if (!tokens[0].equals("fhirtreebg")) {
			throw new IllegalArgumentException(key);
		}
		
		Style style = Style.valueOf(tokens[1].toUpperCase(Locale.UK));
		
		char[] chars = tokens[2].toCharArray();
		boolean[] vlinesRequired = new boolean[chars.length];
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			if (c == '1') {
				vlinesRequired[i] = true;
			} else if (c == '0') {
				vlinesRequired[i] = false;
			} else {
				throw new IllegalArgumentException("Should only be 1s and 0s [" + tokens[2] + "]");
			}
		}
		
		return getBase64(style, vlinesRequired);
	}
	
	public String getBase64(Style style, boolean[] vlinesRequired) {
		String key = getCSSClass(style, vlinesRequired);
		if (!cache.containsKey(key)) {
			cache(key, style, vlinesRequired);
		}
		return cache.get(key);
	}

	private void cache(String key, Style style, boolean[] vlinesRequired) {
		int maxVlines = (width - firstLinePixelIndex) / pixelsBetweenLines;
		if (vlinesRequired.length > maxVlines) {
			throw new IllegalArgumentException("Too many vlines required: [" + Arrays.toString(vlinesRequired) + " : " + maxVlines + "]");
		}

		try {
			byte[] pngBytes = getPngBytes(style, vlinesRequired);
			String base64Png = new String(Base64.encodeBase64(pngBytes), "UTF-8");
			cache.put(key, base64Png);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	byte[] getPngBytes(Style style, boolean[] vlinesRequired) throws IOException {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		
		int height = style.getHeight();
		
		bytesOut.write(getPNGHeaderBytes());
		bytesOut.write(getIHDRBytes(height));
		bytesOut.write(getDataChunk(style, vlinesRequired));
		bytesOut.write(getEndChunk());
		
		return bytesOut.toByteArray();
	}

	private byte[] getPNGHeaderBytes() throws IOException {
		return new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};
	}
	
	private byte[] getIHDRBytes(int height) throws IOException {
		ByteArrayOutputStream ihdrBytes = new ByteArrayOutputStream();
		
		ihdrBytes.write(new byte[]{0, 0, 0, 13}); //Chunk data length
		ihdrBytes.write(new byte[]{'I', 'H', 'D', 'R'}); // IHDR chunk name
		writeInt(ihdrBytes, width);
		writeInt(ihdrBytes, height);
		ihdrBytes.write(bitDepth);
		ihdrBytes.write(colourType);
		ihdrBytes.write(compression); // standard compression
		ihdrBytes.write(filterType); // standard filter
		ihdrBytes.write(interlacingType); // no interlacing
		
		ihdrBytes.write(getCrcBytes(ihdrBytes.toByteArray()));
		return ihdrBytes.toByteArray();
	}

	byte[] getDataChunk(Style style, boolean[] vlinesRequired) throws IOException {
		byte[] pixelArray = getDataBytes(style, vlinesRequired);
		
		byte[] deflatedDataBytes = deflate(pixelArray);

		ByteArrayOutputStream dataChunk = new ByteArrayOutputStream();
		writeInt(dataChunk, deflatedDataBytes.length);
		dataChunk.write(new byte[]{'I', 'D', 'A', 'T'});
		dataChunk.write(deflatedDataBytes);
		dataChunk.write(getCrcBytes(dataChunk.toByteArray()));
		
		return dataChunk.toByteArray();
	}
	
	byte[] getDataBytes(Style style, boolean[] vlinesRequired) throws IOException {
		ByteArrayOutputStream dataChunkData = new ByteArrayOutputStream();
		
		//start with all pixels transparent
		for (int rowIndex=0; rowIndex<style.getHeight(); rowIndex++) {
			dataChunkData.write(0); //filter type = no filtering
			for (int widthIndex=0; widthIndex<width; widthIndex++) {
				dataChunkData.write(new byte[]{99,99,99,0}); //RGBA
			}
		}

		byte[] pixelArray = dataChunkData.toByteArray();
		
		//write in black pixels where required
		boolean[] config = style.getConfig();
		for (int rowIndex=0; rowIndex<config.length; rowIndex++) {
			if (config[rowIndex]) {
				int pixelIndex = firstLinePixelIndex;
				
				for (int vlineIndex=0; vlineIndex<vlinesRequired.length; vlineIndex++) {
					if (vlinesRequired[vlineIndex]) {
						// Calculate offset into chunk byte array
						int filterBytesToSkip = (rowIndex+1);
						int pixelBytesBeforeOnPreviousLines = 4 * width * rowIndex;
						int pixelBytesBeforeOnLine = 4 * (pixelIndex - 1);
						int byteIndex = filterBytesToSkip + pixelBytesBeforeOnPreviousLines + pixelBytesBeforeOnLine;
					
						// Update pixel to be black
						pixelArray[byteIndex] = 0;
						pixelArray[byteIndex + 1] = 0;
						pixelArray[byteIndex + 2] = 0;
						pixelArray[byteIndex + 3] = (byte) 0xff;
					}
					
					pixelIndex += pixelsBetweenLines;
				}
			}
		}
		
		return pixelArray;
	}

	private byte[] deflate(byte[] chunkDataBytes) throws IOException {
		ByteArrayOutputStream deflatedBytes = new ByteArrayOutputStream();

	    byte[] outputBuffer = new byte[1024];
	    
	    Deflater compresser = new Deflater();
	    compresser.setInput(chunkDataBytes);
	    compresser.finish();
	    
	    while (!compresser.finished()) {
	    	int bytesDeflated = compresser.deflate(outputBuffer);
	    	deflatedBytes.write(outputBuffer, 0, bytesDeflated);
	    }
	    compresser.end();
	    
	    return deflatedBytes.toByteArray();
	}

	private byte[] getCrcBytes(byte[] chunk) throws IOException {
		// trim data length bytes
		byte[] chunkNameAndDataBytes = Arrays.copyOfRange(chunk, 4, chunk.length);
		
		CRC32 crc32 = new CRC32();
		crc32.update(chunkNameAndDataBytes);
		return intBytes((int)crc32.getValue());
	}

	private byte[] getEndChunk() {
		return new byte[]{0,0,0,0,'I','E','N','D',(byte)0xAE,0x42,0x60,(byte)0x82};
	}

	private void writeInt(ByteArrayOutputStream bytesOut, int value) throws IOException {
		byte[] intBytes = intBytes(value);
		
		bytesOut.write(intBytes);
	}
	
	private byte[] intBytes(int value) {
		byte[] intBytes = new byte[4];
		for (int index=3; index>=0; index--) {
			byte r = (byte)(value & 0xff);
			intBytes[index] = r;
			value >>= 8;
		}
		return intBytes;
	}
}

enum Style {
	SOLID(new boolean[]{true}),
	DOTTED(new boolean[]{true, false}),
	DASHED(new boolean[]{true, true, false});
	
	private final boolean[] config;
	
	Style(boolean[] config) {
		this.config = config;
	}
	
	public boolean[] getConfig() {
		return config;
	}
	
	public int getHeight() {
		return config.length;
	}
}
