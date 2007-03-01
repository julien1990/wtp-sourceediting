package org.eclipse.wst.html.core.internal.provisional;

import org.eclipse.wst.html.core.internal.provisional.HTML40Namespace;
import org.eclipse.wst.html.core.text.IHTMLPartitions;

public interface JavaScriptNameSpace extends HTML40Namespace{
	/* remove when when we refactor (need to add this content type to many project types in wst) */
	public static final String natureHandlerID = "org.eclipse.jsdt.core.jsNature";
	
	public static final String[] TAKEOVER_PARTITION_TYPES={"none"};
	
	//public static final String NEW_PARTITION_TYPE="org.eclipse.wst.jsdt.StructuredJs";
	//public static final String NEW_PARTITION_TYPE=IHTMLPartitions.SCRIPT;
	public static final String NEW_PARTITION_TYPE=IHTMLPartitions.SCRIPT;
	
	public static final String BASE_FILE_EXTENSION = ".js";
	
	public static final String[] JSVALIDDATATYPES = {"JAVASCRIPT",
													 "TEXT/JAVASCRIPT"}; //$NON-NLS-1$
	
	public static final String[] EVENTS = {ATTR_NAME_ONCLICK, ATTR_NAME_ONDBLCLICK, ATTR_NAME_ONMOUSEDOWN, ATTR_NAME_ONMOUSEUP, ATTR_NAME_ONMOUSEOVER, ATTR_NAME_ONMOUSEMOVE, ATTR_NAME_ONMOUSEOUT, ATTR_NAME_ONKEYPRESS, ATTR_NAME_ONKEYDOWN, ATTR_NAME_ONKEYUP, ATTR_NAME_ONHELP};
	
	
	
	public static String[] KEYWORDS = {"abstract",
										
										"break",
										
										"case",
										"catch",
										
										"class",
										"const",
										"continue",
										"debugger",
										"default",
										"delete",
										"do",
										
										"else",
										"enum",
										"export",
										"extends",
										
										"final",
										"finally",
										
										"for",
										"function",
										"goto",
										"if",
										"implements",
										"import",
										"in",
										"instanceof",
										
										"interface",
										
										"native",
										"new",
										
										"package",
										"private",
										"protected",
										"public",
										"return",
									
										"static",
										"super",
										"switch",
										"synchronized",
										"this",
										"throw",
										"throws",
										"transient",
										
										"try",
										"typeof",
										
										
										"volatile",
										"while",
										"with"};//$NON-NLS-9$//$NON-NLS-8$//$NON-NLS-7$//$NON-NLS-6$//$NON-NLS-5$//$NON-NLS-4$//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
public static String[] TYPES = {  "boolean",
									"byte",
									"char",
									"double",
									"int",
									"long",
									"short",
									"float",
									"var",
									"void"} ;//$NON-NLS-9$//$NON-NLS-8$//$NON-NLS-7$//$NON-NLS-6$//$NON-NLS-5$//$NON-NLS-4$//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
public static String[] CONSTANTS = {"false", "null", "true"};//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$

	
	

}
