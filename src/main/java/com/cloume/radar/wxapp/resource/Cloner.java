package com.cloume.radar.wxapp.resource;

import java.lang.reflect.Field;

public class Cloner<T>{
	private T object;
	
	public Cloner(T object){
		this.object = object;
	}
	
	public T clone(T from){
		for(Field field : from.getClass().getDeclaredFields()){
			Object value = null;
			
			boolean accessible = field.isAccessible();
			try {
				field.setAccessible(true);
				value = field.get(from);
				field.set(object, value);
				field.setAccessible(accessible);
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
				System.err.println(String.format("can not set field %s to %s", field.getName(), value));
			}
		}
		
		return object;
	}
}
