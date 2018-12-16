import com.vc.lib.cmd.Terminal;
import com.vc.lib.util.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test {
	private static final Logger LOG = LogManager.getLogger();

	public static void main(String[] args) {
		runService();
	}

	public static void logSomeMessages() {
		String msg = "Test log message. (Level: {})";
		LOG.trace(msg, "TRACE");
		LOG.debug(msg, "DEBUG");
		LOG.info(msg, "INFO");
		LOG.warn(msg, "WARN");
		LOG.error(msg, "ERROR");
		LOG.fatal(msg, "FATAL");
	}

	public static void runTerminalService() {
		Terminal cmd = new Terminal();
		cmd.attach(new Service() {
			@Override
			public boolean start() {
				setLoopRate(5000);
				return super.start();
			}

			@Override
			protected void run() {
				LOG.info("Hello, world!");
			}
		});

		try {
			cmd.join();
		} catch (InterruptedException e) {
		}
	}

	public static void runService() {
		Service svc = new Service() {
			@Override
			public boolean start() {
				setLoopRate(5000);
				return super.start();
			}

			@Override
			protected void run() {
				LOG.info("Hello, world!");
			}
		};

		svc.start();
	}
}
