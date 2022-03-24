package org.hibernate.bytebuddy.playground;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class MyAdvice {
	@Retention(RetentionPolicy.RUNTIME)
	@interface FieldValue {

	}

	@Advice.OnMethodExit
	public static void exit(@FieldValue Object fieldValue,
			@Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object returned)
			throws Throwable {
		returned = fieldValue;
	}
}
