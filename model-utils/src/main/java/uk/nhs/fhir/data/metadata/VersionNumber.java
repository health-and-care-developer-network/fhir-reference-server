package uk.nhs.fhir.data.metadata;

import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionNumber implements Comparable<VersionNumber> {
	
	private static final Pattern REGEX = Pattern.compile("^([0-9]{1,3})(?:\\.([0-9]{1,3})(?:\\.([0-9]{1,3}))?)?$");
	
	private final int major;
	private final int minor;
	private final int patch;
	private final boolean valid;
	
	public VersionNumber(int major, int minor) {
		this(major, minor, 0);
	}
	
	public VersionNumber(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		
		this.valid = true;
	}
	
	public VersionNumber(String versionStr) {
		
		Matcher matcher = REGEX.matcher(versionStr);
		if (matcher.find()) {
			
			String majorGroup = matcher.group(1);
			Optional<String> minorGroup = Optional.ofNullable(matcher.group(2));
			Optional<String> patchGroup = Optional.ofNullable(matcher.group(3));
			
			this.major = Integer.parseInt(majorGroup);
			
			this.minor = 
				minorGroup
					.map(Integer::parseInt)
					.orElse(0);
			this.patch = 
				patchGroup
					.map(Integer::parseInt)
					.orElse(0);
			
			this.valid = true;
		} else {
			throw new IllegalStateException("Unable to parse version number: " + versionStr);
		}
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}
	
	public boolean isValid() {
		return valid;
	}

	public static final Comparator<VersionNumber> BY_MAJOR_MINOR = 
		Comparator.comparing((VersionNumber version) -> version.major)
			.thenComparing((VersionNumber version) -> version.minor);
	
	@Override
	public int compareTo(VersionNumber other) {
		return BY_MAJOR_MINOR.compare(this, other);
	}

	@Override
	public String toString() {
		String versionString = major + "." + minor;
		if (patch > 0)
			versionString += "." + patch;
		return versionString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + patch;
		result = prime * result + (valid ? 1231 : 1237);
		return result;
	}

	// Used by velocity templates to work out which version history link to bolden
	public boolean equals(String s) {
		if (s == null) {
			return false;
		}
		
		return equals(new VersionNumber(s));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof VersionNumber)) {
			return false;
		}

		VersionNumber other = (VersionNumber)obj;
		
		return (
			major == other.major
		  && minor == other.minor
		  && patch == other.patch
		  && valid == other.valid);
	}
}
