package uk.nhs.fhir.makehtml.html;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PNGChunk {
	private final Chunk type;
	protected final byte[] data;
	
	PNGChunk(Chunk type, byte[] data) {
		this.type = type;
		this.data = data;
	}
	
	public boolean isCritical() {
		return Character.isUpperCase(type.name().charAt(0));
	}
	
	public boolean isPublic() {
		return Character.isUpperCase(type.name().charAt(1));
	}
	
	public boolean isSafeToCopy() {
		return Character.isUpperCase(type.name().charAt(3));
	}
	
	public byte[] getData() {
		return data;
	}
	
	public byte[] inflatedData() throws DataFormatException {
		Inflater decompresser = new Inflater();
	    decompresser.setInput(data, 0, data.length);

	    byte[] buf = new byte[512];
	    ByteArrayOutputStream inflated = new ByteArrayOutputStream();
	    while (!decompresser.finished()) {
	    	int resultLength = decompresser.inflate(buf);
		    inflated.write(buf, 0, resultLength);
	    }
		
	    byte[] inflatedBytes = inflated.toByteArray();
	    
	    decompresser.end();
	    
	    return inflatedBytes;
	}
}

