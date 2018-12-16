import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test {
	private static final Logger LOG = LogManager.getLogger();

	@org.junit.Test
	public void logSomeMessages() {
		String msg = "Test log message. (Level: {})";
		LOG.trace(msg, "TRACE");
		LOG.debug(msg, "DEBUG");
		LOG.info(msg, "INFO");
		LOG.warn(msg, "WARN");
		LOG.error(msg, "ERROR");
		LOG.fatal(msg, "FATAL");
	}
}
