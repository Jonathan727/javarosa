package org.javarosa.formmanager.view.chatterbox.widget;

import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Item;

/**
 * Strategy object that knows how to build and maintain a widget that contains a view of a single form question. This interface
 * supports returning data back from the widget.
 */
public interface IWidgetStyleEditable extends IWidgetStyle {
	/**
	 * Get the data currently entered in the widget, in the datatype appropriate to the type of question this
	 * widget is tied to. If this widget represents a question or concept that is not meant to collect input, return null.
	 * 
	 * @return currently entered question data; null if not applicable
	 */
	IAnswerData getData ();
	
	//handle custom widget focusing. return whether any focusing was performed (thus needing repaint)
	boolean focus ();
	
	//specify how the 'answered' event is triggered
	int getNextMode ();
	
	//get item to set event listeners for 
	Item getInteractiveWidget ();
}
