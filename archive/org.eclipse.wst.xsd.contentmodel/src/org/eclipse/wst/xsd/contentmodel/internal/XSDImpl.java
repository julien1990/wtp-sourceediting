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
package org.eclipse.wst.xsd.contentmodel.internal;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
import org.eclipse.wst.common.contentmodel.CMAnyElement;
import org.eclipse.wst.common.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.common.contentmodel.CMContent;
import org.eclipse.wst.common.contentmodel.CMDataType;
import org.eclipse.wst.common.contentmodel.CMDocument;
import org.eclipse.wst.common.contentmodel.CMDocumentation;
import org.eclipse.wst.common.contentmodel.CMElementDeclaration;
import org.eclipse.wst.common.contentmodel.CMGroup;
import org.eclipse.wst.common.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.common.contentmodel.CMNamespace;
import org.eclipse.wst.common.contentmodel.CMNode;
import org.eclipse.wst.common.contentmodel.CMNodeList;
import org.eclipse.wst.common.contentmodel.annotation.AnnotationMap;
import org.eclipse.wst.common.contentmodel.basic.CMAttributeDeclarationImpl;
import org.eclipse.wst.common.contentmodel.basic.CMDataTypeImpl;
import org.eclipse.wst.common.contentmodel.basic.CMDocumentImpl;
import org.eclipse.wst.common.contentmodel.basic.CMEntityDeclarationImpl;
import org.eclipse.wst.common.contentmodel.basic.CMGroupImpl;
import org.eclipse.wst.common.contentmodel.basic.CMNamedNodeMapImpl;
import org.eclipse.wst.common.contentmodel.basic.CMNodeListImpl;
import org.eclipse.wst.common.contentmodel.util.CMDescriptionBuilder;
import org.eclipse.wst.common.contentmodel.util.NamespaceInfo;
import org.eclipse.wst.common.uriresolver.URIResolverPlugin;
import org.eclipse.wst.xsd.contentmodel.internal.util.XSDSchemaLocatorAdapterFactory;
import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDAttributeUseCategory;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDCompositor;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDConstraint;
import org.eclipse.xsd.XSDContentTypeCategory;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDForm;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDModelGroupDefinition;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDParticleContent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.XSDWildcard;
import org.eclipse.xsd.impl.XSDSchemaImpl;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSwitch;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility class to build cmnodes from XML Schema nodes. The XML Schema model is
 * found in the org.eclipse.xsd plugin.
 * 
 * TODO: getNamespaceURI()currently always returns '##any'.
 */
public class XSDImpl
{
  /*
   * properties common to all cmnodes the following properties defined in
   * CMNodeImpl class: PROPERTY_DOCUMENTATION PROPERTY_DOCUMENTATION_SOURCE
   * PROPERTY_DOCUMENTATION_LANGUAGE PROPERTY_MOF_NOTIFIER
   * PROPERTY_DEFINITION_INFO PROPERTY_DEFINITION
   * 
   * the following properties defined in this class, XSDImpl:
   * PROPERTY_CMDOCUMENT PROPERTY_USES_LOCAL_ELEMENT_DECLARATIONS
   * PROPERTY_IS_NAME_SPACE_AWARE PROPERTY_NS_PREFIX_QUALIFICATION
   * PROPERTY_NILLABLE PROPERTY_SPEC
   */
  public static final String PROPERTY_CMDOCUMENT = "CMDocument";
  public static final String PROPERTY_USES_LOCAL_ELEMENT_DECLARATIONS = "http://com.ibm.etools/cm/properties/usesLocalElementDeclarations";
  public static final String PROPERTY_IS_NAME_SPACE_AWARE = "http://com.ibm.etools/cm/properties/isNameSpaceAware";
  public static final String PROPERTY_NS_PREFIX_QUALIFICATION = "http://com.ibm.etools/cm/properties/nsPrefixQualification";
  public static final String PROPERTY_NILLABLE = "http://com.ibm.etools/cm/properties/nillable";
  public static final String PROPERTY_SPEC = "spec";
  /*
   * properties common to all CMDocument nodes: PROPERTY_TARGET_NAMESPACE_URI
   * PROPERTY_IMPORTED_NAMESPACE_INFO PROPERTY_NAMESPACE_INFO
   * PROPERTY_ELEMENT_FORM_DEFAULT PROPERTY_ANNOTATION_MAP
   * PROPERTY_ENCODING_INFO
   */
  public static final String PROPERTY_TARGET_NAMESPACE_URI = "http://com.ibm.etools/cm/properties/targetNamespaceURI";
  public static final String PROPERTY_IMPORTED_NAMESPACE_INFO = "http://com.ibm.etools/cm/properties/importedNamespaceInfo";
  public static final String PROPERTY_NAMESPACE_INFO = "http://com.ibm.etools/cm/properties/namespaceInfo";
  public static final String PROPERTY_ELEMENT_FORM_DEFAULT = "http://com.ibm.etools/cm/properties/elementFormDefault";
  public static final String PROPERTY_ANNOTATION_MAP = "annotationMap";
  public static final String PROPERTY_ENCODING_INFO = "encodingInfo";
  /*
   * properties common to all CMElementDeclaration nodes: PROPERTY_XSITYPES
   * PROPERTY_DERIVED_ELEMENT_DECLARATION PROPERTY_SUBSTITUTION_GROUP
   * PROPERTY_ABSTRACT
   */
  public static final String PROPERTY_XSITYPES = "XSITypes";
  public static final String PROPERTY_DERIVED_ELEMENT_DECLARATION = "DerivedElementDeclaration";
  public static final String PROPERTY_SUBSTITUTION_GROUP = "SubstitutionGroup";
  public static final String PROPERTY_ABSTRACT = "Abstract";
  /**
   * Definition info for element declarations.
   */
  public static final String DEFINITION_INFO_GLOBAL = "global";
  public static final String DEFINITION_INFO_LOCAL = "local";
  public static final String XML_LANG_ATTRIBUTE = "xml:lang";
  public static final String PLATFORM_PROTOCOL = "platform:";
  protected static XSDAdapterFactoryImpl xsdAdapterFactoryImpl = new XSDAdapterFactoryImpl();
  protected static XSIDocument xsiDocument = new XSIDocument();

  /**
   * Given uri for an XML Schema document, parse the document and build
   * corresponding CMDocument node.
   * 
   * @param uri -
   *          the uri for an XML Schema document
   * @param grammarErrorChecking -
   *          grammar error checking flag
   * @param errorList -
   *          the resulting error list
   * @return the corresponding CMDocument node.
   * @deprecated -- use buildCMDocument(String uri)
   */
  public static CMDocument buildCMDocument(String uri, int grammarErrorChecking, List errorList)
  {
    return buildCMDocument(uri);
  }

  /**
   * Given uri for an XML Schema document, parse the document and build
   * corresponding CMDocument node.
   * 
   * @param uri -
   *          the uri for an XML Schema document
   * @return the corresponding CMDocument node.
   */
  public static CMDocument buildCMDocument(String uri)
  {
    CMDocument cmDocument = null;
    XSDSchema xsdSchema = buildXSDModel(uri);
    if (xsdSchema != null)
    {
      cmDocument = (CMDocument) getAdapter(xsdSchema);
      CMDocumentEncodingHelper.setEncodingInfo(cmDocument, uri);
    }
    return cmDocument;
  }

  /**
   * Given uri for an XML Schema document, parse the document and build
   * corresponding CMDocument node.
   * 
   * @param uri -
   *          the uri for an XML Schema document
   * @return the corresponding CMDocument node.
   */
  public static XSDSchema buildXSDModel(String uriString)
  {
    XSDSchema xsdSchema = null;
 
    try
    {
      // if XML Schema for Schema is requested, get it through schema model 
      if (uriString.endsWith("w3c/XMLSchema.xsd"))
      {
      	xsdSchema = XSDSchemaImpl.getSchemaForSchema(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);			
      }
      else
      { 	
      ResourceSet resourceSet = new ResourceSetImpl();
      // CS : bugzilla bug 1972 ... this line of code attaches a customized
      // XSDSchemaLocator that utilizes the URIResolver so that it's catalog
      // aware.
      //
      resourceSet.getAdapterFactories().add(new XSDSchemaLocatorAdapterFactory());
      // CS : a temp workaround for bug 1972 until the XSD model fixes 69081
      //      
      //resourceSet.setURIConverter(new InternalURIConverter());
      resourceSet.getLoadOptions().put(XSDResourceImpl.XSD_TRACK_LOCATION, Boolean.TRUE);
      URI uri = createURI(uriString);
      XSDResourceImpl resource = (XSDResourceImpl) resourceSet.getResource(uri, true);
      xsdSchema = resource.getSchema();
      if (xsdSchema != null)
      {
        String schemaLocation = xsdSchema.getSchemaLocation();
        if (schemaLocation == null)
        {
          xsdSchema.setSchemaLocation(uriString);
        }
      }
      }
    }
    catch (Exception e)
    {
    }
    return xsdSchema;
  }
  
  // TODO ... looks like we can remove this class?
  //
  static class InternalURIConverter extends URIConverterImpl
  {
    protected InputStream createURLInputStream(URI uri) throws IOException
    {
      if ("http".equals(uri.scheme()))
      {
        String theURI = uri.toString();
        String mapped = URIResolverPlugin.createResolver().resolve(theURI, theURI, theURI);
        if (mapped != null)
        {
          uri = createURI(mapped);
        }
      }
      return super.createURLInputStream(uri);
    }
  }

  /**
   * Returns an appropriate URI based on a uri string.
   * 
   * @param uriString -
   *          a uri string.
   * @return an appropriate URI based on a uri string.
   */
  public static URI createURI(String uriString)
  {
    if (hasProtocol(uriString))
      return URI.createURI(uriString);
    else
      return URI.createFileURI(uriString);
  }

