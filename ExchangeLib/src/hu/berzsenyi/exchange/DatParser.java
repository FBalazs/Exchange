package hu.berzsenyi.exchange;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DatParser {
	public String path;
	private Map<String, String> map;

	public DatParser(String path) {
		this.path = path;
		this.map = new HashMap<String, String>();
	}
	
	public String getValue(String variable) {
		return this.map.get(variable);
	}
	
	public void setValue(String variable, String value) {
		this.map.put(variable, value);
	}
	
	public void parse() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.path));
			String line = br.readLine();
			while (line != null) {
				if (line.startsWith("//") || line.startsWith("#"))
					this.map.put(line, "");
				else {
					String[] split = line.split("=");
					if (split.length == 2)
						this.map.put(line.split("=")[0].trim(), line.split("=")[1].trim());
					else
						this.map.put(line, "");
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {

		}
	}
}
