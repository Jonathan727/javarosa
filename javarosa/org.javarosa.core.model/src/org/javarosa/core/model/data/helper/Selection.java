/*
 * Copyright (C) 2009 JavaRosa-Core Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.data.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a selection
 * from a list. 
 * 
 * @author Drew Roos
 *
 */
public class Selection implements Externalizable {
	public int index = -1;
	
	/* we need the questiondef to fetch natural-language captions for the selected choice
	 * we can't hold a reference directly to the caption hashtable, as it's wiped out and
	 * recreated every locale change
	 * we don't serialize the questiondef, as it's huge, and unneeded outside of a formdef;
	 * it is restored as a post-processing step during formdef deserialization
	 */
	public QuestionDef question; 
	
	public int qID = -1;
	public String xmlValue = null;
	
	/**
	 * for deserialization
	 */
	public Selection() {
		
	}
	
	public Selection (String xmlValue) {
		this.xmlValue = xmlValue;
		
		if (question != null) {
			//don't think setting these is strictly necessary, setting them only on deserialization is probably enough
			this.qID = question.getID();
			this.xmlValue = getValue();
		} //if question is null, these had better be set manually afterward!
	}
	
	public Selection clone () {
		Selection s = new Selection(xmlValue);
		
		//don't think setting these is strictly necessary, question should always be set by the time clone() is called
		//on second thought, this might not be such a safe assumption
		s.qID = qID;
		s.question = question;
		
		return s;
	}
	
	public void setQuestionDef(QuestionDef q) {
		this.qID = q.getID();
		this.question = q;
		index =  q.getSelectedItemIndex(xmlValue); 
	}
	
	public String getText () {
		if (question != null) {
			return (String)question.getSelectItems().keyAt(index);
		} else {
			System.err.println("Warning!! Calling Selection.getText() when QuestionDef not set!");
			return "[cannot access choice caption]";
		}
	}
	
	public String getValue () {
		if (question != null) {
			return (String)question.getSelectItems().elementAt(index);
		} else {
			return xmlValue;
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		index = ExtUtil.readInt(in);
		
		qID = ExtUtil.readInt(in);
		xmlValue = ExtUtil.readString(in);
	}
 
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, index);
		
		ExtUtil.writeNumeric(out, question != null ? question.getID() : qID);
		ExtUtil.writeString(out, getValue());
	}
}
