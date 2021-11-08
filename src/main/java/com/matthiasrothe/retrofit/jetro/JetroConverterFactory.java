package com.matthiasrothe.retrofit.jetro;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import net.sf.jetro.object.ObjectMapper;
import net.sf.jetro.object.reflect.TypeToken;
import net.sf.jetro.tree.JsonCollection;
import net.sf.jetro.tree.builder.JsonTreeBuilder;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class JetroConverterFactory extends Converter.Factory {

	/**
	 * Create an instance with a default ObjectMapper and JsonTreeBuilder.
	 */
	public static JetroConverterFactory create() {
		return create(new ObjectMapper(), new JsonTreeBuilder());
	}

	/**
	 * Create an instance using {@code mapper} for conversion.
	 */
	public static JetroConverterFactory create(ObjectMapper mapper, JsonTreeBuilder builder) {
		if (mapper == null)
			throw new NullPointerException("mapper must not be null");
		return new JetroConverterFactory(mapper, builder);
	}

	private final ObjectMapper mapper;
	private final JsonTreeBuilder builder;
	
	private JetroConverterFactory(ObjectMapper mapper, JsonTreeBuilder builder) {
		this.mapper = mapper;
		this.builder = builder;
	}
	
	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		if (type instanceof Class && JsonCollection.class.isAssignableFrom((Class<?>) type)) {
			return new JetroTreeResponseBodyConverter(builder);
		} else {
			return new JetroResponseBodyConverter<>(TypeToken.of(type));
		}
	}

	@Override
	public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
			Annotation[] methodAnnotations, Retrofit retrofit) {
		return new JetroRequestBodyConverter<>(mapper);
	}
}
