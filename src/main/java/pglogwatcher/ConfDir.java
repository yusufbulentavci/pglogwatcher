package pglogwatcher;

public class ConfDir {
	private String path;

	public ConfDir(String path) {
		this.path=path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "ConfDir [path=" + path + "]";
	}

}
