package org.celllife.clforms.view;

import org.celllife.clforms.IController;
import org.celllife.clforms.api.Prompt;

public interface IPrompter  {
	
	public void showPrompt(Prompt prompt);
	
	public void showPrompt(Prompt prompt, int screenIndex, int totalScreens);

	public void registerController(IController controller);

}
