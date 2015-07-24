package org.javarosa.xform.parse;

import org.kxml2.kdom.Document;

import java.io.Reader;

/**
 * Interface for class factory for creating an XFormParser.
 * Supports experimental extensions of XFormParser.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public interface IXFormParserFactory {
	XFormParser getXFormParser(Reader reader);
	
	XFormParser getXFormParser(Document doc);
	
	XFormParser getXFormParser(Reader form, Reader instance);
	
	XFormParser getXFormParser(Document form, Document instance);

}
