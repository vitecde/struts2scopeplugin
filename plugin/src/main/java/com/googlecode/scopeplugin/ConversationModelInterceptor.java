package com.googlecode.scopeplugin;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.interceptor.ScopeInterceptor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/** 
 * <!-- START SNIPPET: description -->
 *
 * An interceptor that implements a conversation scope. Allowing a user to have multiple conversations
 * of the same type in process at the same time. This avoids the collision that would take place if 
 * you used a session scoped "model" (like Struts 2) or session scope form (like Struts 1).
 *
 * This borrows a lot from the ModelDriven concept. Except that it implements a means of
 * transparently swapping the "conversationModel" into and out of the Action. It is highly advised that 
 * you use a JavaBean to back your conversation model and not a Map. Using a Map can result in strange
 * behaviour due to OGNL setting properties on the Map from the users request, but not performing any
 * type conversion. Using a JavaBean avoids this confusion and will force OGNL to do type conversion,
 * which is what you generally want.
 *
 * It is also similar to ScopeInterceptor, except that it allows mutiple models serving the same purpose
 * to be stored in the user's session. It is also simpler because you do not need to specify the name
 * of the model, nor its class. In this sense it is far more like ModelDriven.
 *
 * <!-- END SNIPPET: description -->
 *
 * <p/> <u>Interceptor parameters:</u>
 *
 * <!-- START SNIPPET: parameters -->
 *
 * <ul>
 *
 * <li>None</li>
 *
 * </ul>
 *
 * <!-- END SNIPPET: parameters -->
 *
 * <p/> <u>Extending the interceptor:</u>
 *
 * <p/>
 *
 * <!-- START SNIPPET: extending -->
 *
 * There are no known extension points for this interceptor.
 *
 * <!-- END SNIPPET: extending -->
 *
 * <p/> <u>Example code:</u>
 *
 * <pre>
 * <!-- START SNIPPET: example -->
 * &lt;action name="someAction" class="com.examples.SomeAction"&gt;
 *     &lt;interceptor-ref name="conversation-interceptor"/&gt;
 *     &lt;interceptor-ref name="basicStack"/&gt;
 *     &lt;result name="success"&gt;good_result.ftl&lt;/result&gt;
 * &lt;/action&gt;
 * </pre>
 *
 * In your view you need to have a hidden element named "S2_CONVERSATION_ID". This id is 
 * set on the value stack under #s2cmodelid. 
 *
 * <pre>
 * &lt;s:hidden name="S2_CONVERSATION_ID" value="%{#s2cmodelid}" / &gt;
 * </pre>
 *
 * Additionally the conversation model is accessible using #s2cmodel. 
 *
 * <pre>
 * &lt;s:property value="#s2cmodel.someProperty" / &gt;
 * </pre>
 *
 *
 * <!-- END SNIPPET: example -->
 *
 * @see ModelDriven
 * @see ScopeInterceptor
 */

public class ConversationModelInterceptor extends AbstractInterceptor implements StrutsStatics {

	private static final String CONVERSATION_COUNT = "S2_CONVERSATION_COUNT";
	private int conversationCount = 0;
	
	public ConversationModelInterceptor () {
		super ();
	}

	public String intercept (ActionInvocation invocation) throws Exception {
		String result = null;
		final Object action = invocation.getAction ();

		if (action instanceof ConversationModelDriven) {
			final ActionContext context = invocation.getInvocationContext ();
			ConversationModelDriven cAction = (ConversationModelDriven) action;
			HttpServletRequest request = (HttpServletRequest) context.get(HTTP_REQUEST);
			HttpSession session =  request.getSession (true);

			///////////////////////////////
			// Before action invocation. //
			///////////////////////////////

			// Is there a S2_CONVERSATION_SCOPE Map in the session. If not put one there.
			Map conversationMap = (Map) session.getAttribute (ConversationModelDriven.S2_CONVERSATION_SCOPE);
			if (conversationMap == null) {
				conversationMap = new HashMap ();
				session.setAttribute (ConversationModelDriven.S2_CONVERSATION_SCOPE, conversationMap);
			}

			// Get the conversation scope id from the HttpServletRequest.
			String conversationId = request.getParameter (ConversationModelDriven.S2_CONVERSATION_ID);
			// System.out.println ("conversationId = '" + conversationId + "'");
			if (StringUtils.isEmpty (conversationId) ) {
				// We need to generate a conversationId
				conversationId = getConversationId (session);
			}
			// System.out.println ("conversationId = '" + conversationId + "'");

			// Set the conversionId on the action.
			cAction.setConversationId (conversationId);

			// Get the conversationModel out of the conversationMap based on the id.
			Object conversationModel = conversationMap.get (conversationId);

			// If the conversationModel is null (ie. it's not in the conversationMap) then this is the start of a conversaion,
			// and we need to prepare the conversationModel and save it in the conversationMap.
			if (conversationModel == null) {
				conversationModel = cAction.prepareModel (); // The action needs to maintain a copy of the model after this call, therefore we don't set it on the action.
				conversationMap.put (conversationId, conversationModel);
			} else {
				// The conversationModel was in the session. We need to set it on the action.
				cAction.setModel (conversationModel);
			}

			// Now push the conversationModel onto the top of the value stack.
			context.getValueStack().push (conversationModel);

			///////////////////////////////////////////////////////////
			// Invoke the next interceptor in the interceptor stack. //
			///////////////////////////////////////////////////////////
			result = invocation.invoke ();

			//////////////////////////////
			// After action invocation. //
			//////////////////////////////
			
			// If the conversation is done remove it from the ConversationScope.
			if (cAction.isConversationFinished () ) {
				conversationMap.remove (conversationId);
			}
		} else {
			result = invocation.invoke ();
		}
		return result;
	}

	private synchronized String getConversationId (HttpSession session) {
		/* 
		// If the conversationCount is not incrementing it could be caused by a bug in the S2 that causes interceptors to be reinstantiated. If that happens uncomment 
		// this and comment the current method out, rebuild and run.
		Integer conversationCount = (Integer) session.getServletContext().getAttribute (CONVERSATION_COUNT);
		if (conversationCount == null) {
			conversationCount = 0;
		}
		conversationCount = conversationCount.intValue() + 1;
		session.getServletContext().setAttribute (CONVERSATION_COUNT, conversationCount);
		return "CID-" + conversationCount + "-" + System.currentTimeMillis ();
		*/
		return "CID-" + this.conversationCount++ + "-" + System.currentTimeMillis ();
	}
}
