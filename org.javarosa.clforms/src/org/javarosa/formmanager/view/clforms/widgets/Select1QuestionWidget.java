package org.javarosa.formmanager.view.clforms.widgets;

import java.util.Enumeration;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Item;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.formmanager.view.clforms.SingleQuestionScreen;

public class Select1QuestionWidget extends SingleQuestionScreen
{
	protected ChoiceGroup cg;

	public Select1QuestionWidget(QuestionDef question) {
		super(question);
	}

	public void creatView() {
		setHint("You must select only one option");

		cg = new ChoiceGroup(qDef.getLongText(),ChoiceGroup.EXCLUSIVE ); //{
		Enumeration itr = qDef.getSelectItems().keys();//access choices directly
		int i = 0;
		while (itr.hasMoreElements()) {
			String label = (String) itr.nextElement();
			cg.append(label, null);//add options to choice group
			i++;
		}
		this.append(cg);
		this.addNavigationButtons();

	}

	//Utility methods
	public void setHint(String helpText)
	{
		//should be abstract and handled by question-type child classes.
	}

	public IAnswerData getWidgetValue() {

		return null;
	}


}