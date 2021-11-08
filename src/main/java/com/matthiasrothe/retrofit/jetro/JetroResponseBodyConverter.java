package com.matthiasrothe.retrofit.jetro;

import java.io.IOException;

import net.sf.jetro.object.deserializer.DeserializationContext;
import net.sf.jetro.object.reflect.TypeToken;
import net.sf.jetro.object.visitor.ObjectBuildingVisitor;
import net.sf.jetro.stream.JsonReader;
import net.sf.jetro.stream.visitor.StreamVisitingReader;
import okhttp3.ResponseBody;
import retrofit2.Converter;

final class JetroResponseBodyConverter<T> implements Converter<ResponseBody, T> {
	private final TypeToken<T> targetTypeToken;

	JetroResponseBodyConverter(TypeToken<T> targetTypeToken) {
		this.targetTypeToken = targetTypeToken;
	}

	@Override
	public T convert(ResponseBody value) throws IOException {
		try (StreamVisitingReader reader = new StreamVisitingReader(new JsonReader(value.charStream()))) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			ObjectBuildingVisitor visitor = new ObjectBuildingVisitor(targetTypeToken,
					new DeserializationContext());
			
			reader.accept(visitor);
			return targetTypeToken.getRawType().cast(visitor.getVisitingResult());
		} finally {
			value.close();
		}
	}
}
