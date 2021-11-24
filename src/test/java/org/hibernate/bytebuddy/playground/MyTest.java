package org.hibernate.bytebuddy.playground;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

public class MyTest {

	@Test
	void test() throws Exception {
		DynamicType.Unloaded<MyClass> unloadedTypeWithAdvice = new ByteBuddy()
				.subclass( MyClass.class )
				.name( MyClass.class.getName() + "_withAdvice" )
				.method( named( "myMethod" ) )
				.intercept( Advice.to( MyAdvice.class ) )
				.make();

		Path dir = Files.createTempDirectory( "bytebuddy-playground-generated" );
		Map<TypeDescription, File> files = unloadedTypeWithAdvice.saveIn( dir.toFile() );
		System.out.println( "Generated class " + unloadedTypeWithAdvice + " saved to " + files );

		Class<? extends MyClass> typeWithAdvice = unloadedTypeWithAdvice
				.load( getClass().getClassLoader() )
				.getLoaded();

		MyClass originalInstance = new MyClass();
		assertThat( originalInstance.myMethod() ).isEqualTo( 0 );
		MyClass instanceWithAdvice = typeWithAdvice.getConstructor().newInstance();
		assertThat( instanceWithAdvice.myMethod() ).isEqualTo( 42 );
	}

}
