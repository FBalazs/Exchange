package hu.berzsenyi.exchange;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

//import javax.swing.JOptionPane;

public class Configuration {
	public String path;
	private List<String> variables, values;

	public Configuration(String path) {
		this.path = path;
		this.variables = new ArrayList<String>();
		this.values = new ArrayList<String>();
	}
	
	public String getValue(String variable, String defaultValue) {
		for(int i = 0; i < this.variables.size(); i++)
			if(this.variables.get(i).equals(variable))
				return this.values.get(i);
		this.variables.add(variable);
		this.values.add(defaultValue);
		return defaultValue;
	}
	
	public void setValue(String variable, String value) {
		for(int i = 0; i < this.variables.size(); i++)
			if(this.variables.get(i).equals(variable)) {
				this.values.set(i, value);
				return;
			}
		this.variables.add(variable);
		this.values.add(value);
	}
	
	public void read() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.path));
			String line = br.readLine();
			while (line != null) {
				if (line.startsWith("//") || line.startsWith("#")) {
					this.variables.add(line);
					this.values.add("");
				} else {
					String[] split = line.split("=");
					if (split.length == 2) {
						this.variables.add(line.split("=")[0].trim());
						this.values.add(line.split("=")[1].trim());
					} else {
						this.variables.add(line);
						this.values.add("");
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {

		}
	}

//	public void write() {
//		try {
//			PrintWriter pw = new PrintWriter(this.path);
//			for(int i = 0; i < this.variables.size(); i++)
//				if(this.values.equals(""))
//					pw.println(this.variables.get(i));
//				else
//					pw.println(this.variables.get(i)+" = "+this.values.get(i));
//			pw.close();
//		} catch (IOException e) {
//			JOptionPane.showMessageDialog(null, "Could not write config file!");
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}
}
