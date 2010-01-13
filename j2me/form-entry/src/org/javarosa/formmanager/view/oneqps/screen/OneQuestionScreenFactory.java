/**
 * 
 */
package org.javarosa.formmanager.view.oneqps.screen;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.services.UnavailableServiceException;
import org.javarosa.formmanager.view.FormElementBinding;
import org.javarosa.formmanager.view.oneqps.acquire.IAcquiringService;

import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;

/**
 * @author melissa
 * 
 */
public class OneQuestionScreenFactory {

	public static OneQuestionScreen getQuestionScreen(
			FormElementBinding prompt, boolean fromFormView,
			boolean goingForward) {
		OneQuestionScreen screenToReturn = null;
		int qType = prompt.instanceNode.dataType;
		int contType = ((QuestionDef) prompt.element).getControlType();

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? "OneQPS_Form_Right"
						: "OneQPS_Form_Left");

		switch (contType) {
		case Constants.CONTROL_INPUT:
			switch (qType) {
			case Constants.DATATYPE_TEXT:
			case Constants.DATATYPE_NULL:
			case Constants.DATATYPE_UNSUPPORTED:
				screenToReturn = new TextQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_DATE:
			case Constants.DATATYPE_DATE_TIME:

				screenToReturn = new DateQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_TIME:
				screenToReturn = new TimeQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_INTEGER:
				screenToReturn = new NumericQuestionScreen(prompt, style);

				break;
			case Constants.DATATYPE_DECIMAL:
				screenToReturn = new DecimalQuestionScreen(prompt, style);

				break;

			case Constants.DATATYPE_BARCODE:
				screenToReturn = new TextQuestionScreen(prompt, style);
				break;

			default:
				screenToReturn = new TextQuestionScreen(prompt, style);

			}

		case Constants.CONTROL_SELECT_ONE:
			screenToReturn = new Select1QuestionScreen(prompt, style);

			break;
		case Constants.CONTROL_SELECT_MULTI:
			screenToReturn = new SelectQuestionScreen(prompt, style);

			break;
		case Constants.CONTROL_TEXTAREA:
			screenToReturn = new TextQuestionScreen(prompt, style);

			break;
		default:
			throw new IllegalStateException(
					"No appropriate screen to render question");

		}

		return screenToReturn;
	}

	public static OneQuestionScreen getQuestionScreen(
			FormElementBinding prompt, boolean fromFormView,
			boolean goingForward, IAcquiringService barcodeService) {
		OneQuestionScreen screenToReturn = null;
		int qType = prompt.instanceNode.dataType;
		int contType = ((QuestionDef) prompt.element).getControlType();

		Style style = StyleSheet
				.getStyle(fromFormView || goingForward ? "OneQPS_Form_Right"
						: "OneQPS_Form_Left");

		return barcodeService.getWidget(prompt, style);

	}

}
