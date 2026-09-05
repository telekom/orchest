package de.telekom.solutions.kmsclient.api.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.telekom.solutions.kmsclient.api.json.deserializers.AnnotationCipherDeserializer;
import de.telekom.solutions.kmsclient.api.json.serializers.AnnotationCipherSerializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = AnnotationCipherSerializer.class)
@JsonDeserialize(using = AnnotationCipherDeserializer.class)
public @interface Cipher {}
