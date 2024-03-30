package com.example;

import java.lang.reflect.Method;
import java.util.Map;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

public final class JsonMessageHelper {

	private static MessageJsonHandler handler;
	static {
		//Map<String, JsonRpcMethod> methods = ServiceEndpoints.getSupportedMethods(JDTLanguageServer.class);
		//handler = new MessageJsonHandler(methods);
	}

	private JsonMessageHelper() {
		//no instantiation
	}

	/**
	 * Returns the deserialized params attribute of a JSON message payload
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getParams(CharSequence jsonPayload) {
		Message message = handler.parseMessage(jsonPayload);
		Method getParam = null;
		try {
			getParam = message.getClass().getMethod("getParams");
			Object params = getParam.invoke(message);
			return (T)params;
		} catch (Exception e) {
			throw new UnsupportedOperationException("Can't deserialize into class");
		}
	}

}