  private static boolean hasProtocol(String uri)
  {
    boolean result = false;
    if (uri != null)
    {
      int index = uri.indexOf(":");
      if (index != -1 && index > 2) // assume protocol with be length 3 so that
                                    // the'C' in 'C:/' is not interpreted as a
                                    // protocol
      {
        result = true;
      }
    }
    return result;
  }

  /**
   * Returns true if string begins with platform protocol.
   * 
   * @param uriString -
   *          a uri string.
   * @return true if string begins with platform protocol.
   */
  public static boolean withPlatformProtocol(String uriString)
  {
    return uriString.startsWith(PLATFORM_PROTOCOL);
  }

  /**
   * Returns the value of the 'Min Occurs' attribute. The default value is "1".
   * 
   * @param component -
   *          a concrete component.
   * @return the value of the 'Min Occurs' attribute.
   */
  public static int getMinOccurs(XSDConcreteComponent component)
  {
    int minOccur = 1;
    if (component != null)
    {
      Object o = component.getContainer();
      if (o instanceof XSDParticle)
      {
        if (((XSDParticle) o).isSetMinOccurs())
        {
          try
          {
            minOccur = ((XSDParticle) o).getMinOccurs();
          }
          catch (Exception e)
          {
            minOccur = 1;
          }
        }
      }
    }
    return minOccur;
  }

  /**
   * Returns the value of the 'Max Occurs' attribute. The default value is "1".
   * 
   * @param component -
   *          a concrete component.
   * @return the value of the 'Max Occurs' attribute.
   */
  public static int getMaxOccurs(XSDConcreteComponent component)
  {
    int maxOccur = 1;
    if (component != null)
    {
      Object o = component.getContainer();
      if (o instanceof XSDParticle)
      {
        if (((XSDParticle) o).isSetMaxOccurs())
        {
          try
          {
            maxOccur = ((XSDParticle) o).getMaxOccurs();
          }
          catch (Exception e)
          {
            maxOccur = 1;
          }
        }
      }
    }
    return maxOccur;
  }

  /**
   * Returns the enumerated values for the given type.
   * 
   * @param type -
   *          a type definition.
   * @return the enumerated values for the given type.
   */
  public static String[] getEnumeratedValuesForType(XSDTypeDefinition type)
  {
    Vector result = new Vector();
    if (type instanceof XSDSimpleTypeDefinition)
    {
      List enumerationFacets = ((XSDSimpleTypeDefinition) type).getEnumerationFacets();
      for (Iterator i = enumerationFacets.iterator(); i.hasNext();)
      {
        XSDEnumerationFacet enum = (XSDEnumerationFacet) i.next();
        List values = enum.getValue();
        for (Iterator j = values.iterator(); j.hasNext();)
        {
          result.add(j.next());
        }
      }
    }
    return (String[]) result.toArray(new String[result.size()]);
  }

  /**
   * Return a list of documentation elements from the given annotation. Working
   * with documentation elements requires dropping down into the DOM model.
   * 
   * @param annotation -
   *          an XSDAnnotation node.
   * @return a list of documentation elements.
   */
  public static CMNodeList getDocumentations(XSDAnnotation annotation)
  {
    CMNodeListImpl documentations = new CMNodeListImpl();
    if (annotation != null)
    {
      List documentationsElements = annotation.getUserInformation();
      for (Iterator i = documentationsElements.iterator(); i.hasNext();)
      {
        documentations.getList().add(new DocumentationImpl((Element) i.next()));
      }
    }
    return documentations;
  }

  /**
   * Adapted from public static List findTypesDerivedFrom(XSDSchema schema,
   * String namespace, String localName) in class XSDSchemaQueryTools found in
   * org.eclipse.xsd plugin.
   * 
   * Find typeDefinitions that derive from a given type.
   * 
   * @param type
   *          the type derived from
   * @return List of any XSDTypeDefinitions found
   */
  public static List findTypesDerivedFrom(XSDTypeDefinition type)
  {
    ArrayList typesDerivedFrom = new ArrayList();
    if (type != null)
    {
      XSDSchema schema = type.getSchema();
      String localName = type.getName();
      if ((null != schema) && (null != localName))
      {
        String namespace = schema.getTargetNamespace();
        // A handy convenience method quickly gets all
        // typeDefinitions within our schema; note that
        // whether or not this returns types in included,
        // imported, or redefined schemas is subject to change
        List typedefs = schema.getTypeDefinitions();
        for (Iterator iter = typedefs.iterator(); iter.hasNext();)
        {
          XSDTypeDefinition typedef = (XSDTypeDefinition) iter.next();
          if (typedef instanceof XSDComplexTypeDefinition)
          {
            // Walk the baseTypes from this typedef seeing if any
            // of them match the requested one
            if (isTypeDerivedFrom(typedef, namespace, localName))
            {
              // We found it, return the original one and continue
              typesDerivedFrom.add(typedef);
              continue;
            }
          }
        }
      }
    }
    return typesDerivedFrom;
  }

  /**
   * Adapted from protected static boolean isTypeDerivedFrom(XSDTypeDefinition
   * typedef, String namespace, String localName) in class XSDSchemaQueryTools
   * found in org.eclipse.xsd plugin.
   * 
   * Recursive worker method to find typeDefinitions that derive from a named
   * type.
   * 
   * @see #findTypesDerivedFrom(XSDSchema, String, String)
   * @param typeDef
   *          to see if it's derived from
   * @param namespace
   *          for the type derived from
   * @param localName
   *          for the type derived from
   * @return true if it is; false otherwise
   */
  protected static boolean isTypeDerivedFrom(XSDTypeDefinition typedef, String namespace, String localName)
  {
    // Walk the baseTypes from this typedef seeing if any
    // of them match the requested one
    XSDTypeDefinition baseType = typedef.getBaseType();
    // As this convenience method if our parameters match
    if (baseType.hasNameAndTargetNamespace(localName, namespace))
    {
      return true;
    }
    XSDTypeDefinition rootType = typedef.getRootType();
    if (rootType == baseType)
    {
      // If we've hit the root, we aren't derived from it
      return false;
    }
    else
    {
      // Otherwise continue to traverse upwards
      return isTypeDerivedFrom(baseType, namespace, localName);
    }
  }

  /**
   * Returns the corresponding cmnode of the specified XML Schema node.
   * 
   * @param target -
   *          an XML Schema node
   * @return the corresponding cmnode.
   */
  public static CMNode getAdapter(Notifier o)
  {
    return (CMNode) xsdAdapterFactoryImpl.adapt(o);
  }

  /**
   * Adapted from public String getPrefix(String ns, boolean withColon) in class
   * TypesHelper found in com.ibm.etools.xsdeditor plugin.
   * 
   * @param schema -
   *          the relevant schema
   * @param ns -
   *          the relevant namespace
   */
  public static String getPrefix(XSDSchema schema, String ns)
  {
    String key = "";
    if ((schema != null) && (ns != null))
    {
      Map map = schema.getQNamePrefixToNamespaceMap();
      Iterator iter = map.keySet().iterator();
      while (iter.hasNext())
      {
        Object keyObj = iter.next();
        Object value = map.get(keyObj);
        if (value != null && value.toString().equals(ns))
        {
          if (keyObj != null)
          {
            key = keyObj.toString();
          }
          else
          {
            key = "";
          }
          break;
        }
      }
    }
    return key;
  }
  /**
   * The Factory for the XSD adapter model. It provides a create method for each
   * non-abstract class of the model.
   */
  public static class XSDAdapterFactoryImpl extends AdapterFactoryImpl
  {
    public Adapter createAdapter(Notifier target)
    {
      XSDSwitch xsdSwitch = new XSDSwitch()
      {
        public Object caseXSDWildcard(XSDWildcard object)
        {
          return new XSDWildcardAdapter(object);
        }

        public Object caseXSDModelGroupDefinition(XSDModelGroupDefinition object)
        {
          return new XSDModelGroupDefinitionAdapter(object);
        }

        public Object caseXSDAttributeUse(XSDAttributeUse object)
        {
          return new XSDAttributeUseAdapter(object);
        }

        public Object caseXSDElementDeclaration(XSDElementDeclaration object)
        {
          return new XSDElementDeclarationAdapter(object);
        }

        public Object caseXSDModelGroup(XSDModelGroup object)
        {
          return new XSDModelGroupAdapter(object);
        }

        public Object caseXSDSchema(XSDSchema object)
        {
          return new XSDSchemaAdapter(object);
        }
      };
      Object o = xsdSwitch.doSwitch((EObject) target);
      Adapter result = null;
      if (o instanceof Adapter)
      {
        result = (Adapter) o;
      }
      else
      {
        Thread.dumpStack();
      }
      return result;
    }

    public Adapter adapt(Notifier target)
    {
      return adapt(target, this);
    }
  }
  /**
   * XSDBaseAdapter -- an abstract base node in the model. All other model nodes
   * are derived from it.
   */
  public static abstract class XSDBaseAdapter extends CMNodeImpl
  {
    protected CMNodeListImpl documentation = new CMNodeListImpl();

    /**
     * Returns the name of the node. The default value is an empty string value.
     * All derived classes must override this method if they do not want the
     * default value.
     * 
     * @return the name of the node.
     */
    public String getNodeName()
    {
      return "";
    }

    /**
     * Returns true of the given factory is the factory for this XSD adapter
     * model.
     * 
     * @param type -
     *          a factory
     * @return true if the type is the adapter factory for this model.
     */
    public boolean isAdapterForType(Object type)
    {
      return type == xsdAdapterFactoryImpl;
    }

