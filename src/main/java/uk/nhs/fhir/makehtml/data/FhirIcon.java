package uk.nhs.fhir.makehtml.data;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirDataTypes;
import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.api.ICompositeDatatype;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt.Type;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.parser.IParser;
import com.google.common.base.Strings;
import uk.nhs.fhir.makehtml.HTMLConstants;
import uk.nhs.fhir.makehtml.NewMain;
import uk.nhs.fhir.util.SharedFhirContext;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

public enum FhirIcon {
	CHOICE("icon_choice", "gif", FhirIcon.choiceBase64),
	DATATYPE("icon_datatype", "gif", FhirIcon.datatypeBase64),
	ELEMENT("icon_element", "gif", FhirIcon.elementBase64),
	EXTENSION("icon_extension", "png", FhirIcon.extensionBase64),
	EXTENSION_COMPLEX("icon_extension_complex", "png", FhirIcon.extensionComplexBase64),
	EXTENSION_SIMPLE("icon_extension_simple", "png", FhirIcon.extensionSimpleBase64),
	EXTERNAL("icon_external", "png", FhirIcon.externalBase64),
	PRIMITIVE("icon_primitive", "png", FhirIcon.primitiveBase64),
	PROFILE("icon_profile", "png", FhirIcon.profileBase64),
	REFERENCE("icon_reference", "png", FhirIcon.referenceBase64),
	RESOURCE("icon_resource", "png", FhirIcon.resourceBase64),
	REUSE("icon_reuse", "png", FhirIcon.reuseBase64),
	SLICE("icon_slice", "png", FhirIcon.sliceBase64),
	TARGET("target", "png", FhirIcon.targetBase64),
	// Note this one won't work until we host our icons ourselves, since the NHS Digital site doesn't have this icon
	MODIFIER_EXTENSION_SIMPLE("icon_modifier_extension_simple", "png", FhirIcon.modifierExtensionSimpleBase64);
	
	private final String name;
	private final String extension;
	private final String base64;
	
	// Set on startup. Path to folder containing extension files.
	private static String suppliedResourcesFolderPath = null;
	public static void setSuppliedResourcesFolderPath(String suppliedResourcesFolderPath) {
		FhirIcon.suppliedResourcesFolderPath = suppliedResourcesFolderPath;
	}
	
	FhirIcon(String name, String extension, String base64) {
		this.name = name;
		this.extension = extension;
		this.base64 = base64;
	}
	
	public String getCSSClass() {
		return "fhiricon-" + name.replaceAll("_", "-");
	}
	
	public String getUrl() {
		return HTMLConstants.NHS_IMAGES_DIR + name + "." + extension;
	}
	
	public String getBase64() {
		return base64;
	}
	
	public String getAsDataUrl() {
		return "url('data:image/" + extension + ";base64," + base64 + "')";
	}

	public static FhirIcon forElementDefinition(ElementDefinitionDt definition) {
		List<Type> types = definition.getType();
		
		if (definition.getPath().endsWith("[x]")) {
			return FhirIcon.CHOICE;
		}
		
		if (!types.isEmpty()) {
			for (Type type : types) {
				String typeName = type.getCode();
				if (typeName != null) {
					
					if (typeName.equals("Extension")) {
						/*
						KGM 25/Apr/2017
						*/
                        return lookupExtension(type, definition);
                    } else {
						Optional<Class<?>> maybeImplementingType = FhirDataTypes.getImplementingType(typeName);
						
						if (maybeImplementingType.isPresent()) {
							Class<?> implementingType = maybeImplementingType.get();
							
							if (ResourceReferenceDt.class.isAssignableFrom(implementingType)) {
								return FhirIcon.REFERENCE;
							}
							
							if (ICompositeDatatype.class.isAssignableFrom(implementingType)) {
								return FhirIcon.DATATYPE;
							}
							
							if (BasePrimitive.class.isAssignableFrom(implementingType)) {
								return FhirIcon.PRIMITIVE;
							}
						}
					}
				}
			}
		}
		
		if (!definition.getSlicing().isEmpty()) {
			return FhirIcon.SLICE;
		}
		
		String path = definition.getPath();
		if (!Strings.isNullOrEmpty(path)) {
			
		} else {
			System.out.println("Null or empty path");
		}
		
		return FhirIcon.ELEMENT;
	}

