package org.hibernate.bytebuddy.playground;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class MyAdvice {
	@Advice.OnMethodEnter(inline = false)
	public static Callable<?> enter(@Advice.Origin Method origin) {
		return new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				Class<?> returnType = origin.getReturnType();
				if ( String.class.equals( returnType ) ) {
					return "42";
				}
				else if ( long.class.equals( returnType ) ) {
					return 42L;
				}
				else if ( int.class.equals( returnType ) ) {
					return 42;
				}
				throw new IllegalStateException( "Unsupported return type: " + returnType );
			}
		};
	}

	@Advice.OnMethodExit
	public static void exit(
			@Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object returned,
			@Advice.Enter Callable<?> mocked)
			throws Throwable {
		returned = mocked.call();
	}
}
