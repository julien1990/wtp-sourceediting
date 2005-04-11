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
package org.eclipse.wst.xml.core.internal.contentmodel.modelqueryimpl;
                          
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
import org.eclipse.wst.xml.core.internal.contentmodel.internal.annotation.AnnotationUtility;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.CMDocumentManager;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.CMDocumentManagerListener;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.CMDocumentReferenceProvider;
import org.eclipse.wst.xml.core.internal.contentmodel.util.CMDocumentCache;


/**
 *
 */
public class CMDocumentManagerImpl implements CMDocumentManager
{                                
  protected CMDocumentCache cmDocumentCache;
  protected CMDocumentReferenceProvider cmDocumentReferenceProvider;
  protected List listenerList = new Vector(); 
  protected Hashtable propertyTable = new Hashtable();
  protected Hashtable publicIdTable = new Hashtable();

       
  public CMDocumentManagerImpl(CMDocumentCache cmDocumentCache, CMDocumentReferenceProvider cmDocumentReferenceProvider)
  {                                                       
    this.cmDocumentCache = cmDocumentCache;                                                                            
    this.cmDocumentReferenceProvider = cmDocumentReferenceProvider;
    setPropertyEnabled(PROPERTY_AUTO_LOAD, true);
    setPropertyEnabled(PROPERTY_USE_CACHED_RESOLVED_URI, false);
  }         

       
  public CMDocumentCache getCMDocumentCache()
  {
    return cmDocumentCache;
  }

 
  public void setPropertyEnabled(String propertyName, boolean value)
  {
    propertyTable.put(propertyName, value ? "true" : "false");
    for (Iterator i = listenerList.iterator(); i.hasNext(); )
    {
      CMDocumentManagerListener listener = (CMDocumentManagerListener)i.next();
      listener.propertyChanged(this, propertyName);
    }
  }                                        

        
  public boolean getPropertyEnabled(String propertyName)
  {
    Object object = propertyTable.get(propertyName);
    return object != null && object.equals("true");
  }


  public void addListener(CMDocumentManagerListener listener)
  {
    listenerList.add(listener);
    cmDocumentCache.addListener(listener);
  }


  public void removeListener(CMDocumentManagerListener listener)
  {
    listenerList.remove(listener);
    cmDocumentCache.removeListener(listener);
  }                       
   
                   
  protected String lookupResolvedURI(String publicId)
  {
    String key = publicId != null ? publicId : "";
    return (String)publicIdTable.get(key);
  }
    

  protected String lookupOrCreateResolvedURI(String publicId, String systemId)
  {                    
    String resolvedURI = null;                  

    String key = publicId != null ? publicId : "";

    if (getPropertyEnabled(PROPERTY_USE_CACHED_RESOLVED_URI))
    {
      resolvedURI = (String)publicIdTable.get(key);
    }   

    if (resolvedURI == null)
    {
      resolvedURI = cmDocumentReferenceProvider.resolveGrammarURI(publicId, systemId);
      if (resolvedURI == null)
      {
        resolvedURI = "";
      }
      publicIdTable.put(key, resolvedURI);     
    }                       
  
    return resolvedURI;
  }


  public int getCMDocumentStatus(String publicId)
  {                                           
    int status = CMDocumentCache.STATUS_NOT_LOADED; 
    String resolvedURI = lookupResolvedURI(publicId);
    if (resolvedURI != null)
    {                      
      status = cmDocumentCache.getStatus(resolvedURI);
    }
    return status;
  }    
              

  public CMDocument getCMDocument(String publicId)
  {                                           
    CMDocument result = null;
    String resolvedURI = lookupResolvedURI(publicId);
    if (resolvedURI != null)
    {                      
      result = cmDocumentCache.getCMDocument(resolvedURI);
    }
    return result;
  }    
  

