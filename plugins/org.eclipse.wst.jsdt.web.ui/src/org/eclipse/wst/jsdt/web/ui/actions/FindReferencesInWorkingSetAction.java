/**
 * 
 */
package org.eclipse.wst.jsdt.web.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.wst.jsdt.core.IJavaElement;

/**
 * @author childsb
 * 
 */
public class FindReferencesInWorkingSetAction extends FindReferencesAction {
	
	public void run(IAction action) {
		try {
			Object handler = getActionHandler(action);
			IJavaElement elements[] = JsElementActionProxy.getJsElementsFromSelection(getCurrentSelection());
			if (elements == null || elements.length == 0) {
				return;
			}
			SimpleJSDTActionProxy.executeMethod(handler, "run", new Class[] { IJavaElement.class }, new Object[] { elements[0] });
		} catch (IllegalArgumentException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (SecurityException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			// If user doesn't select a working set an exception is thrown so we
			// wont print stack trace here
		}
	}
}
