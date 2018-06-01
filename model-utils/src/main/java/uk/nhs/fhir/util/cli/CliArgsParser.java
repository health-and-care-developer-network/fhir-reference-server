package uk.nhs.fhir.util.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public abstract class CliArgsParser<T> {
	private static final Logger LOG = LoggerFactory.getLogger(CliArgsParser.class);
	
	private final CliArgSpec spec;
	
	public CliArgsParser(CliArgSpec spec) {
		Preconditions.checkNotNull(spec);
		this.spec = spec;
	}
	
	public T parseArgs(String[] args) {
		ParsedArgs parsedArgs;
		try {
			 parsedArgs = spec.parseArgs(args);
		} catch (ArgParsingFailed e) {
			return error(e.getMessage());
		}
		
		return extractArgs(parsedArgs);
	}

	private T error(String message) {
		LOG.error(message);
		LOG.info(spec.getUsage());
		return null;
	}

	protected abstract T extractArgs(ParsedArgs parsedArgs);
}
