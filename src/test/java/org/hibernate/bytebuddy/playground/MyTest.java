package org.hibernate.bytebuddy.playground;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;

public class MyTest {

	@Test
	void test() throws Exception {
		DynamicType.Unloaded<MyClass> unloadedTypeWithAdvice = new ByteBuddy()
				// Apply suggestion from https://github.com/raphw/byte-buddy/issues/999#issuecomment-759773044
				// This does not solve the problem, unfortunately.
				.with( MethodGraph.Compiler.Default.forJVMHierarchy() )
				.subclass( MyClass.class )
				.name( MyClass.class.getName() + "_withAdvice" )
				.method( named( "myMethod" ) )
				.intercept( Advice.to( MyAdvice.class ) )
				.make();

		Path dir = Files.createTempDirectory( "bytebuddy-playground-generated" );
		Map<TypeDescription, File> files = unloadedTypeWithAdvice.saveIn( dir.toFile() );
		System.out.println( "Generated class " + unloadedTypeWithAdvice + " saved to " + files );

		Class<? extends MyClass> typeWithAdvice = unloadedTypeWithAdvice
				.load( MyClass.class.getClassLoader() )
				.getLoaded();

		MyClass originalInstance = new MyClass();
		assertThat( originalInstance.myMethod() ).isEqualTo( 0L );
		assertThat( myMethodWithResultType( String.class ).invoke( originalInstance ) ).isEqualTo( "0" );
		assertThat( myMethodWithResultType( int.class ).invoke( originalInstance ) ).isEqualTo( 0 );
		MyClass instanceWithAdvice = typeWithAdvice.getDeclaredConstructor().newInstance();
		assertThat( instanceWithAdvice.myMethod() ).isEqualTo( 42L );
		assertThat( myMethodWithResultType( String.class ).invoke( instanceWithAdvice ) ).isEqualTo( "42" );
		assertThat( myMethodWithResultType( int.class ).invoke( instanceWithAdvice ) ).isEqualTo( 42 );
	}

	private Method myMethodWithResultType(Class<?> returnType) {
		for ( Method method : MyClass.class.getMethods() ) {
			if ( "myMethod".equals( method.getName() ) && returnType.equals( method.getReturnType() ) ) {
				return method;
			}
		}
		throw new IllegalStateException( "Cannot find method 'myMethod' with return type " + returnType );
	}

}