    /**
     * Returns true if the property is supported for this class.
     * 
     * @param propertyName -
     *          name of a property
     * @return true if the property is supported.
     */
    public boolean supports(String propertyName)
    {
      return propertyName.equals(PROPERTY_NS_PREFIX_QUALIFICATION) || propertyName.equals(PROPERTY_NILLABLE) || propertyName.equals(PROPERTY_USES_LOCAL_ELEMENT_DECLARATIONS)
          || propertyName.equals(PROPERTY_DOCUMENTATION) || propertyName.equals(PROPERTY_DOCUMENTATION_SOURCE) || propertyName.equals(PROPERTY_DOCUMENTATION_LANGUAGE)
          || propertyName.equals(PROPERTY_MOF_NOTIFIER) || propertyName.equals(PROPERTY_DEFINITION_INFO) || propertyName.equals(PROPERTY_DEFINITION) || propertyName.equals(PROPERTY_CMDOCUMENT)
          || propertyName.equals(PROPERTY_IS_NAME_SPACE_AWARE) || propertyName.equals(PROPERTY_SPEC) || super.supports(propertyName);
    }

    /**
     * Returns the value of the 'Nillable' attribute. This represents the
     * nillable infoset property. The default value is false. All derived
     * classes must override this method if they do not want the default value.
     * 
     * @return the value of the 'Nillable' attribute.
     */
    public boolean isNillable()
    {
      return false;
    }

    /**
     * Returns the cmdocument that is the owner of this cmnode. The default
     * value is null; All derived classes must override this method if they do
     * not want the default value.
     * 
     * @return the cmdocument corresponding to this cmnode.
     */
    public CMDocument getCMDocument()
    {
      return null;
    }

    /**
     * Return a list of documentation elements. The default value is an empty
     * CMNodeList; All derived classes must override this method if they do not
     * want the default value.
     * 
     * @return a list of documentation elements.
     */
    protected CMNodeList getDocumentation()
    {
      return documentation;
    }

    /**
     * Returns the property value for the property name. Returns null if the
     * property is not supported.
     * 
     * @param propertyName -
     *          name of a property
     * @return the property value for the property name.
     */
    public Object getProperty(String propertyName)
    {
      Object result = null;
      if (propertyName.equals(PROPERTY_CMDOCUMENT))
      {
        result = getCMDocument();
      }
      else if (propertyName.equals(PROPERTY_DOCUMENTATION))
      {
        result = getDocumentation();
      }
      else if (propertyName.equals(PROPERTY_USES_LOCAL_ELEMENT_DECLARATIONS))
      {
        result = Boolean.TRUE;
      }
      else if (propertyName.equals(PROPERTY_IS_NAME_SPACE_AWARE))
      {
        result = Boolean.TRUE;
      }
      else if (propertyName.equals(PROPERTY_NS_PREFIX_QUALIFICATION))
      {
        result = getNSPrefixQualification();
      }
      else if (propertyName.equals(PROPERTY_NILLABLE))
      {
        result = isNillable() ? xsiDocument.nilAttribute : null;
      }
      else if (propertyName.equals(PROPERTY_MOF_NOTIFIER))
      {
        result = getKey();
      }
      else if (propertyName.equals(PROPERTY_SPEC))
      {
        result = getSpec();
      }
      else
      {
        result = super.getProperty(propertyName);
        {
          CMDocument cmDocument = getCMDocument();
          if (cmDocument instanceof XSDSchemaAdapter)
          {
            AnnotationMap map = ((XSDSchemaAdapter) cmDocument).annotationMap;
            if (map != null)
            {
              String spec = getSpec();
              if (spec != null)
              {
                result = map.getProperty(getSpec(), propertyName);
              }
            }
          }
        }
      }
      return result;
    }
       


    /*
     * Returns the value of the form [attribute] which affects the target
     * namespace of locally scoped features. The default value is null. All
     * derived classes must override this method if they do not want the default
     * value. @return the value of the form [attribute].
     */
    public Object getNSPrefixQualification()
    {
      return null;
    }

    /**
     * Returns a general XPath expression for the node.
     * 
     * @return a general XPath expression for the node.
     */
    public String getSpec()
    {
      return "//" + getNodeName();
    }
  }
  /**
   * XSDSchemaAdapter implements CMDocument. A representation of the model
   * object 'Schema'.
   */
  public static class XSDSchemaAdapter extends XSDBaseAdapter implements CMDocument
  {
    protected XSDSchema xsdSchema;
    protected CMNamedNodeMapImpl namedNodeMap;
    protected CMNamedNodeMapImpl entityNodeMap;
    protected String[] encodingInfo = new String[2];
    protected AnnotationMap annotationMap = new AnnotationMap();
    protected Hashtable substitutionGroupTable;

    /**
     * Constructor.
     * 
     * @param xsdSchema -
     *          the schema node.
     */
    public XSDSchemaAdapter(XSDSchema xsdSchema)
    {
      this.xsdSchema = xsdSchema;
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public Object getKey()
    {
      return xsdSchema;
    }

    /**
     * Returns the filename.
     * 
     * @return the filename.
     */
    public String getNodeName()
    {
      // See buildCMDocument() above.
      return xsdSchema.getSchemaLocation();
    }

    /**
     * Returns true if the property is supported for this class.
     * 
     * @param propertyName -
     *          name of a property
     * @return true if the property is supported.
     */
    public boolean supports(String propertyName)
    {
      return propertyName.equals(PROPERTY_TARGET_NAMESPACE_URI) || propertyName.equals(PROPERTY_IMPORTED_NAMESPACE_INFO) || propertyName.equals(PROPERTY_NAMESPACE_INFO)
          || propertyName.equals(PROPERTY_ELEMENT_FORM_DEFAULT) || propertyName.equals(PROPERTY_ANNOTATION_MAP) || propertyName.equals(PROPERTY_ENCODING_INFO) || super.supports(propertyName);
    }

    /**
     * Returns true if a prefix is globally required for elements.
     * 
     * @param xsdSchema -
     *          the corresponding schema node.
     * @return true if a prefix is globally required for elements.
     */
    protected boolean isPrefixRequired(XSDSchema xsdSchema)
    {
      boolean result = true;
      if (xsdSchema.isSetElementFormDefault())
        result = !(xsdSchema.getElementFormDefault().getValue() == XSDForm.QUALIFIED);
      return result;
    }

    /**
     * Returns the property value for the property name. Returns null if the
     * property is not supported.
     * 
     * @param propertyName -
     *          name of a property
     * @return the property value for the property name.
     */
    public Object getProperty(String propertyName)
    {
      Object result = null;
      if (propertyName.equals(PROPERTY_TARGET_NAMESPACE_URI))
      {
        result = xsdSchema.getTargetNamespace();
      }
      else if (propertyName.equals(PROPERTY_IMPORTED_NAMESPACE_INFO))
      {
        List list = new Vector();
        getImportedNamespaceInfo(xsdSchema, list);
        result = list;
      }
      else if (propertyName.equals(PROPERTY_NAMESPACE_INFO))
      {
        List list = new Vector();
        NamespaceInfo info = new NamespaceInfo();
        info.uri = xsdSchema.getTargetNamespace();
        info.prefix = getPrefix(xsdSchema, info.uri);
        info.locationHint = null; // note that this locationHint info is null
                                  // for the root xsd file
        info.isPrefixRequired = isPrefixRequired(xsdSchema);
        list.add(info);
        getImportedNamespaceInfo(xsdSchema, list);
        result = list;
      }
      else if (propertyName.equals(PROPERTY_ELEMENT_FORM_DEFAULT))
      {
        result = xsdSchema.getElementFormDefault().getName();
      }
      else if (propertyName.equals(PROPERTY_ANNOTATION_MAP))
      {
        result = annotationMap;
      }
      else if (propertyName.equals(PROPERTY_ENCODING_INFO))
      {
        result = encodingInfo;
      }
      else if (propertyName.equals("allElements"))
      {
        result = getAllElements();
      }  
      else if (propertyName.startsWith("getElementForType#"))
      {
        int index = propertyName.indexOf("#");
        String typeName = propertyName.substring(index + 1, propertyName.length());
        //
        //
        XSDTypeDefinition td = xsdSchema.resolveTypeDefinition(typeName);
        if (td != null)
        {
          LocalElementVisitor localElementVisitor = new LocalElementVisitor();
          localElementVisitor.visitTypeDefinition(td);
          result = localElementVisitor.getCMNamedNodeMap();
        }
      }
      else
      {
        result = super.getProperty(propertyName);
      }
      return result;
    }

    /**
     * Gather information on namespaces used in external references.
     * 
     * @param theXSDSchema -
     *          the corresponding schema node
     * @param list -
     *          the list of imported namespaces.
     */
    public void getImportedNamespaceInfo(XSDSchema theXSDSchema, List list)
    {
      for (Iterator iterator = theXSDSchema.getContents().iterator(); iterator.hasNext();)
      {
        XSDSchemaContent content = (XSDSchemaContent) iterator.next();
        if (content instanceof XSDImport)
        {
          XSDImport xImport = (XSDImport) content;
          XSDSchema importedXSDSchema = xImport.getResolvedSchema();
          NamespaceInfo info = new NamespaceInfo();
          info.uri = xImport.getNamespace();
          info.prefix = getPrefix(importedXSDSchema, info.uri);
          info.locationHint = xImport.getSchemaLocation();
          if (importedXSDSchema != null)
          {
            info.isPrefixRequired = isPrefixRequired(importedXSDSchema);
          }
          list.add(info);
        }
      }
    }

    /**
     * Returns set of named (top-level) element declarations for this schema
     * node.
     * 
     * @return a set of named (top-level) element declarations.
     */
    public CMNamedNodeMap getElements()
    {
      if (namedNodeMap == null)
      {
        namedNodeMap = new CMNamedNodeMapImpl();
        
        // Note that if we call xsdSchema.getElementDeclarations()
        // we get 'more' elements than we really want since we also
        // get 'imported' elements.  Below we test to ensure the elements
        // actually have the same target namespace as the schema.
        String targetNamespace = xsdSchema.getTargetNamespace();
        for (Iterator i = xsdSchema.getElementDeclarations().iterator(); i.hasNext();)
        {
          XSDElementDeclaration ed = (XSDElementDeclaration) i.next();
          if (targetNamespace != null ? targetNamespace.equals(ed.getTargetNamespace()) : ed.getTargetNamespace() == null)
          {
            XSDElementDeclarationAdapter adapter = (XSDElementDeclarationAdapter) getAdapter(ed);
            namedNodeMap.getHashtable().put(adapter.getNodeName(), adapter);
          }
        }
      }
      return namedNodeMap;
    }

    /**
     * Returns the built-in entity declarations.
     * 
     * @return the built-in entity declarations.
     */
    public CMNamedNodeMap getEntities()
    {
      if (entityNodeMap == null)
      {
        entityNodeMap = new CMNamedNodeMapImpl();
        // add the built in entity declarations
        entityNodeMap.getHashtable().put("amp", new CMEntityDeclarationImpl("amp", "&"));
        entityNodeMap.getHashtable().put("lt", new CMEntityDeclarationImpl("lt", "<"));
        entityNodeMap.getHashtable().put("gt", new CMEntityDeclarationImpl("gt", ">"));
        entityNodeMap.getHashtable().put("quot", new CMEntityDeclarationImpl("quot", "\""));
        entityNodeMap.getHashtable().put("apos", new CMEntityDeclarationImpl("apos", "'"));
      }
      return entityNodeMap;
    }

    /**
     * Returns the type of the node. The types are defined in CMNode class
     * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
     * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
     * DOCUMENTATION).
     * 
     * @return the type of this node.
     */
    public int getNodeType()
    {
      return DOCUMENT;
    }

    /*
     * Returns null. !!! Why are we not implementing this???? @return null.
     */
    public CMNamespace getNamespace()
    {
      return null;
    }

    /**
     * Returns this.
     * 
     * @return this.
     */
    public CMDocument getCMDocument()
    {
      return this;
    }
    
    public CMNamedNodeMap getAllElements()
    {
      CMNamedNodeMapImpl map = new CMNamedNodeMapImpl();
      for (Iterator i = getElements().iterator(); i.hasNext(); )
      {
        CMElementDeclaration ed = (CMElementDeclaration)i.next();
        map.put(ed);           
        addLocalElementDefinitions(map, ed);              
      }     
      return map;
    }
    
    protected void addLocalElementDefinitions(CMNamedNodeMapImpl map, CMElementDeclaration parentElementDeclaration)
    {
      CMNamedNodeMap localElementMap = parentElementDeclaration.getLocalElements();
      for (Iterator i = localElementMap.iterator(); i.hasNext(); )
      {
        CMElementDeclaration ed = (CMElementDeclaration)i.next();
        if (map.getNamedItem(ed.getNodeName()) == null)
        {  
          map.put(ed);        
          addLocalElementDefinitions(map, ed);
        }  
      }               
    }
  }
  /**
   * XSDAttributeUseAdapter implements CMAttributeDeclaration. A representation
   * of the model object 'Attribute Use'.
   */
  public static class XSDAttributeUseAdapter extends XSDBaseAdapter implements CMAttributeDeclaration
  {
    // provides access to the XML Schema node
    protected XSDAttributeUse xsdAttributeUse;
    // provides access to the type of the attribute
    protected CMDataType dataType = new DataTypeImpl();

