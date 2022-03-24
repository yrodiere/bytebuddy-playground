package org.hibernate.bytebuddy.playground;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.hibernate.bytebuddy.playground.differentpackage.MySuperClass;

import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

public class MyTest {

	@Test
	void test() throws Exception {
		DynamicType.Unloaded<MyClass> unloadedTypeWithAdvice = new ByteBuddy()
				.subclass( MyClass.class )
				.name( MyClass.class.getName() + "_withAdvice" )
				.method( named( "myMethod" ) )
				.intercept( Advice.withCustomMapping()
						.bind( MyAdvice.FieldValue.class, MySuperClass.class.getDeclaredField( "myField" ) )
						.to( MyAdvice.class ) )
				.make();

		Path dir = Files.createTempDirectory( "bytebuddy-playground-generated" );
		Map<TypeDescription, File> files = unloadedTypeWithAdvice.saveIn( dir.toFile() );
		System.out.println( "Generated class " + unloadedTypeWithAdvice + " saved to " + files );

		Class<? extends MyClass> typeWithAdvice = unloadedTypeWithAdvice
				.load( MyClass.class.getClassLoader() )
				.getLoaded();

		MyClass originalInstance = new MyClass();
		assertThat( originalInstance.myMethod() ).isEqualTo( 0 );
		originalInstance.setMyField( 42 );
		assertThat( originalInstance.myMethod() ).isEqualTo( 0 );

		MyClass instanceWithAdvice = typeWithAdvice.getDeclaredConstructor().newInstance();
		assertThat( originalInstance.myMethod() ).isEqualTo( 0 );
		instanceWithAdvice.setMyField( 42 );
		assertThat( instanceWithAdvice.myMethod() ).isEqualTo( 42 );
	}

}
