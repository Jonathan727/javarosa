package org.dimagi.view.widget;


import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import org.dimagi.chatscreen.Constants;

import org.dimagi.utils.ViewUtils;
import org.dimagi.view.Widget;

public class Textbox extends Widget {

	int fontHeight;
	String str = "";
	
	/**
	 * Creates a new textbox widget
	 */
	public Textbox() {
	}
	
	/**
	 * Sets the height of the widget
	 */
	public void sizeWidget() {
		fontHeight = Font.getDefaultFont().getHeight();
		this.setHeight(fontHeight * 2);
	}
	
	public void sizeWidget(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}
	
	public void drawInactiveWidget(Graphics g) {
		int xBufferSize = this.getWidth()/10;
		int yBufferSize = this.getHeight()/10;
		g.setColor(ViewUtils.BLACK);
		Font theFont = Font.getDefaultFont();
		g.drawString(str, getWidth()-20, yBufferSize, g.TOP | g.RIGHT);
	}
	
	public void drawActiveWidget(Graphics g) {
		int xBufferSize = this.getWidth()/10;
		int yBufferSize = this.getHeight()/10;
		g.setColor(ViewUtils.BLACK);
		g.drawRect(0, yBufferSize, getWidth()-xBufferSize, fontHeight);
		g.drawString(str, 5, yBufferSize, g.TOP | g.LEFT);
	}
	
	protected void drawInternal(Graphics g) {
		if ( this.isActiveWidget() ) {
			drawActiveWidget(g);
		} else {
			drawInactiveWidget(g);
		}
	}
	
	public void keyPressed(int keyCode) {
		if (keyCode >= 0) {
			str += (char)keyCode;
		} else if (keyCode == -11) { // delete key
			str = str.substring(0, str.length()-1); // remove last character
		}
		refresh();
		this.setShortAnswer(str);
	}
	
}
