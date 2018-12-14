import com.vicinityconcepts.lib.util.Log;
import org.junit.Test;

public class Logging {
	@Test
	public void printSomeText() {
		Log.info("Hello, world!");
		Log.warn("Uh oh, world...");
		Log.error("OH NO! World!!!");
	}
}
