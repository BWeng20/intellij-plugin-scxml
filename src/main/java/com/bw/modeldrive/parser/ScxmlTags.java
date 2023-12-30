package com.bw.modeldrive.parser;

public interface ScxmlTags
{
	/**
	 * SCXML root tag.
	 */
	String TAG_SCXML = "scxml";

	/**
	 * Common attribute to specify the unique name for an element.
	 */
	String ATTR_NAME = "name";

	/**
	 * Attribute to specify the datamodel to use.
	 */
	String ATTR_DATAMODEL = "datamodel";

	/**
	 * Datamodel binding mode.
	 */
	String ATTR_BINDING = "binding";

	/**
	 * Datamodel specification.
	 */
	String TAG_DATAMODEL = "datamodel";

	/**
	 * Data specification.
	 */
	String TAG_DATA = "data";

	/**
	 * Version information.
	 */
	String TAG_VERSION = "version";

	/**
	 * Starts an initial sub-state.
	 */
	String TAG_INITIAL = "initial";

	/**
	 * Common attribute to specify an identification.
	 */
	String ATTR_ID = "id";

	/**
	 * #W3C says:
	 * <p>Holds the representation of a state.</p>
	 *
	 * <h4>3.3.1 Attribute Details</h4>
	 *
	 * <table class="plain">
	 * <caption>Attribute Details</caption><tbody>
	 * <tr><th>Name</th><th>Required</th><th>Attribute Constraints</th><th>Type</th><th>Default Value</th><th>Valid Values</th><th>Description</th></tr>
	 * <tr><td>id</td><td>false</td><td>none</td><td>ID</td><td>none</td><td>A valid id as defined in [XML Schema]</td>
	 * <td>The identifier for this state. See <em>3.14 IDs</em> for details.</td></tr>
	 * <tr><td>initial</td><td>false</td><td>MUST NOT be specified in conjunction<br>with the &lt;initial&gt; element.<br>MUST NOT occur in atomic states.</td>
	 * <td>IDREFS</td><td>none</td><td>A legal state specification.<br>See <em>3.11 Legal State Configurations<br>and Specifications</em> for details.</td>
	 * <td>The id of the default initial state<br>(or states) for this state.</td></tr>
	 * </tbody>
	 * </table>
	 *
	 * <h4>3.3.2 Children</h4>
	 *
	 * <ul><li>&lt;onentry&gt; Optional element holding executable content to be run upon entering this &lt;state&gt;. Occurs 0 or more times.
	 * See <em>3.8 &lt;onentry&gt;</em></li>
	 * <li>&lt;onexit&gt; Optional element holding executable content to
	 * be run when exiting this &lt;state&gt;. Occurs 0 or more times. See
	 * <em>3.9 &lt;onexit&gt;</em></li>
	 *
	 * <li>&lt;transition&gt; Defines an outgoing transition from this
	 * state. Occurs 0 or more times. See <em>3.5
	 * &lt;transition&gt;</em></li>
	 *
	 * <li>&lt;initial&gt; In states that have substates, an optional
	 * child which identifies the default initial state. Any transition
	 * which takes the parent state as its target will result in the state
	 * machine also taking the transition contained inside the
	 * &lt;initial&gt; element. See <em>3.6 &lt;initial&gt;</em></li>
	 *
	 * <li>&lt;state&gt; Defines a sequential substate of the parent
	 * state. Occurs 0 or more times.</li>
	 *
	 * <li>&lt;parallel&gt; Defines a parallel substate. Occurs 0 or more
	 * times. See <em>3.4
	 * &lt;parallel&gt;</em></li>
	 *
	 * <li>&lt;final&gt;. Defines a final substate. Occurs 0 or more
	 * times. See <em>3.7 &lt;final&gt;</em>.</li>
	 *
	 * <li>&lt;history&gt; A child pseudo-state which records the
	 * descendant state(s) that the parent state was in the last time the
	 * system transitioned <em>from</em> the parent. May occur 0 or more
	 * times. See <em>3.10 &lt;history&gt;</em>.</li>
	 *
	 * <li>&lt;datamodel&gt; Defines part or all of the data model. Occurs 0 or 1 times. See <em>5.2 &lt;datamodel&gt;</em></li>
	 *
	 * <li>&lt;invoke&gt; Invokes an external service. Occurs 0 or more times. See <em>6.4 &lt;invoke&gt;</em> for details.</li>
	 * </ul>
	 *
	 * <p>[Definition: An <em>atomic state</em> is a &lt;state&gt; that has no &lt;state&gt;, &lt;parallel&gt; or &lt;final&gt; children.]</p>
	 *
	 * <p>[Definition: A <em>compound state</em> is a &lt;state&gt; that has &lt;state&gt;, &lt;parallel&gt;, or
	 * &lt;final&gt; children (or a combination of these).]</p>
	 *
	 * <p>[Definition: The <em>default initial state(s)</em> of a compound state are those specified by
	 * the 'initial' attribute or &lt;initial&gt; element, if either is present. Otherwise it is the state's first child state in document
	 * order. ]</p>
	 *
	 * <p>In a conformant SCXML document, a compound state <em>MAY</em> specify either an "initial" attribute or an &lt;initial&gt; element, but
	 * not both. See <em>3.6 &lt;initial&gt;</em> for a discussion of the difference between the two notations.</p>
	 */
	String TAG_STATE = "state";

