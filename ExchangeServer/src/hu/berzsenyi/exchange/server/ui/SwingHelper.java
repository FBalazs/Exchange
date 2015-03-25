package hu.berzsenyi.exchange.server.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JButton;

public class SwingHelper {
	public static void setButtonTextSizeToFill(JButton btn) {
		btn.setFont(new Font(btn.getFont().getFontName(), btn.getFont().getStyle(), btn.getFont().getSize()*btn.getWidth()/2/btn.getFontMetrics(btn.getFont()).stringWidth(btn.getText())));
	}
	
	public static void splitComponentsVertical(Rectangle space, double spacing, Component... components) {
		double relH = 1/(components.length+spacing*(components.length-1));
		for(int i = 0; i < components.length; i++)
			components[i].setBounds(space.x, (int)(space.y+(space.height*relH*i*(spacing+1))), space.width, (int)(space.height*relH));
	}
}
