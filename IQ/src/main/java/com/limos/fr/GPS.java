package com.limos.fr;

import org.apache.kafka.common.serialization.Serde;

public class GPS implements TimestampedEvent{
    double x;
    double y;
    long ts;

    public GPS(double x, double y, long ts) {
        this.x = x;
        this.y = y;
        this.ts = ts;
    }


    public String key() {
        return "key";
    }

    public long getTs() {
        return ts;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static void main(String[] args){
        GPS GPS = new GPS(120030L, 12345L, 5);
        GPS GPS1 = new GPS(120030L, 12345L, 56);
        GPS GPS2 = new GPS(120030L, 12345L, 5);

        Serde<GPS> serde = GPSSerde.instance();

        byte[] GPSByte = serde.serializer().serialize("csbdjc", GPS);
        byte[] GPS1Byte = serde.serializer().serialize("csbdjc", GPS1);
        byte[] GPS2Byte = serde.serializer().serialize("csbdjc", GPS2);

        System.out.println(serde.deserializer().deserialize("csbdjc", GPSByte));
        System.out.println(serde.deserializer().deserialize("csbdjc", GPS1Byte));
        System.out.println(serde.deserializer().deserialize("csbdjc", GPS2Byte));

    }

    @Override
    public String toString() {
        return "GPS{" +
                "x=" + x +
                ", y=" + y +
                ", ts=" + ts +
                '}';
    }
}