    /**
     * Constructor.
     * 
     * @param xsdAttributeUse -
     *          the XML Schema node.
     */
    public XSDAttributeUseAdapter(XSDAttributeUse xsdAttributeUse)
    {
      this.xsdAttributeUse = xsdAttributeUse;
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public Object getKey()
    {
      return xsdAttributeUse;
    }

    /**
     * Returns a general XPath expression for the node.
     * 
     * @return a general XPath expression for the node.
     */
    public String getSpec()
    {
      return "//@" + getAttrName();
    }

    /**
     * Returns the type of the node. The types are defined in CMNode class
     * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
     * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
     * DOCUMENTATION).
     * 
     * @return the type of this node.
     */
    public int getNodeType()
    {
      return ATTRIBUTE_DECLARATION;
    }

    /**
     * Returns the name of the node. Similar to getAttrName().
     * 
     * @return the name of the node.
     */
    public String getNodeName()
    {
      return getAttrName();
    }

    /**
     * getEnumAttr method
     * 
     * @return java.util.Enumeration
     * @deprecated -- to be replaced in future with additional CMDataType
     *             methods (currently found on CMDataTypeHelper)
     */
    public Enumeration getEnumAttr()
    {
      return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /**
     * Returns the name of this attribute. Similar to getNodeName().
     * 
     * @return the name of this attribute.
     */
    public String getAttrName()
    {
      return xsdAttributeUse.getContent().getName();
    }

    /**
     * Returns the type of the attribute.
     * 
     * @return the type of the attribute.
     */
    public CMDataType getAttrType()
    {
      return dataType;
    }

    /**
     * Returns the value of the default or fixed constraint.
     * 
     * @return the value of the default or fixed constraint.
     */
    public String getDefaultValue()
    {
      return dataType.getImpliedValue();
    }

    /**
     * Returns the usage constraint for this attribute. The usages are defined
     * in CMAttributeDeclaration class (OPTIONAL, REQUIRED, FIXED or
     * PROHIBITED).
     * 
     * @return the usage constraint for this attribute.
     */
    public int getUsage()
    {
      int useKind = OPTIONAL;
      switch (xsdAttributeUse.getUse().getValue())
      {
        case XSDAttributeUseCategory.OPTIONAL : {
          useKind = OPTIONAL;
          break;
        }
        case XSDAttributeUseCategory.PROHIBITED : {
          useKind = PROHIBITED;
          break;
        }
        case XSDAttributeUseCategory.REQUIRED : {
          useKind = REQUIRED;
          break;
        }
      }
      return useKind;
    }

    /*
     * Returns the value of the form [attribute] which affects the target
     * namespace of locally scoped features. If the form is not set on this
     * attribute, then see if there is a globally defined default. @return the
     * value of the form [attribute].
     */
    public Object getNSPrefixQualification()
    {
      String form = null;
      if (xsdAttributeUse.getContent().isSetForm())
      {
        form = xsdAttributeUse.getContent().getForm().getName();
      }
      else
      {
        XSDSchema schema = xsdAttributeUse.getSchema();
        if (schema != null)
          form = schema.getAttributeFormDefault().getName();
      }
      return form;
    }

    /**
     * Return a list of documentation elements.
     * 
     * @return a list of documentation elements.
     */
    protected CMNodeList getDocumentation()
    {
      XSDAnnotation annotation = xsdAttributeUse.getAttributeDeclaration().getAnnotation();
      return getDocumentations(annotation);
    }

    /**
     * Returns the cmdocument that is the owner of this cmnode.
     * 
     * @return the cmdocument corresponding to this cmnode.
     */
    public CMDocument getCMDocument()
    {
      return (CMDocument) getAdapter(xsdAttributeUse.getSchema());
    }
    /**
     * XSDAttributeUseAdapter.DataTypeImpl An inner class to hold type
     * information for this attribute.
     */
    public class DataTypeImpl implements CMDataType
    {
      /**
       * Returns the type of the node. The types are defined in CMNode class
       * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
       * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
       * DOCUMENTATION).
       * 
       * @return the type of this node.
       */
      public int getNodeType()
      {
        return CMNode.DATA_TYPE;
      }

      /**
       * Returns the name of the attribute type. Same as getDataTypeName().
       * 
       * @return the name of the attribute type.
       */
      public String getNodeName()
      {
        return getDataTypeName();
      }

      /**
       * Returns false. This class does not support any properties.
       * 
       * @param propertyName -
       *          name of a property
       * @return false.
       */
      public boolean supports(String propertyName)
      {
        return false;
      }

      /**
       * Returns null. This class does not support any properties.
       * 
       * @param propertyName -
       *          name of a property
       * @return null.
       */
      public Object getProperty(String propertyName)
      {
        return null;
      }

      /**
       * Returns the name of the attribute type. Same as getNodeName().
       * 
       * @return the name of the attribute type.
       */
      public String getDataTypeName()
      {
        XSDSimpleTypeDefinition sc = xsdAttributeUse.getAttributeDeclaration().getTypeDefinition();
        String typeName = sc.getName();
        return typeName != null ? typeName : "string";
      }

      /**
       * Returns the kind of constraint: none, default or fixed. The kinds are
       * defined in CMDataType class (IMPLIED_VALUE_NONE, IMPLIED_VALUE_FIXED or
       * IMPLIED_VALUE_DEFAULT).
       * 
       * @return the kind of constraint: none, default or fixed.
       */
      public int getImpliedValueKind()
      {
        int result = IMPLIED_VALUE_NONE;
        if (xsdAttributeUse.isSetConstraint())
        {
          if (xsdAttributeUse.getConstraint().getValue() == XSDConstraint.DEFAULT)
            result = IMPLIED_VALUE_DEFAULT;
          else if (xsdAttributeUse.getConstraint().getValue() == XSDConstraint.FIXED)
            result = IMPLIED_VALUE_FIXED;
        }
        return result;
      }

      /**
       * Returns the value of the default or fixed constraint.
       * 
       * @return the value of the default or fixed constraint.
       */
      public String getImpliedValue()
      {
        String result = null;
        if (xsdAttributeUse.isSetConstraint())
        {
          result = xsdAttributeUse.getLexicalValue();
        }
        return result;
      }

      /**
       * Returns the enumerated values for the attribute type.
       * 
       * @return the enumerated values for the attribute type.
       */
      public String[] getEnumeratedValues()
      {
        return getEnumeratedValuesForType(getXSDType());
      }

      /**
       * Generate a valid value for the attribute based on its type.
       * 
       * @return a valid value for the attribute based on its type.
       */
      public String generateInstanceValue()
      {
        XSDAttributeDeclaration attr = xsdAttributeUse.getAttributeDeclaration();
        return XSDTypeUtil.getInstanceValue(attr.getResolvedAttributeDeclaration().getTypeDefinition());
      }

      /**
       * Returns the corresponding XML Schema type definition.
       * 
       * @return the corresponding XML Schema type definition.
       */
      protected XSDTypeDefinition getXSDType()
      {
        XSDAttributeDeclaration attr = xsdAttributeUse.getAttributeDeclaration();
        return attr.getResolvedAttributeDeclaration().getTypeDefinition();
      }
    }
  }
  /**
   * ElementDeclarationBaseImpl implements CMElementDeclaration. This is the
   * base class for XSDElementDeclaration and DerivedElementDeclarationImpl.
   * 
   * Abstract methods in this class are: public abstract Object getKey(); public
   * abstract Object getNSPrefixQualification(); public abstract
   * XSDElementDeclaration getXSDElementDeclaration(); public abstract
   * XSDTypeDefinition getXSDType(); public abstract List getXSITypes(); public
   * abstract CMElementDeclaration getDerivedElementDeclaration(String
   * uriQualifiedTypeName); public abstract CMNode getDefinition(); public
   * abstract String getDefinitionInfo(); public abstract CMNodeListImpl
   * getSubstitutionGroup();
   */
  public static abstract class ElementDeclarationBaseImpl extends XSDBaseAdapter implements CMElementDeclaration
  {
    protected CMDataType dataType = new DataTypeImpl();
    protected CMNamedNodeMap namedNodeMap;

    /**
     * Returns corresponding XML Schema element declaration.
     * 
     * @return corresponding XML Schema element declaration.
     */
    protected abstract XSDElementDeclaration getXSDElementDeclaration();

    /**
     * Returns corresponding XML Schema element declaration.
     * 
     * @return corresponding XML Schema element declaration.
     */
    protected abstract XSDElementDeclaration getResolvedXSDElementDeclaration();

    /**
     * Returns the type of the node. The types are defined in CMNode class
     * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
     * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
     * DOCUMENTATION).
     * 
     * @return the type of this node.
     */
    public int getNodeType()
    {
      return ELEMENT_DECLARATION;
    }

    /**
     * Returns the name of the node. The same as getElementName().
     * 
     * @return the name of the node.
     */
    public String getNodeName()
    {
      return getElementName();
    }

    /**
     * Returns the name of this element. The same as getNodeName().
     * 
     * @return the name of this element.
     */
    public String getElementName()
    {
      return getResolvedXSDElementDeclaration().getName();
    }

    /**
     * Returns true if the property is supported for this class.
     * 
     * @param propertyName -
     *          name of a property
     * @return true if the property is supported.
     */
    public boolean supports(String propertyName)
    {
      return propertyName.equals(PROPERTY_XSITYPES) || propertyName.equals(PROPERTY_DERIVED_ELEMENT_DECLARATION) || propertyName.equals(PROPERTY_SUBSTITUTION_GROUP)
          || propertyName.equals(PROPERTY_ABSTRACT) || super.supports(propertyName);
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public abstract Object getKey();

    /**
     * Returns the set of attributes defined for this element.
     * 
     * @return the set of attributes defined for this element.
     */
    public CMNamedNodeMap getAttributes()
    {
      CMNamedNodeMapImpl map = new CMNamedNodeMapImpl();
      XSDTypeDefinition td = getXSDType();
      getAttributes(map, td);
      addXSITypeAttribute(map);
      return map;
    }

    /**
     * Gather the set of attributes defined for this element.
     * 
     * @param map -
     *          used for returning the set of attributes.
     * @param xsdTypeDefinition -
     *          the type definition for this element.
     */
    public void getAttributes(CMNamedNodeMapImpl map, XSDTypeDefinition xsdTypeDefinition)
    {
      if (xsdTypeDefinition instanceof XSDComplexTypeDefinition)
      {
        XSDComplexTypeDefinition ctd = (XSDComplexTypeDefinition) xsdTypeDefinition;
        for (Iterator i = ctd.getAttributeUses().iterator(); i.hasNext();)
        {
          XSDAttributeUse xsdAttributeUse = (XSDAttributeUse) i.next();
          XSDAttributeUseAdapter adapter = (XSDAttributeUseAdapter) getAdapter(xsdAttributeUse);
          if (adapter != null && adapter.getNodeName() != null)
          {
            map.getHashtable().put(adapter.getNodeName(), adapter);
          }
        }
      }
    }

    /**
     * Returns the content for this element.
     * 
     * @return the content for this element.
     */
    public CMContent getContent()
    {
      CMContent result = null;
      XSDTypeDefinition td = getXSDType();
      if (td instanceof XSDComplexTypeDefinition)
      {
        DerivedChildVisitor dcv = new DerivedChildVisitor(td);
        dcv.visitTypeDefinition(td);
        CMNodeList nodeList = dcv.getChildNodeList();
        if (nodeList.getLength() > 1)
        {
          result = new CMGroupImpl(nodeList, CMGroup.SEQUENCE);
        }
        else if (nodeList.getLength() > 0)
        {
          result = (CMContent) nodeList.item(0);
        }
      }
      return result;
    }

    /**
     * Returns the content type of this element. The content type is defined in
     * CMElementDeclaration (ANY, EMPTY, ELEMENT, MIXED, PCDATA or CDATA).
     * 
     * @return the content type of this element.
     */
    public int getContentType()
    {
      int contentType = EMPTY;
      XSDTypeDefinition td = getXSDType();
      if (td instanceof XSDSimpleTypeDefinition)
      {
        String typeName = td.getName();
        if (typeName != null && typeName.equals("anyType"))
        {
          contentType = ANY;
        }
        else
        {
          contentType = PCDATA;
        }
      }
      else if (td instanceof XSDComplexTypeDefinition)
      {
        XSDContentTypeCategory category = ((XSDComplexTypeDefinition) td).getContentTypeCategory();
        if (category != null)
        {
          switch (category.getValue())
          {
            case XSDContentTypeCategory.ELEMENT_ONLY :
              contentType = ELEMENT;
              break;
            case XSDContentTypeCategory.EMPTY :
              contentType = EMPTY;
              break;
            case XSDContentTypeCategory.MIXED :
              contentType = MIXED;
              break;
            case XSDContentTypeCategory.SIMPLE :
              contentType = PCDATA;
              break;
          }
        }
      }
      return contentType;
    }

    /**
     * Returns the name of the element type.
     * 
     * @return the name of the element type.
     */
    public CMDataType getDataType()
    {
      CMDataType result = null;
      int contentType = getContentType();
      boolean hasDataType = contentType == PCDATA || contentType == MIXED;
      if (hasDataType)
      {
        result = dataType;
      }
      return result;
    }

    /**
     * Returns the value of 'Min Occurs' attribute. The default value is "1".
     * 
     * @return the value of the 'Min Occurs' attribute.
     */
    public int getMinOccur()
    {
      return getMinOccurs(getXSDElementDeclaration());
    }

    /**
     * Returns the value of the 'Max Occurs' attribute. The default value is
     * "1".
     * 
     * @return the value of the 'Max Occurs' attribute.
     */
    public int getMaxOccur()
    {
      return getMaxOccurs(getXSDElementDeclaration());
    }

    /**
     * Returns the referenced element declaration if this is an element
     * reference. Otherwise it returns itself.
     * 
     * @return an element declaration.
     */
    protected abstract CMNode getDefinition();

    /**
     * Returns a string indicating whether the element declaration is global or
     * local. Returns null if this is an element reference.
     * 
     * @return a string indicating whether the element declaration is global or
     *         local.
     */
    protected abstract String getDefinitionInfo();

    /**
     * Returns the elements local to this element declaration.
     * 
     * @return the elements local to this element declaration.
     */
    public CMNamedNodeMap getLocalElements()
    {
      if (namedNodeMap == null)
      {
        LocalElementVisitor localElementVisitor = new LocalElementVisitor();
        localElementVisitor.visitTypeDefinition(getXSDType());
        namedNodeMap = localElementVisitor.getCMNamedNodeMap();
      }
      return namedNodeMap;
    }

    /**
     * Returns the property value for the property name. Returns null if the
     * property is not supported.
     * 
     * @param propertyName -
     *          name of a property
     * @return the property value for the property name.
     */
    public Object getProperty(String propertyName)
    {
      Object result = null;
      if (propertyName.equals(PROPERTY_DEFINITION_INFO))
      {
        result = getDefinitionInfo();
      }
      else if (propertyName.equals(PROPERTY_DEFINITION))
      {
        result = getDefinition();
      }
      else if (propertyName.equals(PROPERTY_XSITYPES))
      {
        result = getXSITypes();
      }
      else if (propertyName.startsWith(PROPERTY_DERIVED_ELEMENT_DECLARATION))
      {
        int index = propertyName.indexOf("=");
        if (index != -1)
        {
          String uriQualifiedTypeName = propertyName.substring(index + 1);
          result = getDerivedElementDeclaration(uriQualifiedTypeName);
        }
      }
      else if (propertyName.equals(PROPERTY_SUBSTITUTION_GROUP))
      {
        return getSubstitutionGroup();
      }
      else if (propertyName.equals(PROPERTY_ABSTRACT))
      {
        return getAbstract();
      }
      else
      {
        result = super.getProperty(propertyName);
      }
      return result;
    }

    /**
     * Returns the value of the 'Nillable' attribute. This represents the
     * nillable infoset property. The default value is false.
     * 
     * @return the value of the 'Nillable' attribute.
     */
    public boolean isNillable()
    {
      if (getXSDElementDeclaration().isSetNillable())
        return getXSDElementDeclaration().isNillable();
      else
        return false;
    }

    /**
     * Returns whether the element is 'Abstract'.
     * 
     * @return true if the element is 'Abstract'.
     */
    public Boolean getAbstract()
    {
      boolean result = getResolvedXSDElementDeclaration().isAbstract();
      // TODO... how do we handle elements with abstract type's ?
      return result ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Returns a list of documentation elements.
     * 
     * @return a list of documentation elements.
     */
    protected CMNodeList getDocumentation()
    {
      XSDAnnotation annotation = getXSDElementDeclaration().getAnnotation();
      return getDocumentations(annotation);
    }

    /**
     * Returns the corresponding XML Schema type definition.
     * 
     * @return the corresponding XML Schema type definition.
     */
    protected abstract XSDTypeDefinition getXSDType();

    /**
     * Returns a list of type names.
     * 
     * @return a list of type names.
     */
    protected abstract List getXSITypes();

    /**
     * Return the element declaration corresponding to the given uri qualified
     * type name.
     * 
     * @param uriQualifiedTypeName -
     *          a uri qualified type name
     * @return corresponding element declaration.
     */
    protected abstract CMElementDeclaration getDerivedElementDeclaration(String uriQualifiedTypeName);

    /**
     * Returns a list of documentation elements.
     * 
     * @return a list of documentation elements.
     */
    protected void addXSITypeAttribute(CMNamedNodeMapImpl map)
    {
      List list = getXSITypes();
      int listSize = list.size();
      if (listSize > 1)
      {
        CMDataType dataType = new CMDataTypeImpl("typeNames", (String) null);
        CMAttributeDeclarationImpl attribute = new CMAttributeDeclarationImpl("type", CMAttributeDeclaration.OPTIONAL, dataType);
        attribute.setCMDocument(xsiDocument);
        attribute.setPrefixQualification(true);
        attribute.setXSITypes(list);
        map.getHashtable().put(attribute.getNodeName(), attribute);
      }
    }

    /**
     * Returns the cmdocument that is the owner of this cmnode.
     * 
     * @return the cmdocument corresponding to this cmnode.
     */
    public CMDocument getCMDocument()
    {
      return (CMDocument) getAdapter(getXSDElementDeclaration().getSchema());
    }

    /**
     * Returns the substitution group for this element. The group consists of:
     * 1. the element declaration itself 2. and any element declaration that has
     * a {substitution group affiliation} in the group
     * 
     * @return the substitution group for this element.
     */
    protected abstract CMNodeListImpl getSubstitutionGroup();
    /*
     * XSDElementDeclarationAdapter.DataTypeImpl An inner class to hold type
     * information for this element.
     */
    public class DataTypeImpl implements CMDataType
    {
      /**
       * Returns the type of the node. The types are defined in CMNode class
       * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
       * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
       * DOCUMENTATION).
       * 
       * @return the type of this node.
       */
      public int getNodeType()
      {
        return CMNode.DATA_TYPE;
      }

      /**
       * Returns the name of the element type. Same as getDataTypeName().
       * 
       * @return the name of the element type.
       */
      public String getNodeName()
      {
        return getDataTypeName();
      }

      /**
       * Returns false. This class does not support any properties.
       * 
       * @param propertyName -
       *          name of a property
       * @return false.
       */
      public boolean supports(String propertyName)
      {
        return false;
      }

      /**
       * Returns null. This class does not support any properties.
       * 
       * @param propertyName -
       *          name of a property
       * @return null.
       */
      public Object getProperty(String propertyName)
      {
        return null;
      }

      /**
       * Returns the name of the element type. Same as getNodeName().
       * 
       * @return the name of the element type.
       */
      public String getDataTypeName()
      {
        String typeName = null;
        XSDSimpleTypeDefinition std = getXSDType().getSimpleType();
        if (std != null)
          typeName = std.getName();
        return typeName != null ? typeName : "string";
      }

      /**
       * Returns the kind of constraint: none, default or fixed. The kinds are
       * defined in CMDataType class (IMPLIED_VALUE_NONE, IMPLIED_VALUE_FIXED or
       * IMPLIED_VALUE_DEFAULT).
       * 
       * @return the kind of constraint: none, default or fixed.
       */
      public int getImpliedValueKind()
      {
        int result = IMPLIED_VALUE_NONE;
        if (getXSDElementDeclaration().isSetConstraint())
        {
          if (getXSDElementDeclaration().getConstraint().getValue() == XSDConstraint.DEFAULT)
            result = IMPLIED_VALUE_DEFAULT;
          else if (getXSDElementDeclaration().getConstraint().getValue() == XSDConstraint.FIXED)
            result = IMPLIED_VALUE_FIXED;
        }
        return result;
      }

      /**
       * Returns the value of the default or fixed constraint.
       * 
       * @return the value of the default or fixed constraint.
       */
      public String getImpliedValue()
      {
        String result = null;
        if (getXSDElementDeclaration().isSetConstraint())
        {
          result = getXSDElementDeclaration().getLexicalValue();
        }
        return result;
      }

      /**
       * Returns the enumerated values for the attribute type.
       * 
       * @return the enumerated values for the attribute type.
       */
      public String[] getEnumeratedValues()
      {
        return getEnumeratedValuesForType(getXSDType());
      }

      public String generateInstanceValue()
      {
        return XSDTypeUtil.getInstanceValue(getXSDType());
      }

      /**
       * Returns the cmdocument that is the owner of this cmnode.
       * 
       * @return the cmdocument corresponding to this cmnode.
       */
      public CMDocument getCMDocument()
      {
        return (CMDocument) getAdapter(getXSDElementDeclaration().getSchema());
      }
    }
  }
  /**
   * XSDElementDeclarationAdapter implements CMElementDeclaration. A
   * representation of the model object 'Element Declaration'.
   */
  public static class XSDElementDeclarationAdapter extends ElementDeclarationBaseImpl
  {
    protected List derivedElementDeclarations = null;
    protected List xsiTypes = null;
    protected XSDElementDeclaration xsdElementDeclaration;
    protected CMNodeListImpl substitutionGroup;

    /**
     * Constructor.
     * 
     * @param xsdElementDeclaration -
     *          the XML Schema node.
     */
    public XSDElementDeclarationAdapter(XSDElementDeclaration xsdElementDeclaration)
    {
      this.xsdElementDeclaration = xsdElementDeclaration;
    }

    /**
     * Returns corresponding XML Schema element declaration.
     * 
     * @return corresponding XML Schema element declaration.
     */
    protected XSDElementDeclaration getXSDElementDeclaration()
    {
      return xsdElementDeclaration;
    }

    /**
     * Returns corresponding XML Schema element declaration.
     * 
     * @return corresponding XML Schema element declaration.
     */
    protected XSDElementDeclaration getResolvedXSDElementDeclaration()
    {
      return xsdElementDeclaration.getResolvedElementDeclaration();
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public Object getKey()
    {
      return xsdElementDeclaration;
    }

    /**
     * Returns the referenced element declaration if this is an element
     * reference. Otherwise it returns itself.
     * 
     * @return an element declaration.
     */
    public CMNode getDefinition()
    {
      return getAdapter(xsdElementDeclaration.getResolvedElementDeclaration());
    }

    /**
     * Returns a string indicating whether the element declaration is global or
     * local. Returns null if this is an element reference.
     * 
     * @return a string indicating whether the element declaration is global or
     *         local.
     */
    protected String getDefinitionInfo()
    {
      if (xsdElementDeclaration.isElementDeclarationReference())
        return null;
      else if (xsdElementDeclaration.isGlobal())
        return DEFINITION_INFO_GLOBAL;
      else
        return DEFINITION_INFO_LOCAL;
    }

    public Object getNSPrefixQualification()
    {
      String form = null;
      if (xsdElementDeclaration.isElementDeclarationReference())
      {
        form = "qualified";
      }
      else
      {
        if (xsdElementDeclaration.isSetForm())
        {
          form = xsdElementDeclaration.getForm().getName();
        }
        else
        {
          XSDSchema schema = xsdElementDeclaration.getSchema();
          if (schema != null)
            form = schema.getElementFormDefault().getName();
        }
      }
      return form;
    }

    /**
     * Returns the corresponding XML Schema type definition.
     * 
     * @return the corresponding XML Schema type definition.
     */
    protected XSDTypeDefinition getXSDType()
    {
      return xsdElementDeclaration.getResolvedElementDeclaration().getTypeDefinition();
    }

    /**
     * Returns a list of type names.
     * 
     * @return a list of type names.
     */
    protected List getXSITypes()
    {
      if (xsiTypes == null)
      {
        computeDerivedTypeInfo();
      }
      return xsiTypes;
    }

    protected void computeDerivedTypeInfo()
    {
      xsiTypes = new Vector();
      derivedElementDeclarations = new Vector();
      computeDerivedTypeInfoHelper(getXSDType(), xsiTypes, derivedElementDeclarations);
    }

    protected void computeDerivedTypeInfoHelper(XSDTypeDefinition type, List typeNameList, List edList)
    {
      if (type instanceof XSDComplexTypeDefinition)
      {
        List derivedTypes = findTypesDerivedFrom(type);
        ArrayList inclusiveDerivedTypes = new ArrayList();
        inclusiveDerivedTypes.add(type);
        if ((derivedTypes != null) && (derivedTypes.size() > 0))
        {
          inclusiveDerivedTypes.addAll(derivedTypes);
        }
        for (Iterator i = inclusiveDerivedTypes.iterator(); i.hasNext();)
        {
          XSDTypeDefinition derivedType = (XSDTypeDefinition) i.next();
          XSDSchema schema = derivedType.getSchema();
          if (schema != null)
          {
            String uri = schema.getTargetNamespace();
            String name = derivedType.getName();
            if (name != null)
            {
              name = uri != null ? ("[" + uri + "]" + name) : name;
              typeNameList.add(name);
              DerivedElementDeclarationImpl ed = new DerivedElementDeclarationImpl(this, derivedType, name);
              edList.add(ed);
            }
          }
        }
      }
    }

    /**
     * Return the element declaration corresponding to the given uri qualified
     * type name.
     * 
     * @param uriQualifiedTypeName -
     *          a uri qualified type name
     * @return corresponding element declaration.
     */
    protected CMElementDeclaration getDerivedElementDeclaration(String uriQualifiedTypeName)
    {
      CMElementDeclaration result = null;
      if (derivedElementDeclarations == null)
      {
        computeDerivedTypeInfo();
      }
      for (Iterator i = derivedElementDeclarations.iterator(); i.hasNext();)
      {
        DerivedElementDeclarationImpl ed = (DerivedElementDeclarationImpl) i.next();
        if ((ed != null) && (ed.uriQualifiedTypeName != null))
        {
          if (ed.uriQualifiedTypeName.equals(uriQualifiedTypeName))
          {
            result = ed;
            break;
          }
        }
      }
      return result;
    }

    /**
     * Returns the substitution group for this element. The group consists of:
     * 1. the element declaration itself 2. and any element declaration that has
     * a {substitution group affiliation} in the group
     * 
     * @return the substitution group for this element.
     */
    protected CMNodeListImpl getSubstitutionGroup()
    {
      if (substitutionGroup == null)
      {
        substitutionGroup = new CMNodeListImpl();
        List sgroup = getResolvedXSDElementDeclaration().getSubstitutionGroup();
        for (Iterator i = sgroup.iterator(); i.hasNext();)
        {
          XSDElementDeclaration ed = (XSDElementDeclaration) i.next();
          substitutionGroup.add(getAdapter(ed));
        }
      }
      return substitutionGroup;
    }
  }
  /**
   * DerivedElementDeclarationImpl extends ElementDeclarationBaseImpl
   *  
   */
  public static class DerivedElementDeclarationImpl extends ElementDeclarationBaseImpl
  {
    protected XSDElementDeclarationAdapter owner;
    protected XSDTypeDefinition xsdType;
    public String uriQualifiedTypeName;

    /**
     * Constructor.
     * 
     * @param owner -
     * @param xsdType -
     * @param uriQualifiedTypeName -
     */
    public DerivedElementDeclarationImpl(XSDElementDeclarationAdapter owner, XSDTypeDefinition xsdType, String uriQualifiedTypeName)
    {
      this.owner = owner;
      this.xsdType = xsdType;
      this.uriQualifiedTypeName = uriQualifiedTypeName;
    }

    /**
     * Returns corresponding XML Schema element declaration.
     * 
     * @return corresponding XML Schema element declaration.
     */
    protected XSDElementDeclaration getXSDElementDeclaration()
    {
      return (XSDElementDeclaration) owner.getKey();
    }

    /**
     * Returns corresponding XML Schema element declaration.
     * 
     * @return corresponding XML Schema element declaration.
     */
    protected XSDElementDeclaration getResolvedXSDElementDeclaration()
    {
      return ((XSDElementDeclaration) owner.getKey()).getResolvedElementDeclaration();
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public Object getKey()
    {
      return owner.getKey();
    }

    /**
     * Returns the corresponding XML Schema type definition.
     * 
     * @return the corresponding XML Schema type definition.
     */
    protected XSDTypeDefinition getXSDType()
    {
      return xsdType;
    }

    /**
     * Returns a list of type names.
     * 
     * @return a list of type names.
     */
    protected List getXSITypes()
    {
      return owner.getXSITypes();
    }

    /**
     * Return the element declaration corresponding to the given uri qualified
     * type name.
     * 
     * @param uriQualifiedTypeName -
     *          a uri qualified type name
     * @return corresponding element declaration.
     */
    protected CMElementDeclaration getDerivedElementDeclaration(String uriQualifiedTypeName)
    {
      return owner.getDerivedElementDeclaration(uriQualifiedTypeName);
    }

    /**
     * Returns the referenced element declaration if this is an element
     * reference. Otherwise it returns itself.
     * 
     * @return an element declaration.
     */
    protected CMNode getDefinition()
    {
      return this;
    }

    /**
     * Returns a string indicating whether the element declaration is global or
     * local. Returns null if this is an element reference.
     * 
     * @return a string indicating whether the element declaration is global or
     *         local.
     */
    protected String getDefinitionInfo()
    {
      return owner.getDefinitionInfo();
    }

    /*
     * Returns the value of the form [attribute] which affects the target
     * namespace of locally scoped features. @return the value of the form
     * [attribute].
     */
    public Object getNSPrefixQualification()
    {
      return owner.getNSPrefixQualification();
    }

    /**
     * Returns the substitution group for this element. The group consists of:
     * 1. the element declaration itself 2. and any element declaration that has
     * a {substitution group affiliation} in the group
     * 
     * @return the substitution group for this element.
     */
    protected CMNodeListImpl getSubstitutionGroup()
    {
      return owner.getSubstitutionGroup();
    }
  }
  /**
   * XSDWildcardAdapter
   */
  public static class XSDWildcardAdapter extends XSDBaseAdapter implements CMAnyElement
  {
    protected XSDWildcard xsdWildcard;

    public XSDWildcardAdapter(XSDWildcard xsdWildcard)
    {
      this.xsdWildcard = xsdWildcard;
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public Object getKey()
    {
      return xsdWildcard;
    }

    /**
     * Returns the type of the node. The types are defined in CMNode class
     * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
     * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
     * DOCUMENTATION).
     * 
     * @return the type of this node.
     */
    public int getNodeType()
    {
      return ANY_ELEMENT;
    }

    /**
     * Returns the name of the node. The default value is an empty string value.
     * All derived classes must override this method if they do not want the
     * default value.
     * 
     * @return the name of the node.
     */
    public String getNodeName()
    {
      return "any";
    }

    public String getNamespaceURI()
    {
      String uri = xsdWildcard.getElement().getAttribute("namespace");
      return (uri != null || uri.length() == 0) ? uri : "##any";
    }

    /**
     * Returns the value of 'Min Occurs' attribute. The default value is "1".
     * 
     * @return the value of the 'Min Occurs' attribute.
     */
    public int getMinOccur()
    {
      return getMinOccurs(xsdWildcard);
    }

    /**
     * Returns the value of the 'Max Occurs' attribute. The default value is
     * "1".
     * 
     * @return the value of the 'Max Occurs' attribute.
     */
    public int getMaxOccur()
    {
      return getMaxOccurs(xsdWildcard);
    }

    /**
     * Returns the cmdocument that is the owner of this cmnode.
     * 
     * @return the cmdocument corresponding to this cmnode.
     */
    public CMDocument getCMDocument()
    {
      return (CMDocument) getAdapter(xsdWildcard.getSchema());
    }

    /**
     * Returns a list of documentation elements.
     * 
     * @return a list of documentation elements.
     */
    protected CMNodeList getDocumentation()
    {
      XSDAnnotation annotation = xsdWildcard.getAnnotation();
      return getDocumentations(annotation);
    }
  }
  /**
   * XSDModelGroupAdapter
   */
  public static class XSDModelGroupAdapter extends XSDBaseAdapter implements CMGroup
  {
    protected XSDModelGroup xsdModelGroup;

    public XSDModelGroupAdapter()
    {
    }

    public XSDModelGroupAdapter(XSDModelGroup xsdModelGroup)
    {
      this.xsdModelGroup = xsdModelGroup;
    }

    /**
     * Returns the type of the node. The types are defined in CMNode class
     * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
     * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
     * DOCUMENTATION).
     * 
     * @return the type of this node.
     */
    public int getNodeType()
    {
      return GROUP;
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public Object getKey()
    {
      return xsdModelGroup;
    }

    /**
     * Returns a list of the children of this group.
     * 
     * @return a list of the children of this group.
     */
    public CMNodeList getChildNodes()
    {
      CMNodeListImpl nodeList = new CMNodeListImpl();
      if (xsdModelGroup != null)
      {
        for (Iterator i = xsdModelGroup.getParticles().iterator(); i.hasNext();)
        {
          XSDParticle particle = (XSDParticle) i.next();
          XSDParticleContent content = particle.getContent();
          CMNode adapter = getAdapter(content);
          if (adapter != null)
          {
            nodeList.getList().add(adapter);
          }
        }
      }
      return nodeList;
    }

    /**
     * Returns the name of the node. The default value is an empty string value.
     * All derived classes must override this method if they do not want the
     * default value.
     * 
     * @return the name of the node.
     */
    public String getNodeName()
    {
      CMDescriptionBuilder descriptionBuilder = new CMDescriptionBuilder();
      return descriptionBuilder.buildDescription(this);
    }

    /**
     * Returns the value of 'Min Occurs' attribute. The default value is "1".
     * 
     * @return the value of the 'Min Occurs' attribute.
     */
    public int getMinOccur()
    {
      return getMinOccurs(xsdModelGroup);
    }

    /**
     * Returns the value of the 'Max Occurs' attribute. The default value is
     * "1".
     * 
     * @return the value of the 'Max Occurs' attribute.
     */
    public int getMaxOccur()
    {
      return getMaxOccurs(xsdModelGroup);
    }

    /**
     * Return operator of this group -- CHOICE, SEQUENCE or ALL value.
     * 
     * @return the operator of this group.
     */
    public int getOperator()
    {
      int result = 0;
      //todo... handle ALONE case by checkig if child count == 1
      if (xsdModelGroup != null)
      {
        switch (xsdModelGroup.getCompositor().getValue())
        {
          case XSDCompositor.CHOICE : {
            result = CHOICE;
            break;
          }
          case XSDCompositor.SEQUENCE : {
            result = SEQUENCE;
            break;
          }
          case XSDCompositor.ALL : {
            result = ALL;
            break;
          }
        }
      }
      return result;
    }

    /**
     * Returns a list of documentation elements.
     * 
     * @return a list of documentation elements.
     */
    protected CMNodeList getDocumentation()
    {
      XSDAnnotation annotation = xsdModelGroup.getAnnotation();
      return getDocumentations(annotation);
    }

    /**
     * Returns the cmdocument that is the owner of this cmnode.
     * 
     * @return the cmdocument corresponding to this cmnode.
     */
    public CMDocument getCMDocument()
    {
      return (CMDocument) getAdapter(xsdModelGroup.getSchema());
    }
  }
  /**
   * XSDModelGroupDefinitionAdapter
   */
  public static class XSDModelGroupDefinitionAdapter extends XSDBaseAdapter implements CMGroup
  {
    protected XSDModelGroupDefinition xsdModelGroupDefinition;

    public XSDModelGroupDefinitionAdapter(XSDModelGroupDefinition xsdModelGroupDefinition)
    {
      this.xsdModelGroupDefinition = xsdModelGroupDefinition;
    }

    /**
     * Returns the type of the node. The types are defined in CMNode class
     * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
     * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
     * DOCUMENTATION).
     * 
     * @return the type of this node.
     */
    public int getNodeType()
    {
      return GROUP;
    }

    /**
     * Returns the key for this cmnode which is the corresponding XML Schema
     * node.
     * 
     * @return the key for this cmnode.
     */
    public Object getKey()
    {
      return xsdModelGroupDefinition;
    }

    /**
     * Returns a list of the children of this group.
     * 
     * @return a list of the children of this group.
     */
    public CMNodeList getChildNodes()
    {
      CMNodeListImpl nodeList = new CMNodeListImpl();
      XSDModelGroup modelGroup = xsdModelGroupDefinition.getResolvedModelGroupDefinition().getModelGroup();
      if (modelGroup != null)
      {
        CMNode adapter = getAdapter(modelGroup);
        nodeList.add(adapter);
      }
      return nodeList;
    }

    /**
     * Returns the name of the node. The default value is an empty string value.
     * All derived classes must override this method if they do not want the
     * default value.
     * 
     * @return the name of the node.
     */
    public String getNodeName()
    {
      CMDescriptionBuilder descriptionBuilder = new CMDescriptionBuilder();
      return descriptionBuilder.buildDescription(this);
    }

    /**
     * Returns the value of 'Min Occurs' attribute. The default value is "1".
     * 
     * @return the value of the 'Min Occurs' attribute.
     */
    public int getMinOccur()
    {
      return getMinOccurs(xsdModelGroupDefinition);
    }

    /**
     * Returns the value of the 'Max Occurs' attribute. The default value is
     * "1".
     * 
     * @return the value of the 'Max Occurs' attribute.
     */
    public int getMaxOccur()
    {
      return getMaxOccurs(xsdModelGroupDefinition);
    }

    /**
     * Return operator of this group -- CHOICE, SEQUENCE or ALL value.
     * 
     * @return the operator of this group.
     */
    public int getOperator()
    {
      return XSDCompositor.SEQUENCE;
    }

    /**
     * Returns a list of documentation elements.
     * 
     * @return a list of documentation elements.
     */
    protected CMNodeList getDocumentation()
    {
      XSDAnnotation annotation = xsdModelGroupDefinition.getAnnotation();
      return getDocumentations(annotation);
    }

    /**
     * Returns the cmdocument that is the owner of this cmnode.
     * 
     * @return the cmdocument corresponding to this cmnode.
     */
    public CMDocument getCMDocument()
    {
      return (CMDocument) getAdapter(xsdModelGroupDefinition.getSchema());
    }
  }
  /**
   * DocumentationImpl implements CMDocumentation. A representation of the
   * documentation element part of the 'User Information' feature. Working with
   * the documentation element requires dropping down into the DOM model.
   */
  public static class DocumentationImpl implements CMDocumentation
  {
    protected Element documentation;

    /**
     * Constructor.
     * 
     * @param documentation -
     *          a documentation element.
     */
    public DocumentationImpl(Element documentation)
    {
      this.documentation = documentation;
    }

    /**
     * Returns the type of the node. The types are defined in CMNode class
     * (ANY_ELEMENT, ATTRIBUTE_DECLARATION, DATA_TYPE, DOCUMENT,
     * ELEMENT_DECLARATION, ENTITY_DECLARATION, GROUP, NAME_SPACE or
     * DOCUMENTATION).
     * 
     * @return the type of this node.
     */
    public int getNodeType()
    {
      return DOCUMENTATION;
    }

    /**
     * Returns an empty string value.
     * 
     * @return an empty string value.
     */
    public String getNodeName()
    {
      return "";
    }

    /**
     * Returns false. This class does not support any properties.
     * 
     * @param propertyName -
     *          name of a property
     * @return false.
     */
    public boolean supports(String propertyName)
    {
      return false;
    }

    /**
     * Returns null. This class does not support any properties.
     * 
     * @param propertyName -
     *          name of a property
     * @return null.
     */
    public Object getProperty(String propertyName)
    {
      return null;
    }

    /**
     * Returns the content of the documentation element.
     * 
     * @return the content of the documentation element.
     */
    public String getValue()
    {
      String content = "";
      boolean contentFound = false;
      NodeList nodes = documentation.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
        Node node = nodes.item(i);
        if (node instanceof Text)
        {
          contentFound = true;
          content += node.getNodeValue();
        }
      }
      return contentFound ? content : null;
    }

    /**
     * Returns the xml:lang attribute value of the documentation element.
     * 
     * @return the xml:lang attribute value of the documentation element.
     */
    public String getLanguage()
    {
      return documentation.hasAttributeNS(XSDConstants.XML_NAMESPACE_URI_1998, XML_LANG_ATTRIBUTE) ? documentation.getAttributeNS(XSDConstants.XML_NAMESPACE_URI_1998, XML_LANG_ATTRIBUTE) : null;
    }

    /**
     * Returns the source attribute value of the documentation element.
     * 
     * @return the source attribute value of the documentation element.
     */
    public String getSource()
    {
      return documentation.hasAttributeNS(null, XSDConstants.SOURCE_ATTRIBUTE) ? documentation.getAttributeNS(null, XSDConstants.SOURCE_ATTRIBUTE) : null;
    }
  }
  /**
   * XSIDocument extends CMDocumentImpl. This class is used to hold those
   * attributes that are for direct use in any XML documents. These attributes
   * are in a different namespace, which has the namespace name
   * http://www.w3.org/2001/XMLSchema-instance. Attributes in this namespace
   * include: xsi:type xsi:nil xsi:schemaLocation xsi:noNamespaceSchemaLocation
   */
  public static class XSIDocument extends CMDocumentImpl
  {
    public CMAttributeDeclarationImpl nilAttribute;

    /**
     * Constructor. Creates the 'xsi:nil'
     */
    public XSIDocument()
    {
      super(XSDConstants.SCHEMA_INSTANCE_URI_2001);
      // create the 'nill' attribute
      String[] values = {"false", "true"};
      nilAttribute = new CMAttributeDeclarationImpl("nil", CMAttributeDeclaration.REQUIRED, new CMDataTypeImpl("boolean", values));
      nilAttribute.setPrefixQualification(true);
      nilAttribute.setCMDocument(this);
    }
  }
  /**
   * Note this XSD model visitor differs from the XSD model visitor in
   * com.ibm.etools.xsdeditor plugin. In visitModelGroup method we call
   * getParticles() instead of getContents(). This gathers all of the content of
   * a derived type.
   */
  public static class XSDCMVisitor extends XSDVisitor
  {
    public void visitSimpleTypeDefinition(XSDSimpleTypeDefinition type)
    {
      XSDParticle ctd = type.getComplexType();
      if (ctd != null)
        visitParticle(ctd);
    }

    public void visitModelGroup(XSDModelGroup modelGroup)
    {
      if (modelGroup.getParticles() != null)
      {
        for (Iterator iterator = modelGroup.getParticles().iterator(); iterator.hasNext();)
        {
          XSDParticle particle = (XSDParticle) iterator.next();
          visitParticle(particle);
        }
      }
    }

    public void visitModelGroupDefinition(XSDModelGroupDefinition modelGroupDef)
    {
      XSDModelGroup modelGroup = modelGroupDef.getResolvedModelGroupDefinition().getModelGroup();
      if (modelGroup != null)
      {
        visitModelGroup(modelGroup);
      }
    }
  }
  /**
   * A visitor class that walks the xsd model and computes the list of children
   * that belong to the initially visited element type.
   */
  public static class DerivedChildVisitor extends XSDCMVisitor
  {
    protected CMNodeListImpl childNodeList = new CMNodeListImpl();
    protected List baseTypeList = new Vector();
    Object root;

    DerivedChildVisitor(Object root)
    {
      this.root = root;
    }

    public CMNodeListImpl getChildNodeList()
    {
      return childNodeList;
    }

    public void visitWildcard(XSDWildcard wildcard)
    {
      childNodeList.getList().add(getAdapter(wildcard));
    }

    public void visitElementDeclaration(XSDElementDeclaration element)
    {
      childNodeList.getList().add(getAdapter(element));
    }

    public void visitModelGroup(XSDModelGroup modelGroup)
    {
      childNodeList.getList().add(getAdapter(modelGroup));
    }

    public void visitModelGroupDefinition(XSDModelGroupDefinition modelGroupDefinition)
    {
      childNodeList.getList().add(getAdapter(modelGroupDefinition));
    }
  }
  /**
   * A visitor class that gathers all of the elements within a type definition.
   */
  public static class LocalElementVisitor extends XSDCMVisitor
  {
    protected CMNamedNodeMapImpl namedNodeMap = new CMNamedNodeMapImpl();
    protected List baseTypeList = new Vector();

    public void visitElementDeclaration(XSDElementDeclaration element)
    {
      XSDElementDeclarationAdapter adapter = (XSDElementDeclarationAdapter) getAdapter(element);
      namedNodeMap.getHashtable().put(adapter.getNodeName(), adapter);
    }

    public CMNamedNodeMap getCMNamedNodeMap()
    {
      return namedNodeMap;
    }
  }
}