/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.validation.tests.internal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * Test class for the XML validator to test the XMLProject.
 * 
 * @author Lawrence Mandel, IBM
 */
public class XMLExamplesTest extends BaseTestCase
{ 
  private static final String XMLExamples_DIR = "XMLExamples/";
  /**
   * Create a tests suite from this test class.
   * 
   * @return A test suite containing this test class.
   */
  public static Test suite()
  {
    return new TestSuite(XMLExamplesTest.class);
  }
  
  /**
   * Test /XMLExamples/Invoice/Invoice.xml.
   */
  public void testInvoice()
  {
  	String testname = "Invoice";
    String testfile = FILE_PROTOCOL + PLUGIN_ABSOLUTE_PATH + SAMPLES_DIR + XMLExamples_DIR + "Invoice/" + testname + ".xml";
	List keys = new ArrayList();
	int numErrors = 0;
	int numWarnings = 0;

	runTest(testfile, keys, numErrors, numWarnings);
  }
  
  /**
   * Test /XMLExamples/InvoiceInvalid/Invoice.xml.
   */
  public void testInvoiceInvalid()
  {
  	String testname = "Invoice";
    String testfile = FILE_PROTOCOL + PLUGIN_ABSOLUTE_PATH + SAMPLES_DIR + XMLExamples_DIR + "InvoiceInvalid/" + testname + ".xml";
	List keys = new ArrayList();
	keys.add("FILE_NOT_FOUND");
	int numErrors = 1;
	int numWarnings = 0;
    
	runTest(testfile, keys, numErrors, numWarnings);
  }
  
  /**
   * Test /XMLExamples/PublicationCatalogue/Catalogue.xml.
   */
  public void testCatalogue()
  {
  	String testname = "Catalogue";
    String testfile = FILE_PROTOCOL + PLUGIN_ABSOLUTE_PATH + SAMPLES_DIR + XMLExamples_DIR + "PublicationCatalogue/" + testname + ".xml";
	List keys = new ArrayList();
	int numErrors = 0;
	int numWarnings = 0;

	runTest(testfile, keys, numErrors, numWarnings);
  }
  
  /**
   * Test /XMLExamples/PublicationCatalogueInvalid/Catalogue.xml.
   */
  public void testCatalogueInvalid()
  {
  	String testname = "CatalogueInvalid";
    String testfile = FILE_PROTOCOL + PLUGIN_ABSOLUTE_PATH + SAMPLES_DIR + XMLExamples_DIR + "PublicationCatalogueInvalid/" + testname + ".xml";
	List keys = new ArrayList();
	keys.add("FILE_NOT_FOUND");
	int numErrors = 1;
	int numWarnings = 0;
    
	runTest(testfile, keys, numErrors, numWarnings);
  }
  
  /**
   * Test /XMLExamples/j2ee/web.xml.
   */
  public void testWeb()
  {
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=103614  
	assertTrue("this testWeb test removed since blocking rest of tests", false);
	  
	  
  	String testname = "web";
    String testfile = FILE_PROTOCOL + PLUGIN_ABSOLUTE_PATH + SAMPLES_DIR + XMLExamples_DIR + "j2ee/" + testname + ".xml";
	List keys = new ArrayList();
	int numErrors = 0;
	int numWarnings = 0;

	runTest(testfile, keys, numErrors, numWarnings);
  }
}
