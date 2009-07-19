package org.eclipse.wst.xsl.internal.model.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.xsl.core.XSLCore;
import org.eclipse.wst.xsl.core.internal.StylesheetBuilder;
import org.eclipse.wst.xsl.core.model.StylesheetModel;
import org.eclipse.wst.xsl.core.model.Template;
import org.eclipse.wst.xsl.core.model.XSLAttribute;

public class TestStylesheetModel extends AbstractModelTest {
    private StylesheetModel model = null;
	public TestStylesheetModel() {
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		StylesheetBuilder builder = StylesheetBuilder.getInstance();
		builder.release();
		model = null;
	}
	
	public void testStyleSheetModel() {
		
		model = XSLCore.getInstance().getStylesheet(getFile("globalVariablesTest.xsl"));
		assertNotNull("Failed to load stylesheet 'globalVariablesTest.xsl'.", model);
		model = XSLCore.getInstance().getStylesheet(getFile("style1.xsl"));
		assertNotNull("Failed to load stylesheet 'style1.xsl'.", model);
		model = XSLCore.getInstance().getStylesheet(getFile("XSLT20Test.xsl"));
		assertNotNull("Failed to load stylesheet 'XSLT20Test.xsl'.", model);
		model = XSLCore.getInstance().getStylesheet(getFile("circularref.xsl"));
		assertNotNull("Failed to load stylesheet 'circularref.xsl'.", model);
		model = XSLCore.getInstance().getStylesheet(getFile("modeTest.xsl"));
		assertNotNull("Failed to load stylesheet 'modeTest.xsl'.", model);
		
	}
	
	public void testFindAvailableTemplateModes() {
		ArrayList<String> modes = new ArrayList();
		model = XSLCore.getInstance().getStylesheet(getFile("modeTest.xsl"));
		List<Template> templates = model.getTemplates();
		assertTrue("No templates returned.", templates.size() > 0);
		
		for (Template template : templates) {
			XSLAttribute attribute = template.getAttribute("mode");
			if (attribute != null) {
				if (modes.indexOf(attribute.getValue()) == -1 ) {
					modes.add(attribute.getValue());
				}
			}
		}
		assertEquals("Wrong number of mode templates returned.", 3, modes.size());
	}
		
	public void testCircularReference() {
		model = XSLCore.getInstance().getStylesheet(getFile("circularref.xsl"));
		assertTrue("Undettected circular reference", model.hasCircularReference());
	}
	
	public void testNoCircularReference() {
		model = XSLCore.getInstance().getStylesheet(getFile("modeTest.xsl"));
		assertFalse("Undettected circular reference", model.hasCircularReference());
	}	

}
