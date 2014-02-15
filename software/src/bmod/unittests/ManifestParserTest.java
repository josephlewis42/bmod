package bmod.unittests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bmod.ManifestParser;

public class ManifestParserTest
{

	@Test
	public void testNormalizeKey()
	{
		assertTrue(ManifestParser.normalizeKey("TheLazyOSMac").equals("The Lazy OS Mac"));
		assertTrue(ManifestParser.normalizeKey("hELLOWorld").equals("h ELLO World"));
		assertTrue(ManifestParser.normalizeKey("ALLCAPS").equals("ALLCAPS"));
	}

}
