package uk.nhs.fhir.util;

import uk.nhs.fhir.util.cli.CliPathArg;
import uk.nhs.fhir.util.cli.CliStringArg;
import uk.nhs.fhir.util.cli.CliStringSetArg;
import uk.nhs.fhir.util.cli.CliFlag;

/**
 * These logically fit better into the profile-renderer deployable project, however it is useful if the same
 * arg specs can be available to the server/renderer so that any changes are inherited.
 */
public class RendererCliArg {
	public static final CliPathArg ARG_INPUT = new CliPathArg("input-dir", "Specification source directory");
	public static final CliPathArg ARG_OUTPUT = new CliPathArg("output-dir", "Rendered artefact destination directory");
	public static final CliStringArg ARG_NEW_PATH = new CliStringArg("newbaseurl", "New base URL for resources", "base-url", "b");
	public static final CliStringSetArg ARG_MISSING_EXT = new CliStringSetArg("extprefix", 
		"If an extension with this prefix is depended on but unavailable, the renderer will try to continue rendering without it",
		"missing-ext-prefix", "p");
	public static final CliStringSetArg ARG_LOCAL_DOMAINS = new CliStringSetArg("localdomain", 
		"Local domains (for resources hosted on this FHIR server)", "local-domains", "l");
	public static final CliFlag FLAG_COPY_ON_ERROR = new CliFlag("copy-on-error", "", "force-copy", "f");
	public static final CliPathArg ARG_HTTP_CACHE = new CliPathArg("httpcache", 
		"Directory to use to hold the HTTP cache, used when retrieving Git history (to avoid unecessary calls to Git)", "http-cache", "c");
}
