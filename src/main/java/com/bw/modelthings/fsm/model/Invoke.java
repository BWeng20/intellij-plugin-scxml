package com.bw.modelthings.fsm.model;

/**
 * <b>W3C says:</b><br>
 * The &lt;invoke&gt; element is used to create an instance of an external service.
 */
public class Invoke implements FsmElement
{
	/**
	 * The unique id, counting in document order.<br>
	 */
	public int _docId;

	/**
	 * W3c says:<br>
	 * Attribute 'id':<br>
	 * A string literal to be used as the identifier for this instance of &lt;invoke&gt;. See 3.14 IDs for details.
	 */
	public String _id;

	/**
	 * W3c says:<br>
	 * Attribute 'idlocation':<br>
	 * Location expression.<br>
	 * Any data model expression evaluating to a data model location.<br>
	 * Must not occur with the 'id' attribute.
	 */
	public String _idLocation;

	/**
	 * W3c says:<br>
	 * Attribute 'type':<br>
	 * A URI specifying the type of the external service.
	 */
	public String _typeName;

	/**
	 * W3c says:<br>
	 * Attribute 'typeexpr':<br>
	 * A dynamic alternative to 'type'. If this attribute is present, the SCXML Processor must evaluate it
	 * when the parent &lt;invoke&gt; element is evaluated and treat the result as if it had been entered as
	 * the value of 'type'.
	 */
	public String _typeExpr;

	/**
	 * W3c says:<br>
	 * List of valid location expressions
	 */
	public java.util.List<String> _nameList;

	/**
	 * W3c says:<br>
	 * A URI to be passed to the external service.<br>
	 * Must not occur with the 'srcexpr' attribute or the &lt;content&gt; element.
	 */
	public String _src;

	/**
	 * W3c says:<br>
	 * A dynamic alternative to 'src'. If this attribute is present,
	 * the SCXML Processor must evaluate it when the parent &lt;invoke&gt; element is evaluated and treat the result
	 * as if it had been entered as the value of 'src'.
	 */
	public String _srcExpr;

	/**
	 * W3c says:<br>
	 * Boolean.<br>
	 * A flag indicating whether to forward events to the invoked process.
	 */
	public boolean _autoforward;

	/**
	 * content inside &lt;content&gt; child
	 */
	public String _content;

	/**
	 * expr-attribute of &lt;content&gt; child
	 */
	public String _contentExpr;

	/**
	 * W3c says:<br>
	 * Executable content to massage the data returned from the invoked component. Occurs 0 or 1 times.<br>
	 * See 6.5 &lt;finalize&gt; for details.
	 */
	public ExecutableContent _finalize;

	/**
	 * Creates a new Invoke instance.
	 */
	public Invoke()
	{
	}
}
