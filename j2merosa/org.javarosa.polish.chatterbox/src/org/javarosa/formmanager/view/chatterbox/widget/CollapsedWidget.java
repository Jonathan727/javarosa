package org.javarosa.formmanager.view.chatterbox.widget;

import javax.microedition.lcdui.Command;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;

/**
 * Default read-only view of a question: a single frame with question QuestionDef (abbreviated) on the left/top and 
 * question answer in readable-text form on the right/bottom.
 */
public class CollapsedWidget implements IWidgetStyle {
	private StringItem prompt;
	private StringItem answer;

	public CollapsedWidget () {
		reset();
	}

	/**
	 * TODO: be smart about layout/wrapping; take into account lengths of QuestionDef and answer for optimum
	 * use of vertical screen space
	 */

	public void initWidget (IFormElement question, Container c) {
		//#style split
		UiAccess.setStyle(c); //it is dubious whether this works properly; Chatterbox.babysitStyles() takes care of this for now

		//#style splitleft
		prompt = new StringItem(null, null);
		prompt.setDefaultCommand(new Command("Update", Command.ITEM, 1));
		
		//#style splitright
		answer = new StringItem(null, null); 

		//polish has a quirk where it really wants to impose the parent styling onto the first item in the
		//container, even if you explicitly override it with a new style. this null item takes the fall
		c.add(new StringItem(null, null));
		c.add(prompt);
		c.add(answer);
	}

	public void refreshWidget (IFormElement element, IAnswerData data, int changeFlags) {
		if(!(element instanceof QuestionDef)) {
			throw new IllegalArgumentException("element passed to refreshWidget that is not a QuestionDef");
		}
		QuestionDef question = (QuestionDef)element;
		prompt.setText(question.getShortText());
		if (data != null) {
			answer.setText(data.getDisplayText());
		}
	}

	public void reset () {
		prompt = null;
		answer = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.view.chatterbox.widget.IWidgetStyle#widgetType()
	 */
	public int widgetType() {
		return Constants.CONTROL_UNTYPED;
	}
}
