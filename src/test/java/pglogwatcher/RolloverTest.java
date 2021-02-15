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

public class RolloverTest extends ScenarioTest {

	@Test
	public void denebakalim() throws IOException {
		File dir = new File("/tmp/rollover");
		FileUtils.deleteDirectory(dir);
		dir.mkdir();
		ExtraFileUtils.copyResourcesRecursively(new URL(ExtraFileUtils.class.getResource("/scenarios/rollover").toString()),
				new File("/tmp"));

		LogDir ld = new LogDir("/tmp/rollover", 1, 1);

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					File file = new File("/tmp/rollover/postgresql-2021-02-08_10_32_00.csv-wait");
					file.renameTo(new File("/tmp/rollover/postgresql-2021-02-08_10_32_00.csv"));
					System.out.println("!!Test Renamed waiting");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}).start();

		ld.run();
		System.out.println();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> original = Files
				.readAllLines(new File("/tmp/rollover/postgresql-2021-02-08_10_31_38.csv.json").toPath());
		List<String> revised = Files
				.readAllLines(new File("/tmp/rollover/expection/postgresql-2021-02-08_10_31_38.csv.json").toPath());
		List<String> original1 = Files
				.readAllLines(new File("/tmp/rollover/postgresql-2021-02-08_10_32_00.csv.json").toPath());
		List<String> revised1 = Files
				.readAllLines(new File("/tmp/rollover/expection/postgresql-2021-02-08_10_32_00.csv.json").toPath());
//
//		//compute the patch: this is the diffutils part
		Patch<String> patch = DiffUtils.diff(original, revised);
		if (patch.getDeltas().size() > 0) {
			// simple output the computed patch to console
			for (AbstractDelta<String> delta : patch.getDeltas()) {
				System.out.println(delta);
			}
			assertTrue(false);
		}

		Patch<String> patch2 = DiffUtils.diff(original1, revised1);
		if (patch2.getDeltas().size() > 0) {
			// simple output the computed patch to console
			for (AbstractDelta<String> delta : patch2.getDeltas()) {
				System.out.println(delta);
			}
			assertTrue(false);
		}
		
		assertTrue(new File("/tmp/rollover/postgresql-2021-02-08_10_31_38.csv-done").exists());
		assertTrue(!new File("/tmp/rollover/postgresql-2021-02-08_10_31_38.csv").exists());
		assertTrue(new File("/tmp/rollover/postgresql-2021-02-08_10_32_00.csv").exists());
		assertTrue(!new File("/tmp/rollover/postgresql-2021-02-08_10_32_00.csv-done").exists());

	}

//	public static void main(String[] args) throws IOException {
//		tailTest.denebakalim();
//
//	}

}