	private static FhirIcon lookupExtension(Type type, ElementDefinitionDt definition)  {

		FhirContext ctx = SharedFhirContext.get();

		List<UriDt> profiles = type.getProfile();
		if (profiles.isEmpty()) {
		    // Extension isn't profiled. So using base type and is simple
			return FhirIcon.EXTENSION_SIMPLE;
		}

		boolean hasPrimitiveExtension = true;

		for (UriDt uriDt : profiles) {

			String fileName;
			try {
				URI uri = new URI(uriDt.getValue());
				fileName = uri.toURL().getFile() + ".xml";
			} catch (URISyntaxException | MalformedURLException e) {
				throw new IllegalStateException("URI/URL error for uri " + uriDt.getValue(), e);
			}
			
			fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
			
			String pathName = suppliedResourcesFolderPath + fileName;
			File file = new File(pathName);
			
			if (!NewMain.STRICT && !file.exists()) {
				for (File f : new File(suppliedResourcesFolderPath).listFiles()) {
					if (f.getName().toLowerCase().equals(fileName.toLowerCase())) {
						file = f;
						break;
					}
				}
				
				if (fileName.equalsIgnoreCase("extension-careconnect-gpc-nhscommunication-1.xml") && !file.exists()) {
					System.out.println("FIXING PATH FOR " + fileName);
					fileName = "Extension-CareConnect-NhsCommunication-1.xml";
					file = new File(suppliedResourcesFolderPath + fileName);
				}
			}
			
			try (FileInputStream fis = new FileInputStream(file)){
				Reader reader = new InputStreamReader(fis);
				IParser parser = ctx.newXmlParser();
				StructureDefinition extension = parser.parseResource(StructureDefinition.class, reader);
				// KGM 8/May/2017 System.out.println(extension.getFhirVersion());

				for (ElementDefinitionDt element : extension.getSnapshot().getElement()) {
					if (element.getPath().contains("Extension.extension.url")) {

						hasPrimitiveExtension = false;
					}
				}

			} catch (IOException ie) {
				throw new IllegalStateException(ie);
			}
		}

		if (hasPrimitiveExtension) {
			return FhirIcon.EXTENSION_SIMPLE;
		} else {
			return FhirIcon.EXTENSION_COMPLEX;
		}
	}

