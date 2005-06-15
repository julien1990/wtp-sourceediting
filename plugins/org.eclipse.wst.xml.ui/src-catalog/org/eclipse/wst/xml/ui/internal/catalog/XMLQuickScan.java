/*
* Copyright (c) 2002 IBM Corporation and others.
* All rights reserved.   This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*   IBM - Initial API and implementation
*   Jens Lukowski/Innoopract - initial renaming/restructuring
* 
*/
package org.eclipse.wst.xml.ui.internal.catalog;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public class XMLQuickScan
{
  public static final String copyright = "(c) Copyright IBM Corporation 2002.";
  public static String getTargetNamespaceURIForSchema(String uri)
  {
    String result = null;
    try
    {
      URL url = new URL(uri);
      InputStream inputStream = url.openStream();
      result = XMLQuickScan.getTargetNamespaceURIForSchema(inputStream);
    }
    catch (Exception e)
    {
    }
    return result;
  }

  public static String getTargetNamespaceURIForSchema(InputStream input)
  {
    TargetNamespaceURIContentHandler handler = new TargetNamespaceURIContentHandler();
    ClassLoader prevClassLoader = Thread.currentThread().getContextClassLoader();
    try
    {
      Thread.currentThread().setContextClassLoader(XMLQuickScan.class.getClassLoader());
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
	  SAXParser parser = factory.newSAXParser();
	  parser.parse(new InputSource(input), handler);
    }
    catch (Exception e)
    {
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(prevClassLoader);
    }
    return handler.targetNamespaceURI;
  }

  protected static class TargetNamespaceURIContentHandler extends DefaultHandler
  {
    public String targetNamespaceURI;

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      if (localName.equals("schema"))
      {
        int nAttributes = attributes.getLength();
        for (int i = 0; i < nAttributes; i++)
        {
          if (attributes.getLocalName(i).equals("targetNamespace"))
          {
            targetNamespaceURI = attributes.getValue(i);
            break;
          }
        }
      }
      // todo there's a nice way to do this I'm sure    
      // here I intentially cause an exception... 
      String x = null;
      x.length();
    }
  }
}