	/**
	 * Attribute of {@link #TAG_STATE &lt;state&gt;}. The initial sub-states.
	 */
	String ATTR_INITIAL = "initial";

	/**
	 * Child of {@link #TAG_STATE &lt;state&gt;}.
	 */
	String TAG_HISTORY = "history";

	/**
	 * Same as {@link #TAG_STATE &lt;state&gt;} but for parallel states.
	 */
	String TAG_PARALLEL = "parallel";

	/**
	 * Same as {@link #TAG_STATE &lt;state&gt;} but for final states.
	 */
	String TAG_FINAL = "final";

	/**
	 * Child of {@link #TAG_STATE &lt;state&gt;}.
	 */
	String TAG_TRANSITION = "transition";

	/**
	 * Starts a transition condition specification.
	 */
	String TAG_COND = "cond";

	/**
	 * Starts an event specification.
	 */
	String TAG_EVENT = "event";

	/**
	 * Starts a type specification.
	 */
	String TAG_TYPE = "type";

	/**
	 * Child of {@link #TAG_STATE &lt;state&gt;}.
	 */
	String TAG_ON_ENTRY = "onentry";

	/**
	 * Child of {@link #TAG_STATE &lt;state&gt;}.
	 */
	String TAG_ON_EXIT = "onexit";

	/**
	 * Child of {@link #TAG_STATE &lt;state&gt;}.
	 */
	String TAG_INVOKE = "invoke";
	String ATTR_SRCEXPR = "srcexpr";
	String ATTR_AUTOFORWARD = "autoforward";

	String TAG_FINALIZE = "finalize";
	String TAG_DONEDATA = "donedata";

	String TAG_INCLUDE = "include";
	String TAG_HREF = "href";
	String ATTR_PARSE = "parse";
	String ATTR_XPOINTER = "xpointer";

	/**
	 * Executable content
	 */
	String TAG_RAISE = "raise";

