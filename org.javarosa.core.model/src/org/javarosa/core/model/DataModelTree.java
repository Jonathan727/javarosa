package org.javarosa.core.model;

import java.io.DataOutputStream;
import java.util.Enumeration;

import org.javarosa.core.model.utils.IDataModelVisitor;
import org.javarosa.core.model.utils.ITreeVisitor;

/**
 * DataModelTree is an implementation of IFormDataModel
 * that contains a Data Model which stores Question Answers
 * in an XML-style hierarchical tree, with no repeated
 * tree elements.
 *  
 * @author Clayton Sims
 *
 */
public class DataModelTree implements IFormDataModel {

	/** The root of this tree */
	private TreeElement root;
	
	/** The name for this data model */
	private String name;
	
	/** The integer Id of the model */
	private int id;
	
	public DataModelTree() { 
	}
	
	/**
	 * Creates a new data model using the root given.
	 * 
	 * @param root The root of the tree for this data model.
	 */
	public DataModelTree(TreeElement root) {
		this.root = root;
	}
	
	/**
	 * Sets the root element of this Model's tree
	 * @param root The root of the tree for this data model.
	 */
	public void setRootElement(TreeElement root) {
		this.root = root;
	}
	
	/**
	 * @return This model's root tree element
	 */
	public TreeElement getRootElement() {
		return root;
	}

	public DataOutputStream externalizeToXMLInstance() {
		// TODO Auto-generated method stub
		return null;
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#updateDataValue(IDataBinding, Object)
	 */
	public boolean updateDataValue(IDataReference questionBinding, QuestionData value) {
		QuestionDataElement questionElement = resolveReference(questionBinding);
		if(questionElement != null) {
			questionElement.setValue(value);
			return true;
		}
		else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormDataModel#getDataValue(org.javarosa.core.model.IDataReference)
	 */
	public QuestionData getDataValue(IDataReference questionReference) {
		QuestionDataElement element = resolveReference(questionReference);
		if(element != null) {
			return element.getValue();
		}
		else {
			return null;
		}
	}

	
	/**
	 * Resolves a binding to a particular question data element
	 * @param binding The binding representing a particular question
	 * @return A QuestionDataElement corresponding to the binding
	 * provided. Null if none exists in this tree.
	 */
	public QuestionDataElement resolveReference(IDataReference binding) {
		if (root.isLeaf()) {
			if ((root.getClass() == QuestionDataElement.class)
					&& ((QuestionDataElement) root).matchesReference(binding)) {
				return (QuestionDataElement) root;
			} else {
				return null;
			}
		} else {
			return resolveReference(binding,(QuestionDataGroup)root);
		}
	}
	
	/**
	 * Resolves a binding to a particular question data element
	 * @param binding The binding representing a particular question
	 * @param group 
	 * @return A QuestionDataElement corresponding to the binding
	 * provided. Null if none exists in this tree.
	 */
	private QuestionDataElement resolveReference(IDataReference binding, QuestionDataGroup group) {
		Enumeration en = group.getChildren().elements();
		while(en.hasMoreElements()) {
			TreeElement dme = (TreeElement)en.nextElement();
			if(!dme.isLeaf()) {
				return resolveReference(binding, (QuestionDataGroup)dme);
			} else {
				if ((root.getClass() == QuestionDataElement.class)
						&& ((QuestionDataElement) dme).matchesReference(binding)) {
					return (QuestionDataElement) dme;
				}
			}
		}
		return null;
	}
	
	/**
	 * Identifies whether the tree for this DataModel contains the given element.
	 * 
	 * @param element The element to be identified
	 * @return True if this model's tree contains the given element. False otherwise.
	 */
	public boolean contains(TreeElement element) {
		return root.contains(element);
	}
	
	public void accept(IDataModelVisitor visitor) {
		visitor.visit(this);
		if(root != null) {
			if(visitor.getClass() == ITreeVisitor.class) {
				root.accept((ITreeVisitor)visitor);
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
