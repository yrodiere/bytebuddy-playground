package org.hibernate.bytebuddy.playground;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

public class MyClass {

	@WithBridgeMethods(value = { String.class, int.class }, adapterMethod = "longToStringOrInt")
	public long myMethod() {
		return 0L;
	}

	private Object longToStringOrInt(long value, Class type) {
		if (type == String.class)
			return String.valueOf(value);
		if (type == int.class)
			return (int) value;
		throw new AssertionError("Unexpected type: " + type);
	}

}