	/**
	 * <h4>W3C says:</h4>
	 * <h4>6.2.1 Overview</h4>
	 *
	 * <p>&lt;send&gt; is used to send events and data to external
	 * systems, including external SCXML Interpreters, or to raise events
	 * in the current SCXML session.</p>
	 *
	 * <h4>6.2.2 Attribute Details</h4>
	 *
	 * <table class="plain"><caption>attibute table</caption>
	 * <tbody>
	 * <tr> <th>Name</th> <th>Required</th> <th>Attribute Constraints</th>
	 * <th>Type</th><th>Default Value</th>
	 * <th>Valid Values</th><th>Description</th>
	 * </tr>
	 *
	 * <tr><td>event</td><td>false</td><td>Must not occur with 'eventexpr'. If the type is
	 * http://www.w3.org/TR/scxml/#SCXMLEventProcessor, either this
	 * attribute or 'eventexpr' must be present.</td><td>EventType.datatype</td>
	 * <td>none</td><td></td>
	 * <td>A string indicating the name of message being generated. See <em>E Schema</em> for details on the data type.</td>
	 * </tr>
	 *
	 * <tr> <td>eventexpr</td><td>false</td> <td>Must not occur with 'event'. If the type is
	 * http://www.w3.org/TR/scxml/#SCXMLEventProcessor, either this
	 * attribute or 'event' must be present.</td><td>Value expression</td>
	 * <td>none</td> <td></td> <td>A dynamic alternative to 'event'. If this attribute is present,
	 * the SCXML Processor <em >MUST</em> evaluate it when the parent &lt;send&gt;
	 * element is evaluated and treat the result as if it had been entered
	 * as the value of 'event'.</td>
	 * </tr>
	 *
	 * <tr><td>target</td><td>false</td><td>Must not occur with 'targetexpr'</td>
	 * <td>URI</td><td>none</td><td>A valid target URI</td>
	 * <td>The unique identifier of the message target that the platform should send the event to. See <em>6.2.4 The Target of Send</em> for details.</td>
	 * </tr>
	 *
	 * <tr><td>targetexpr</td><td>false</td><td>Must not occur with 'target'</td>
	 * <td>Value expression</td><td>none</td><td>An expression evaluating to a valid target URI</td>
	 * <td>A dynamic alternative to 'target'. If this attribute is
	 * present, the SCXML Processor <em >MUST</em> evaluate it when the parent &lt;send&gt;
	 * element is evaluated and treat the result as if it had been entered
	 * as the value of 'target'.</td></tr>
	 *
	 * <tr><td>type</td><td>false</td><td>Must not occur with 'typeexpr'</td>
	 * <td>URI</td><td>none</td><td></td><td>The URI that identifies the transport mechanism for the message.
	 * See <em>6.2.5 The Type of Send</em> for details.</td>
	 * </tr><tr>
	 * <td>typeexpr</td> <td>false</td> <td>Must not occur with 'type'</td>
	 * <td>value expression</td> <td>none</td><td></td>
	 * <td>A dynamic alternative to 'type'. If this attribute is present,
	 * the SCXML Processor <em >MUST</em> evaluate it when the parent &lt;send&gt;
	 * element is evaluated and treat the result as if it had been entered
	 * as the value of 'type'.</td>
	 * </tr><tr><td>id</td><td>false</td><td>Must not occur with 'idlocation'.</td>
	 * <td>xml:ID</td><td>none</td><td>Any valid token</td><td>A string literal to be used as the identifier for this instance
	 * of &lt;send&gt;. See <em>3.14 IDs</em> for details.</td>
	 * </tr>
	 * <tr><td>idlocation</td><td>false</td> <td>Must not occur with 'id'.</td>
	 * <td>Location expression</td><td>none</td><td>Any valid location expression</td>
	 * <td>Any location expression evaluating to a data model location in
	 * which a system-generated id can be stored. See below for details.</td>
	 * </tr>
	 * <tr><td>delay</td><td>false</td><td>Must not occur with 'delayexpr' or when the attribute 'target'
	 * has the value "_internal".</td><td>Duration.datatype</td>
	 * <td>None</td><td>A time designation as defined in CSS2 [CSS2] format</td>
	 * <td>Indicates how long the processor should wait before dispatching
	 * the message. See <em>E Schema</em> for details on the data type.</td>
	 * </tr>
	 *
	 * <tr><td>delayexpr</td><td>false</td>
	 * <td>Must not occur with 'delay' or when the attribute 'target' has
	 * the value "_internal".</td><td>Value expression</td>
	 * <td>None</td><td>A value expression which returns a time designation as defined
	 * in CSS2 [CSS2] format</td>
	 * <td>A dynamic alternative to 'delay'. If this attribute is present,
	 * the SCXML Processor <em >MUST</em> evaluate it when the parent &lt;send&gt;
	 * element is evaluated and treat the result as if it had been entered as the value of 'delay'.</td>
	 * </tr>
	 *
	 * <tr><td>namelist</td><td>false</td><td>Must not be specified in conjunction with &lt;content&gt;
	 * element.</td><td>List of location expressions</td>
	 * <td>none</td><td>List of valid location expressions</td>
	 * <td>A space-separated list of one or more data model locations to be included as attribute/value pairs with the message. (The name of
	 * the location is the attribute and the value stored at the location is the value.) See <em>5.9.2 Location Expressions</em> for details.</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * <h4>6.2.3 Children</h4>
	 *
	 * <ul>
	 * <li>&lt;param&gt;. The SCXML Processor <em >MUST</em> evaluate
	 * this element when the parent &lt;send&gt; element is evaluated and
	 * pass the resulting data to the external service when the message is
	 * delivered. Occurs 0 or more times. See <em>5.7 &lt;param&gt;</em> for details.</li>
	 *
	 * <li>&lt;content&gt;. The SCXML Processor <em >MUST</em> evaluate
	 * this element when the parent &lt;send&gt; element is evaluated and
	 * pass the resulting data to the external service when the message is
	 * delivered. Occurs 0 or 1 times. See <em>5.6 &lt;content&gt;</em> for details.</li>
	 * </ul>
	 *
	 * <p>A conformant SCXML document <em>MUST</em> specify exactly one of 'event',
	 * 'eventexpr' and &lt;content&gt;. A conformant document <em>MUST NOT</em>
	 * specify "namelist" or &lt;param&gt; with &lt;content&gt;.</p>
	 *
	 * <p>The SCXML Processor <em >MUST</em> include all attributes and values
	 * provided by &lt;param&gt; or 'namelist' even if duplicates
	 * occur.</p>
	 *
	 * <p>If 'idlocation' is present, the SCXML Processor <em >MUST</em> generate
	 * an id when the parent &lt;send&gt; element is evaluated and store
	 * it in this location. See <em>3.14 IDs</em> for details.</p>
	 *
	 * <p>If a delay is specified via 'delay' or 'delayexpr', the SCXML
	 * Processor <em >MUST</em> interpret the character string as a time
	 * interval. It <em >MUST</em> dispatch the message only when the delay
	 * interval elapses. (Note that the evaluation of the
	 * <code>send</code> tag will return immediately.) The Processor <em >MUST</em> evaluate
	 * all arguments to &lt;send&gt; when the &lt;send&gt; element is
	 * evaluated, and not when the message is actually dispatched. If the
	 * evaluation of &lt;send&gt;'s arguments produces an error, the
	 * Processor <em >MUST</em> discard the message without attempting to
	 * deliver it. If the SCXML session terminates before the delay
	 * interval has elapsed, the SCXML Processor <em >MUST</em> discard
	 * the message without attempting to deliver it.</p>
	 *
	 * <h4>6.2.4 The Target of Send</h4>
	 *
	 * <p>The target of the &lt;send&gt; operation specifies the
	 * destination of the event. The target is defined by either the 'target' or the 'targetexpr' attribute. In most cases, the format of the target depends on
	 * the type of the target (for example a SIP URL for SIP-INFO messages or a HTTP URL for Web Services). If the value of the 'target' or 'targetexpr'
	 * attribute is not supported or invalid, the Processor <em >MUST</em> place the error error.execution on the internal event queue. If it is unable
	 * to dispatch the message, the Processor <em>MUST</em> place the error error.communication on the internal event queue.</p>
	 *
	 * <h4>6.2.5 The Type of Send</h4>
	 *
	 * <p>The type of the &lt;send&gt; operation specifies the method that
	 * the SCXML processor <em >MUST</em> use to deliver the message to its target.
	 * A conformant SCXML document <em>MAY</em> use either the 'type' or the 'typeexpr'
	 * attribute to define the type. If neither the 'type' nor the
	 * 'typeexpr' is defined, the SCXML Processor <em >MUST</em> assume
	 * the default value of http://www.w3.org/TR/scxml/#SCXMLEventProcessor. If the SCXML
	 * Processor does not support the type that is specified, it <em>MUST</em> place the event error.execution on the internal event queue.</p>
	 *
	 * <p>SCXML Processors <em >MUST</em> support the following type:</p>
	 *
	 * <table class="plain"><caption></caption><tbody>
	 * <tr><th>Value</th><th>Details</th></tr>
	 * <tr><td>http://www.w3.org/TR/scxml/#SCXMLEventProcessor</td>
	 * <td>Target is an SCXML session. The transport mechanism is platform-specific.</td>
	 * </tr>
	 * </tbody></table>
	 *
	 * <p>For details on the http://www.w3.org/TR/scxml/#SCXMLEventProcessor type, see <em>C.1 SCXML Event I/O
	 * Processor</em>.</p>
	 *
	 * <p>Support for HTTP POST is optional, however Processors that
	 * support it <em>must</em> use the following value for the "type"
	 * attribute:</p>
	 *
	 * <table class="plain"><caption></caption>
	 * <tbody><tr><th>Value</th><th>Details</th></tr>
	 * <tr><td>http://www.w3.org/TR/scxml/#BasicHTTPEventProcessor</td>
	 * <td>Target is a URL. Data is sent via HTTP POST</td></tr>
	 * </tbody>
	 * </table>
	 *
	 * <p>For details on the
	 * http://www.w3.org/TR/scxml/#BasicHTTPEventProcessor type, see <em>C.2 Basic HTTP Event I/O Processor</em>.</p>
	 *
	 * <p>Processors <em>MAY</em> support other types such as web-services,
	 * SIP or basic HTTP GET. When they do so, they <em>SHOULD</em>
	 * assign such types the URI of the description of the relevant Event
	 * I/O Processor. Processors <em>MAY</em> define short form notations as an authoring convenience (e.g., "scxml" as equivalent to
	 * http://www.w3.org/TR/scxml/#SCXMLEventProcessor).</p>
	 *
	 * <h4>6.2.6 Message Content</h4>
	 *
	 * <p>The sending SCXML Interpreter <em >MUST</em> not alter
	 * the content of the &lt;send&gt; and <em >MUST</em> include
	 * it in the message that it sends to the destination specified in the
	 * target attribute of &lt;send&gt;.</p>
	 *
	 * <p>Note that the document author can specify the message content in one of two mutually exclusive ways:</p>
	 *
	 * <ul>
	 * <li>An optional 'event' attribute, combined with an optional 'namelist' attribute, combined with 0 or more &lt;param&gt;
	 * children. Here is an example using the 'event' and 'namelist' attributes:
	 *
	 * <pre>&lt;datamodel&gt;
	 * &lt;data id="target" expr="'tel:+18005551212'"/&gt;
	 * &lt;data id="content" expr="'http://www.example.com/mycontent.txt'"/&gt;
	 * &lt;/datamodel&gt;
	 *    ...
	 * &lt;send target="target" type="x-messaging" event="fax.SEND" namelist="content"/&gt;
	 * </pre></li>
	 *
	 * <li>A single &lt;content&gt; child containing inline content specifying the message body. See <em>5.6 &lt;content&gt;</em> for details.
	 *
	 * <pre>&lt;send target="csta://csta-server.example.com/" type="x-csta"&gt;
	 *       &lt;content&gt;
	 *       &lt;csta:MakeCall&gt;
	 *         &lt;csta:callingDevice&gt;22343&lt;/callingDevice&gt;
	 *         &lt;csta:calledDirectoryNumber&gt;18005551212&lt;/csta:calledDirectoryNumber&gt;
	 *       &lt;/csta:MakeCall&gt;
	 *       &lt;/content&gt;
	 * &lt;/send&gt;
	 * </pre>
	 * </li>
	 * </ul>
	 *
	 * <p>Note that the absence of any error events does not mean that the
	 * event was successfully delivered to its target, but only that the
	 * platform was able to dispatch the event.</p>
	 */
	String TAG_SEND = "send";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_EVENT = "event";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_EVENTEXPR = "eventexpr";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_TARGET = "target";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_TARGETEXPR = "targetexpr";

