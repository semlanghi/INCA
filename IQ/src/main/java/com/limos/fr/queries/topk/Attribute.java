package com.limos.fr.queries.topk;

public class Attribute {
	
	private String name;
	private Type type;
	private String value;
	
	public Attribute() {
	}

	public Type getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public long getValueLong() throws Exception{
		if (type!=Type.long_)
			throw new Exception("Type of this attribut '"+name+"' is not long. It is <<"+type+">>");
		return Long.parseLong(value);
	}

	public int getValueInt() throws Exception{
		if (type!=Type.int_)
			throw new Exception("Type of this attribut '"+name+"' is not int. It is <<"+type+">>");
		return Integer.parseInt(value);
	}

	public float getValueFloat() throws Exception{
		if (type!=Type.float_)
			throw new Exception("Type of this attribut '"+name+"' is not float. It is <<"+type+">>");
		return Float.parseFloat(value);
	}

	public double getValueDouble() throws Exception{
		if (type!=Type.double_)
			throw new Exception("Type of this attribut '"+name+"' is not double. It is <<"+type+">>");
		return Double.parseDouble(value);
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
