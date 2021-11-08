package com.matthiasrothe.retrofit.jetro;

import java.io.IOException;

import net.sf.jetro.object.ObjectMapper;
import net.sf.jetro.tree.JsonCollection;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class JetroRequestBodyConverter<T> implements Converter<T, RequestBody> {
	private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
	
	private final ObjectMapper mapper;
	
	JetroRequestBodyConverter(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public RequestBody convert(T value) throws IOException {
		if (value instanceof JsonCollection) {
			return RequestBody.create(MEDIA_TYPE, ((JsonCollection) value).toJson());
		} else {
			return RequestBody.create(MEDIA_TYPE, mapper.toJson(value));
		}
	}
}
