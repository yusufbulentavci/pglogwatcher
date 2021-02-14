package pglogwatcher;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

public class CsvErrorTest extends ScenarioTest {

	@Test
	public void denebakalim() throws IOException {
		File dir = new File("/tmp/csverror");
		FileUtils.deleteDirectory(dir);
		dir.mkdir();
		ExtraFileUtils.copyResourcesRecursively(
				new URL(ExtraFileUtils.class.getResource("/scenarios/csverror").toString()), new File("/tmp"));

		LogDir ld = new LogDir("/tmp/csverror", 0, 0);
		ld.run();

		List<String> original = Files
				.readAllLines(new File("/tmp/csverror/postgresql-2021-02-08_10_31_38.csv.json").toPath());
		List<String> revised = Files
				.readAllLines(new File("/tmp/csverror/expection/postgresql-2021-02-08_10_31_38.csv.json").toPath());
		
//		//compute the patch: this is the diffutils part
		Patch<String> patch = DiffUtils.diff(original, revised);

		if (patch.getDeltas().size() > 0) {
			// simple output the computed patch to console
			for (AbstractDelta<String> delta : patch.getDeltas()) {
				System.out.println(delta);
			}
			assertTrue(false);
		}

		assertTrue(!new File("/tmp/csverror/postgresql-2021-02-08_10_31_38.csv-done").exists());
		assertTrue(new File("/tmp/csverror/postgresql-2021-02-08_10_31_38.csv").exists());
		
	}

//	public static void main(String[] args) throws IOException {
//		csverrorTest.denebakalim();
//
//	}

}
