package com.matthiasrothe.retrofit.jetro;

import java.io.IOException;

import net.sf.jetro.tree.JsonElement;
import net.sf.jetro.tree.builder.JsonTreeBuilder;
import okhttp3.ResponseBody;
import retrofit2.Converter;

final class JetroTreeResponseBodyConverter implements Converter<ResponseBody, JsonElement> {
	private final JsonTreeBuilder builder;

	JetroTreeResponseBodyConverter(JsonTreeBuilder builder) {
		this.builder = builder;
	}

	@Override
	public JsonElement convert(ResponseBody value) throws IOException {
		try {
			return builder.build(value.charStream());
		} finally {
			value.close();
		}
	}
}
