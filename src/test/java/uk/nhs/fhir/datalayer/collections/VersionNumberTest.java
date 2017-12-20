package uk.nhs.fhir.datalayer.collections;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.nhs.fhir.data.metadata.VersionNumber;

public class VersionNumberTest {

	@Test
	public void testGoodVersionNumberMajorMinorPatch() {
		VersionNumber vn;
		vn = new VersionNumber("1.2.3");
		assertEquals(1, vn.getMajor());
		assertEquals(2, vn.getMinor());
		assertEquals(3, vn.getPatch());
	}
	
	@Test
	public void testGoodVersionNumberMajorMinorPatchBiggerNumbers() {
		VersionNumber vn;
		vn = new VersionNumber("123.234.345");
		assertEquals(123, vn.getMajor());
		assertEquals(234, vn.getMinor());
		assertEquals(345, vn.getPatch());
	}

	@Test
	public void testGoodVersionNumberMajorMinor() {
		VersionNumber vn;
		vn = new VersionNumber("1.2");
		assertEquals(1, vn.getMajor());
		assertEquals(2, vn.getMinor());
		assertEquals(0, vn.getPatch());
	}

	@Test
	public void testGoodVersionNumberMajor() {
		VersionNumber vn;
		vn = new VersionNumber("1");
		assertEquals(1, vn.getMajor());
		assertEquals(0, vn.getMinor());
		assertEquals(0, vn.getPatch());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testBadVersionNumberAlpha() {
		new VersionNumber("1.2.3a");
	}

	@Test(expected=IllegalStateException.class)
	public void testBadVersionNumberAlphaOnly() {
		new VersionNumber("draft");
	}

	@Test(expected=IllegalStateException.class)
	public void testBadVersionNumberTooManyDots() {
		new VersionNumber("1.2.3.4");
	}

	@Test(expected=IllegalStateException.class)
	public void testBadVersionNumberTooManyDigits() {
		new VersionNumber("1.2.3012");
	}
	
	@Test
	public void testComparingVersions() {
		VersionNumber vn1, vn2;
		vn1 = new VersionNumber("1");
		vn2 = new VersionNumber("2");
		assertTrue(vn2.compareTo(vn1) > 0);
	}
	
	@Test
	public void testComparingMajorToMinorVersion() {
		VersionNumber vn1, vn2;
		vn1 = new VersionNumber("1");
		vn2 = new VersionNumber("1.1");
		assertTrue(vn2.compareTo(vn1) > 0);
	}
	
	@Test
	public void testComparingIdenticalVersions() {
		VersionNumber vn1, vn2;
		vn1 = new VersionNumber("1.1");
		vn2 = new VersionNumber("1.1");
		assertTrue(vn2.compareTo(vn1) == 0);
	}
	
	@Test
	public void testComparingIgnoringPatchVersions() {
		VersionNumber vn1, vn2;
		vn1 = new VersionNumber("1.1.2");
		vn2 = new VersionNumber("1.1.3");
		assertTrue(vn2.compareTo(vn1) == 0);
	}
	
	@Test
	public void testMajorVersionToString() {
		VersionNumber vn = new VersionNumber("6");
		assertEquals("6.0", vn.toString());
	}
	
	@Test
	public void testMajorMinorVersionToString() {
		VersionNumber vn = new VersionNumber("6.1");
		assertEquals("6.1", vn.toString());
	}
	
	@Test
	public void testMajorMinorPatchVersionToString() {
		VersionNumber vn = new VersionNumber("6.1.2");
		assertEquals("6.1.2", vn.toString());
	}
}
