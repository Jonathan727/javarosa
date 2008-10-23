package org.javarosa.media.image.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Answer data representing a pointer object.  The pointer is a reference to some 
 * other object that it knows how to get out of memory.
 * 
 * @author Cory Zue
 *
 */
public class PointerAnswerData implements IAnswerData {

	private IDataPointer data;
	
	public String getDisplayText() {
		return data.getDisplayText();
	}

	public Object getValue() {
		return data;
	}

	public void setValue(Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		data = ((IDataPointer)o);
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		// TODO Auto-generated method stub
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}

}