  /* (non-Javadoc)
 * @see org.eclipse.wst.xml.core.internal.contentmodel.modelquery.CMDocumentManager#getCMDocument(java.lang.String, java.lang.String, java.lang.String)
 */
public CMDocument getCMDocument(String publicId, String systemId, String type)
  {                
    CMDocument cmDocument = null;                           
    String resolvedURI = null;

    if (getPropertyEnabled(PROPERTY_AUTO_LOAD))
    {
      resolvedURI = lookupOrCreateResolvedURI(publicId, systemId);
    }    
    else
    {
      resolvedURI = lookupResolvedURI(publicId);
    } 

    if (resolvedURI != null)
    {                   
      int status = cmDocumentCache.getStatus(resolvedURI);
      if (status == CMDocumentCache.STATUS_LOADED)
      {                      
        cmDocument = cmDocumentCache.getCMDocument(resolvedURI);
      }   
      else if (status == CMDocumentCache.STATUS_NOT_LOADED)
      {     
        if (getPropertyEnabled(PROPERTY_AUTO_LOAD))
        {
          cmDocument = loadCMDocument(publicId, resolvedURI, type, getPropertyEnabled(PROPERTY_ASYNC_LOAD));
        }
      }
    }
    return cmDocument;   
  } 
  
  public void addCMDocumentReference(String publicId, String systemId, String type)
  {
    String resolvedURI = lookupOrCreateResolvedURI(publicId, systemId);
    if (resolvedURI != null && resolvedURI.length() > 0)
    {                                                                      
      int status = cmDocumentCache.getStatus(resolvedURI);
      if (status == CMDocumentCache.STATUS_NOT_LOADED)
      {                      
        loadCMDocument(publicId, resolvedURI, type, getPropertyEnabled(PROPERTY_ASYNC_LOAD));
      }         
    } 
  }
     

  public void addCMDocument(String publicId, String systemId, String resolvedURI, String type, CMDocument cmDocument)
  {
    String key = publicId != null ? publicId : "";
    publicIdTable.put(key, resolvedURI);
    cmDocumentCache.putCMDocument(resolvedURI, cmDocument);
  }


  protected CMDocument loadCMDocument(final String publicId, final String resolvedURI, final String type, boolean async)
  {                                                      
    CMDocument result = null;
                         
    //System.out.println("about to build CMDocument(" + publicId + ", " + unresolvedURI + " = " + resolvedURI + ")");
    if (async)
    {     
      cmDocumentCache.setStatus(resolvedURI, CMDocumentCache.STATUS_LOADING);
      //Thread thread = new Thread(new AsyncBuildOperation(publicId, resolvedURI, type));
      //thread.start();
      Job job = new Job("loading " + resolvedURI)
      {
        public boolean belongsTo(Object family)
        {
          boolean result = (family == CMDocumentManager.class);
          return result;
        }
        
        protected IStatus run(IProgressMonitor monitor)
        {
          try
          {
            new AsyncBuildOperation(publicId, resolvedURI, type).run();
          }
          catch (Exception e)
          {
          }
          return Status.OK_STATUS;
        }
      };
      job.schedule();
    }
    else
    {                
      result = buildCMDocument(publicId, resolvedURI, type);
    }          
    return result;
  } 

    

  protected class AsyncBuildOperation implements Runnable
  {
    protected String publicId;
    protected String resolvedURI;    
    protected String type;

    public AsyncBuildOperation(String publicId, String resolvedURI, String type)
    {
      this.publicId = publicId;
      this.resolvedURI = resolvedURI;                                          
      this.type = type;
    }      

    public void run()
    {
      buildCMDocument(publicId, resolvedURI, type);
    }
  }
    
  
  public synchronized CMDocument buildCMDocument(String publicId, String resolvedURI, String type)
  {                                     
    cmDocumentCache.setStatus(resolvedURI, CMDocumentCache.STATUS_LOADING);
  
    CMDocument result = null;         
    int x = 1;
    x++;
    if (resolvedURI != null && resolvedURI.length() > 0)
    {
      // TODO... pass the TYPE thru to the CMDocumentBuilder
      result = ContentModelManager.getInstance().createCMDocument(resolvedURI, type);
    }
    if (result != null)
    { 
      // load the annotation files for the document 
      if (publicId != null)
      {    
        AnnotationUtility.loadAnnotationsForGrammar(publicId, result);
      }
      cmDocumentCache.putCMDocument(resolvedURI, result);
    }
    else
    {
      cmDocumentCache.setStatus(resolvedURI, CMDocumentCache.STATUS_ERROR);
    }
    return result;
  } 

  public void removeAllReferences()
  {
    // TODO... initiate a timed release of the entries in the CMDocumentCache
    publicIdTable = new Hashtable();
  }
}                                            
