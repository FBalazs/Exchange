package hu.berzsenyi.exchange;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class DatParser {
	private String path;
	private Map<String, String> map;

	public DatParser(String path) {
		this.path = path;
		map = new HashMap<String, String>();
	}
	
	public String getPath() {
		return path;
	}
	
	public String getValue(String variable) {
		return map.get(variable);
	}
	
	public void setValue(String variable, String value) {
		map.put(variable, value);
	}
	
	public void parse() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			String line = br.readLine();
			while (line != null) {
				if (line.startsWith("//") || line.startsWith("#"))
					map.put(line, "");
				else {
					String[] split = line.split("=");
					if (split.length == 2)
						map.put(line.split("=")[0].trim(), line.split("=")[1].trim());
					else
						map.put(line, "");
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
