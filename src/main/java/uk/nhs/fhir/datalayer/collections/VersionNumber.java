package uk.nhs.fhir.datalayer.collections;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionNumber implements Comparable<VersionNumber> {
	
	private static final Logger LOG = Logger.getLogger(VersionNumber.class.getName());
	private static final Pattern versionMask1 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$");
	private static final Pattern versionMask2 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})$");
	private static final Pattern versionMask3 = Pattern.compile("^([0-9]{1,3})$");
	
	private int major = 0;
	private int minor = 0;
	private int patch = 0;
	private boolean valid = false;
	private boolean versionless = false;
	
	public VersionNumber() {
		this.versionless = true;
		this.valid = true;
	}
	
	public VersionNumber(String versionStr) {
		if (versionStr != null) {
			// First, check this is a valid version number, then extract the components
			Matcher matcher1 = versionMask1.matcher(versionStr);
			if (matcher1.find()) {
				// We have major.minor.patch
				this.major = Integer.parseInt(matcher1.group(1));
				this.minor = Integer.parseInt(matcher1.group(2));
				this.patch = Integer.parseInt(matcher1.group(3));
				this.valid = true;
			} else {
				Matcher matcher2 = versionMask2.matcher(versionStr);
				if (matcher2.find()) {
					// We have major.minor
					this.major = Integer.parseInt(matcher2.group(1));
					this.minor = Integer.parseInt(matcher2.group(2));
					this.patch = 0;
					this.valid = true;
				} else {
					Matcher matcher3 = versionMask3.matcher(versionStr);
					if (matcher3.find()) {
						// We have major only
						this.major = Integer.parseInt(matcher3.group(1));
						this.minor = 0;
						this.patch = 0;
						this.valid = true;
					} else {
						LOG.warning("Unable to parse version number: " + versionStr);
					}
				}
			}
		}
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public int getPatch() {
		return patch;
	}

	public void setPatch(int patch) {
		this.patch = patch;
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
			versionString = versionString + '.' + patch;
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

	public boolean isVersionless() {
		return versionless;
	}
}