	private static final String choiceBase64 = "R0lGODlhEAAQAMQfAGm6/idTd4yTmF+v8Xa37KvW+lyh3KHJ62aq41ee2bXZ98nm/2mt5W2Ck5XN/C1chEZieho8WXXA/2Gn4P39/W+y6V+l3qjP8Njt/lx2izxPYGyv51Oa1EJWZ////////yH5BAEAAB8ALAAAAAAQABAAAAWH4Cd+Xml6Y0pCQts0EKp6GbYshaM/skhjhCChUmFIeL4OsHIxXRAISQTl6SgIG8+FgfBMoh2qtbLZQr0TQJhk3TC4pYPBApiyFVDEwSOf18UFXxMWBoUJBn9sDgmDewcJCRyJJBoEkRyYmAABPZQEAAOhA5seFDMaDw8BAQ9TpiokJyWwtLUhADs=";
	private static final String datatypeBase64 = "R0lGODlhEAAQAOZ/APrkusOiYvvfqbiXWaV2G+jGhdq1b8GgYf3v1frw3vTUlsWkZNewbcSjY/DQkad4Hb6dXv3u0f3v1ObEgfPTlerJiP3w1v79+e7OkPrfrfnjuNOtZPrpydaxa+/YrvvdpP779ZxvFPvnwKKBQaFyF/369M2vdaqHRPz58/HNh/vowufFhfroxO3OkPrluv779tK0e6JzGProwvrow9m4eOnIifPTlPDPkP78+Naxaf3v0/zowfXRi+bFhLWUVv379/rnwPvszv3rye3LiPvnv+3MjPDasKiIS/789/3x2f747eXDg+7Mifvu0tu7f+/QkfDTnPXWmPrjsvrjtPbPgrqZW+/QlPz48K2EMv36866OUPvowat8Ivvgq/Pbrvzgq/PguvrgrqN0Gda2evfYm9+7d/rpw9q6e/LSku/Rl/XVl/LSlfrkt+zVqe7Wqv3x1/bNffbOf59wFdS6if3u0vrqyP3owPvepfXQivDQkO/PkKh9K7STVf779P///////yH5BAEAAH8ALAAAAAAQABAAAAfNgH+Cg36FfoOIhH4JBxBghYl/hQkNAV0IVT5GkJKLCwtQaSsSdx9aR26Gcwt2IkQaNRI6dBERIzCFDSgWSW8WCDkbBnoOQ3uFARc/JQJfCAZlT0x4ZFyFBxdNQT9ZCBNWKQoKUQ+FEDgcdTIAV14YDmg2CgSFA0hmQC5TLE4VRTdrKJAoxOeFCzZSwsw4U6BCizwUQhQyEaAPiAwCVNCY0FCNnA6GPAwYoETIFgY9loiRA4dToTYnsOxg8CBGHE6ICvEYQ4AKzkidfgoKBAA7";
	private static final String elementBase64 = "R0lGODlhEAAQAMQfAOvGUf7ztuvPMf/78/fkl/Pbg+u8Rvjqteu2Pf3zxPz36Pz0z+vTmPzurPvuw/npofbjquvNefHVduuyN+uuMu3Oafbgjfnqvf/3zv/3xevPi+vRjP/20/bmsP///////yH5BAEAAB8ALAAAAAAQABAAAAV24CeOZGmepqeqqOgxjBZFa+19r4ftWQUAgqDgltthMshMIJAZ4jYDHsBARSAmFOJvq+g6HIdEFgcYmBWNxoNAsDjGHgBnmV5bCoUDHLBIq9sFEhIdcAYJdYASFRUQhQkLCwkOFwcdEBAXhVabE52ecDahKy0oIQA7";
	private static final String extensionBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJvSURBVDjLpZPrS5NhGIf9W7YvBYOkhlkoqCklWChv2WyKik7blnNris72bi6dus0DLZ0TDxW1odtopDs4D8MDZuLU0kXq61CijSIIasOvv94VTUfLiB74fXngup7nvrnvJABJ/5PfLnTTdcwOj4RsdYmo5glBWP6iOtzwvIKSWstI0Wgx80SBblpKtE9KQs/We7EaWoT/8wbWP61gMmCH0lMDvokT4j25TiQU/ITFkek9Ow6+7WH2gwsmahCPdwyw75uw9HEO2gUZSkfyI9zBPCJOoJ2SMmg46N61YO/rNoa39Xi41oFuXysMfh36/Fp0b7bAfWAH6RGi0HglWNCbzYgJaFjRv6zGuy+b9It96N3SQvNKiV9HvSaDfFEIxXItnPs23BzJQd6DDEVM0OKsoVwBG/1VMzpXVWhbkUM2K4oJBDYuGmbKIJ0qxsAbHfRLzbjcnUbFBIpx/qH3vQv9b3U03IQ/HfFkERTzfFj8w8jSpR7GBE123uFEYAzaDRIqX/2JAtJbDat/COkd7CNBva2cMvq0MGxp0PRSCPF8BXjWG3FgNHc9XPT71Ojy3sMFdfJRCeKxEsVtKwFHwALZfCUk3tIfNR8XiJwc1LmL4dg141JPKtj3WUdNFJqLGFVPC4OkR4BxajTWsChY64wmCnMxsWPCHcutKBxMVp5mxA1S+aMComToaqTRUQknLTH62kHOVEE+VQnjahscNCy0cMBWsSI0TCQcZc5ALkEYckL5A5noWSBhfm2AecMAjbcRWV0pUTh0HE64TNf0mczcnnQyu/MilaFJCae1nw2fbz1DnVOxyGTlKeZft/Ff8x1BRssfACjTwQAAAABJRU5ErkJggg==";
	private static final String extensionComplexBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJ5SURBVDjLpZPNS1RhFMaff2EWLWo5tGnRaqCFRBAM0cZFwVSQpVHNQAWVMQwaSSZWtimLiKnsO5lEjKzs4y1zRK3oItfMj1FnnJkaUtNrjo45H3eejpCKNa5anMX73vs855zfOS9I4n9i2SHbCpvph8q8A9PNcCzcz76EM9EETj+DmmqENaeBiJ3mRyuzQy5mwyVMKqiFbzNN0MxgKZOd2zj5GMZE/ZL5ooHZAntGW89s7Bw5Ws25llWcfQHrzHPYE/51ZOQ0M4Fiitj4UQdbzhZSb+FJ63ZypJqp7p0UsTf+FN6kvoMMl3GmNY9jj+BckcF8/HoFldLzpZIqxhthJPVdkr2cifdb5sXefyAKLFvyzVJJAssisIxstILZ0DEyeJzpHifHfNBGamFZ+C9yC7bhG7BBxCrZZqWQpoiNP6S1TMBFDh4gA0VMdxfy+0NosftQX+8gGKkBY741HLoGhbnXUOZwKTn+gGa4nOlBN9MDxdJzCTmwj+wvEKPDTPUc5Zx+kOk+NxmqZOJTIXsviYGQVgKLAos/n0CbbIAS0ir1eY9kF4O+3UzpBYzehhaugQpdR3DwKth7EeyqEoO/oYzXwyKwDDN0ipme/VKFi0l9L8M3oYW8SwxWnIKI1XT7Vqb6i/ntLoLTHdulhROcUJsZuJJjCsvEPpyf8m8io5U0VB6FtFNIe6da84XFEcYaNrDzLDw5DUZ9cEwqm6zxGWYGPBTShogtQtoerV0rLA5JKy5+ubya7SdzbKKMyRG7ByPeIfvebKfAWszUdQFavKOI0bqNbCuF4XfneAvzIaStQrpOxEpIL746rQKOD2VQbSXwtLiXg/wNTNvAOhsl8oEAAAAASUVORK5CYII=";
	private static final String modifierExtensionSimpleBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4AcOAxIJiwFsLwAAAPhJREFUOMvtk7FKA0EURc+sgURYgkVCIilS+gfROiB+gH6AVYQU1inSJ2XqtCntUvkNwc4mYCHqFrqFoOjOzowzz8pONgtpLLzwqne5PM7lKRFhG0VsqT8eYE7P+/rkrF/kqRQtg7MTjAU4LB1gBpe76LxHZo+j/XaPOCM7OJoE467xahU/3ejiC7yfqfreQFo1onYL0TlRbkfykIwkfZ8DF8UMrBvKc7pEKX5GdkDS12WQMNwIsbqY++DsWNZ3iNagLWF9D1TG9WTly0E0tkO1xtfi6iMYh2o04iCfHeC2XI3GNsNjMsX6LqK68vI2RVTzN6v6/wW+AfnubFleAJy6AAAAAElFTkSuQmCC";
	private static final String extensionSimpleBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAADdSURBVDjLY/j//z8DJZhhmBvw41KQ07dzbk5kG/Dtgu/Jb2fsT5JkwI+bqZw/rsfYA21v+XE97f+PS5H/vx5Ra/98QN7+824ZTiIMSJr580bW/x+3iv//etD9/+fdpv/fzwX+/3LY6P/n7TIzCRtwPYYZaPvGH7dKgAb0AA1o/v/tQsh/oO0bP26TZiYqDIB+1/1+wef/z3vN/3/erPr/5aAOyHZdogMRGPIe38/7gvz+Gej3z18OG/8H2u5BvAFn7GO/Htdv/3pAQejzXjkhoO3tH7dIxY7EpEwMBgAr6O5Q8udliwAAAABJRU5ErkJggg==";
	private static final String externalBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKAQMAAAC3/F3+AAAABlBMVEUA2RJbkNPV4PPWAAAAAXRSTlMAQObYZgAAAAFiS0dEAIgFHUgAAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQffCBEAABDq3m8FAAAAIElEQVQI12NgPsDAeIDh8wGGdgeGPgaGHgaGRhj6zwAAfRwHm4TMMaMAAAAASUVORK5CYII=";
	private static final String primitiveBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3gYBFzI0BrFQCwAAAERJREFUOMtj/P//PwMlgImBQjDwBrCcOnWKokBgYWBgYDCU+06W5i8MUggvnH/EOVJjAW4AuQHJ+O75LYqikXE0LzAAALePEntTkEoSAAAAAElFTkSuQmCC";
	private static final String profileBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAAsTAAALEwEAmpwYAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAADCElEQVR42qSQS29UZQCGn+873/lm2rlRynRoQZpAGhAr9QJGYjBqdEPiJUjQaHRtXBljjITEhRsX6g8wbtSoMboQEhXTssFgSqqGaklLbCu90LGdYTqXc2bOnOvnYvgHvD/gyfO8whjD3Uxyl1MAjUtnmLk8xbdrz/HJuVOkNj778J9rvwxUavIDbRfKR0++K8vXv39gbXn+ulE6WKvGRBEIcQcA5oStTDB28MiRmflacXTXo+/lj4+jb1181iuvftVt/FQaKC2/LJU9W6mIr5MKnxuDAyCMMTR+e2nSKV94Qu04Ybfzp7D3nUHpDIR16PxFWJlCNi+TipcRSCYvJW9UKsmnSt35wLKGrqYy/bYdXiFdfotocxIj0nSjNK58kGD3WYKD56kMfUmzuxOSwJMSpDS9BEsXZ4TMIiQUMiH18ttUt2Zpqwna7CWIBYHfIZ+xKbb9juOKq306IjJWDxDGuWkrNVglrheFpSkVHYbCj+l2+6m7w8y7p2lZT5KJ/8Bv1W4Ik/y77ozw5+b9vYTF1cYxt7NLSmlAJMQJCJ0mnTGMDC4xKr4gDjoU1Cpu07+ytL0v+nHpadaaIz2DbCH9ou57fLC9PUtOJ9S917CTabTYIAxLaOmgwxVUsML5aw83ppaOHk+rKKut0CiAZqPRGD38DJvOTZr1n9HDw8TmTULLoFI7yOfXOVTtoPa8z9+1785q5Z5TtrQMoqoA3MbtX12n8U5p/BWaG+OEfpVY1MnvfojsYAkTjBLYC6TuOUwu12c7joOyBUDcM6htXVyZm/79wPjEsYG996LsCQQxQhhMu0IceUR+m7wtyOZsbA12SgImUQBSymh5Ye7VJHR+2D924L7CziJCCEwSkcQxYbdFt1kjDH36sxqVEui0AETvRCEEQojFhbkbJ313+5s9w7nHOm0Pz/Pantetd1xnqxkWWvuLtx6xlMnotIWdEoDICGMMFz56Ht/38T0fP/Dy0nhPtZxOy/PDdROb2wN5y20lpWhRvzBW2frv9SgKDwkh0kD7/wEAzc5u8hho91cAAAAASUVORK5CYII=";
	private static final String referenceBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAadEVYdFNvZnR3YXJlAFBhaW50Lk5FVCB2My41LjEwMPRyoQAAAFxJREFUOE/NjEEOACEIA/0o/38GGw+agoXYeNnDJDCUDnd/gkoFKhWozJiZI3gLwY6rAgxhsPKTPUzycTl8lAryMyMsVQG6TFi6cHULyz8KOjC7OIQKlQpU3uPjAwhX2CCcGsgOAAAAAElFTkSuQmCC";
	private static final String resourceBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAJBSURBVDjLhdKxa5NBGMfx713yvkmbJnaoFiSF4mJTh06Kg4OgiyCCRXCof4YIdXdxFhQVHPo3OFSoUx0FySQttaVKYq2NbdO8ed/L3fM4JG3tYPvAcfBw9+HHPWdUlf/V0tLSqKo+EpEHInJFRIohhDUR+RBCeDM7O7ua55QSkRfVanVufHyckZERrLV0Op2Zra2tmXq9fg+YsmcAdyYnJykUCke9OI6ZmJgghHAZ4KwE3ntPs9mkVCohIjQaDWq1GiEEAM5KoHEcY62lVCrRarUoFotUKpUjIL/y/uqXYmV62ph/LSVrr30P4bEFcM4B0Ov1jk547/uAUTs1ceNdZIwB7V/GGHz6+9LXxY96eDiEgHMOY8xJAK8p4grZz5cElwNbwZgyxYu3EFM01lriOCZJEqIoIooiALIsGwA9Y1UcwcWoKNLdpLu9zvbnBWqNBhuvn5EDUmB0EH/1E2TZw5U+YLQovkun+Ytsaw1xCbnCOap334LC7s4Oe/ttvA+ICLmhMXRxDufczUECS37oAuevPwUEVFFp4/eXkXSdYc2IopSepnjtUh5/wg9gfn6+OQBUNaRIUkfDHhraSLoBKqikIF3yHJDLHaAkFOLciVHnyVAVj/S2Ub/XRyQD9aAZKgkaOohvo6ENgykcA07VEFDfQv1uf4W9Y8y30bCPhg4qKZJtMnjTPqBO/vhkZ7h3EJeRslWNQMqgY2jIAIfa/m5sIKSpqpPsGEiz599e3b+GchtD+bSvjQJm2SG6cNj6C+QmaxAek5tyAAAAAElFTkSuQmCC";
	private static final String reuseBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAKjSURBVDjLrZLdT1JhHMfd6q6L7voT3NrEuQq6aTircWFQ04u4MetCZ4UXGY2J1UoMdCoWxMZWDWtrrqHgylZ54RbkZtkiJ5aAku8MXXqQl3PgAOfb8xwq5LrOzmfnd34vn+d5tqcMQNm/UPZfBMHXx2ZWvI386odLe7jIL7w5EQ68qjhEOFjCmMj+v4LQmCwtCHkSCuSlFOIst4X1KU1mbUqT/kPki57bmL6xEnx55HxRMCqNCTkO6fUBpH5YkFoeBLsyAiHLEFcSQi5B2C38Z3eAPJ8JjcrmigKnLJ7nd8mwDcnFh4h/68T29FVsfW4F4zeCmb0LZqYDO191hOtkZ5sIuY8lioJhKZ9lo2DmbNjx9WDTowW7+YmsGv+9Ov3GijsgxwsNy7iiYOg4L54/nyawQC4lDubYANIRG7g1I9glHVILl5EMNCCXnEfouXSP4JksI+RY5OIfkWXGwf8cQSb6hAz2gV2+BXaxFangBSS/n0PCfxq5xAxCg3sFj2TpPB8Hvz2G3dWneOvqhLnPCIfDgd5uPebfNyAyrUR/t1bMmft7MdR1NiuXyw8UBDYpJ/AMkhsOPLa2wmKxIBqNIhwOw+Px4EG/Hvb7GoSCc2JucnJS7FEqlb2FizRwNMLHFmPvXnQJN/U6+Px+3LvdApVKiebmZlitVuj1ejFWqc7AZNCJEq1WGxMFAVPFtUCPZKhDXZUyGu6IAr+pklOclGNiYgI+nw9erxculws0N2uqjFOBwWDgSu61RCK50tLSwlBBfX39eE1NDa9QKFBXVydCY5qjNSqgvSWCw+RRqVTzZrOZcTqd2263G3a7HW1tbWhvbxdjmqM12kN7SwTl5eX7qqurq2pra5eampqSGo2GI2TUanUj4RSJ4zRHa7SH9v4C8Nrl+GFh7LoAAAAASUVORK5CYII=";
	private static final String sliceBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAAsTAAALEwEAmpwYAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAAB6JQAAgIMAAPn/AACA6QAAdTAAAOpgAAA6mAAAF2+SX8VGAAADIElEQVR42myTS2tcZQCGn+871zmTmcxk0mhrEtI0N7GNKWYhEhAiCK24EYR27aJrly4K/gNFcOFGCi24FLctLqUBbwSTaZo2qYlN0qaTmWTmzMw557scFxF10Ree5ftsXl5ROl/Br3golVE+V54vnxlYiAbDUrfZ6zR2WqutjeZqEIV092JeFtdqS1AKpheX3/7kwlsz71Reiaa80AlVYpLWfndrc6V+//cffv62uxdvvEwgBueqi0s3l269eXXxjeOsg3K7CN+SZxLfDFAJSvz6/cqf927cfddquyvEf+U8z3HDoj9qQjv+x+M1pB8QDlgcH0wqSLttdlMFZTHhFt1x3VW7wjk1aK3x/RBXJaZvcj8ulwolS5lceuQCcMANMvxCTLKT9U2iM6stwgqssoxPzzA1M4+relnn+MFhX3qKcMTg10rkjsQqQ9bs0H/a4tlPz+Pc0gSwyjI2Ocvy1Ws8+2sLN6oVaoPnSxXdt7TWXiA4AJFjFVglcCOH4fmhihe5taSpH4/PzvHelesIKTBG4+pUK7cYqMqlIXLVR7oG6XEq0A4ijDhebdi0kzB2YZblK9cQUmKNPp1RxSpu1o/6InQonAE5JMm9AK01aSeju5nw9N5OJ4jOHiy9/zGZ0hiTIgEpJW5YK1RLo8Vy2uhx/EQxH0wzWfPI+hnZSYZOFM7FS1X7afmL0YnLh8YoRwghbI797s5vX7kmNdqrhHrwYpXeCXx4aPjg8p3ToaU4Betgpz7qyM/Bi3AkJJni1jdfbruqp5JWvalF4EA1Qh11oB6DC+T/AJDt05poYL0BXJkT9xLyHOkWhsLSwLmo0H8e030U0/BeZ2djGYP9ty0l5IXXiM/mkCU4UtBLNUqrzNV9pYJaJCoLw9ikzY/HTe4eTWKNxSiJ9Au0HzXZ/nrrs6J/42FujRRCCGMM2xvrK07STBrt7XbqF/05ryjLzgh4EwYqCYoTXmwdNNZu37+9/8vDm0KIdaR8kGVpPU2Tert11P7fNZgMh8KFcDgac0InMonJkkbvIGkm68AaYKrDIwy/OkpuLcYY9p5s8vcAX+qXtAX9dqAAAAAASUVORK5CYII=";
	private static final String targetBase64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAADk0lEQVR42l2TbWxTdRTGn/+9XW+7vmHbdR0bg8rYmG5OaRgBB4Vow+aWOf3Ai8OXoe6DomEhkBgSo8YYSTRuGqcBphHmSAgYjFTGdC5ASB2UjXVFYF23dXYt27qWvt/2tvdal0CmJznJ+XCeX07OeQ7B/6JljN1DE+FggpsqkstKqVRqho+zDi5NmTwBf+bwgEl5cWk/eVBUn7IzO1fbRqxDF8tmJvzgIEO5sQ7FxgZ0DZdAIzRj4+PHYo655F80nzBdfUaTeAjYNhgWNeY6x26Mmg1npEmwVAxvps9ANXsJpnWrQEJ/wsMqES59G5ZABUg84OConOp/IYuAQ9NJezzyReXZkU7omvTQSouhui/Hjl47+iZNeG/DIEoMIUzHS/Fx8CPcCS+Hivb8er62qp483b9gaDSEJiyxCiicxdglbIZrBY8yuxtDdjlq2rpQE94NNeNCWsiBzaXDkQ0ajNyOBAqT3WaydzxlS2RSxjdE27C2g8OnQR18IjmeoqVoLMigePVNKPLcIPLssITC6WEl9stegm82gfUrmyzktTvpOamWzms+9zo2nu2B74lncf2dX1B35RMw0QXEY79DonGAYnhkMsBnvA4X6O9QSJXA6v7eRXaOpu/rVbSq5UQrqga6AKkcKHoUyF+xWC8kh6HId0KgMoiyFI6IH8G0lkNBdqIr1vJ58sKNdEghoZUNti/xYvcB0BAA+TJApYUgZtCuW4e7BRXIY9phDvtQQivhjybQcIuFxlAWINuvce4cgS4uiLmw9dyHqLt7CkKSwCkToTOPQarpa/BrmuGd9GDN2FHUJztRjggsQTmGsH6KmC8n+rxBxrxcDUj4APb9+DzIqA3thRz6lDy0TDM2v9WNYBoY+zuNyvEevOzfi03qHFy/tyxIGi/Fqu2TkssUCKPNJzhsawM30I12UQQ3I2tR0doLUakeIS8wP5NdT7QDs579OCTVx9R86taikYw/xS9MjUtqJbkEx/wHkPntOL6aS8H5ym3kVq0CO88jEaQgxAAuug8B+7fRLbUn/qjp37N7EZA1k3hhVjoY8Eqf/Mb7LlTDJ9FT1IJ+0+dZMSBw2cyeUKonSLOjSbXIbVU9tum5h1Z+AImHJZb3rx7corf+ID6/chdOVh8FxxIQESCSZAH5wryCiTgYWab+P8+0NHp3GDsmPL7Wn7d+EPJUvhoiYiIW0Rle4Pl7chXblhVeW9r/Dx49g+gajg94AAAAAElFTkSuQmCC";
}