	/**
	 * Distinct value for {@link #ATTR_TARGET target}. See {@link #TAG_SEND &lt;send&gt;}
	 */
	String TARGET_INTERNAL = "_internal";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_TYPE = "type";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_TYPEEXPR = "typeexpr";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_IDLOCATION = "idlocation";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_DELAY = "delay";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_DELAYEXPR = "delayexpr";

	/**
	 * Attribute of {@link #TAG_SEND &lt;send&gt;}
	 */
	String ATTR_NAMELIST = "namelist";

	/**
	 * Starts a param specification.
	 */
	String TAG_PARAM = "param";

	/**
	 * Starts a content specification.
	 */
	String TAG_CONTENT = "content";

	/**
	 * Starts a log specification.
	 */
	String TAG_LOG = "log";

	/**
	 * Starts a script specification.
	 */
	String TAG_SCRIPT = "script";

	/**
	 * Src Attribute.
	 */
	String ATTR_SRC = "src";

	/**
	 * Starts an Assign specification.
	 */
	String TAG_ASSIGN = "assign";

	/**
	 * Location attribute.
	 */
	String ATTR_LOCATION = "location";

	/**
	 * Starts an If specification.
	 */
	String TAG_IF = "if";

	/**
	 * Starts a ForEach specification.
	 */
	String TAG_FOR_EACH = "foreach";

	/**
	 * Array attribute.
	 */
	String ATTR_ARRAY = "array";

	/**
	 * Item attribute.
	 */
	String ATTR_ITEM = "item";

	/**
	 * Index attribute.
	 */
	String ATTR_INDEX = "index";

	/**
	 * Starts a Cancel specification.
	 */
	String TAG_CANCEL = "cancel";

	/**
	 * SendIdExpr attribute.
	 */
	String ATTR_SENDIDEXPR = "sendidexpr";

	/**
	 * SendId attribute.
	 */
	String ATTR_SENDID = "sendid";

	/**
	 * Starts an Else case specification.
	 */
	String TAG_ELSE = "else";

	/**
	 * Starts an ElseIf specification.
	 */
	String TAG_ELSEIF = "elseif";

	/**
	 * Expression attribute.
	 */
	String ATTR_EXPR = "expr";

	/**
	 * Namespace used for include directive.
	 */
	String NS_XINCLUDE = "http://www.w3.org/2001/XInclude";

	/**
	 * Main SCXML Namespace.
	 */
	String NS_SCXML = "http://www.w3.org/2005/07/scxml";

}
