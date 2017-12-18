package uk.nhs.fhir.data.metadata;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionNumber implements Comparable<VersionNumber> {
	
	private static final Logger LOG = LoggerFactory.getLogger(VersionNumber.class.getName());
	
	private static final Pattern REGEX = Pattern.compile("^([0-9]{1,3})(?:\\.([0-9]{1,3})(?:\\.([0-9]{1,3}))?)?$");
	
	private final int major;
	private final int minor;
	private final int patch;
	private final boolean valid;
	
	public VersionNumber(String versionStr) {
		
		Matcher matcher = REGEX.matcher(versionStr);
		if (matcher.find()) {
			this.major = Integer.parseInt(matcher.group(1));
			
			this.minor = 
				Optional.ofNullable(matcher.group(2))
					.map(Integer::parseInt)
					.orElse(0);
			
			this.patch = 
				Optional.ofNullable(matcher.group(3))
					.map(Integer::parseInt)
					.orElse(0);
			
			this.valid = true;
		} else {
			LOG.warn("Unable to parse version number: " + versionStr);
			
			this.major = 0;
			this.minor = 0;
			this.patch = 0;
			
			this.valid = false;
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

	@Override
	public int compareTo(VersionNumber other) {
		if (this.major < other.getMajor()) {
			return -1;
		} else if (this.major > other.getMajor()) {
			return 1;
		} else {
			if (this.minor < other.getMinor()) {
				return -1;
			} else if (this.minor > other.getMinor()) {
				return 1;
			} else {
				// Ignore patch versions and assume the are the same as any other resource with the same major.minor version
				return 0;
			}
		}
	}

	@Override
	public String toString() {
		String versionString = major + "." + minor;
		if (patch > 0)
			versionString += '.' + patch;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionNumber other = (VersionNumber) obj;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (patch != other.patch)
			return false;
		if (valid != other.valid)
			return false;
		return true;
	}
}
