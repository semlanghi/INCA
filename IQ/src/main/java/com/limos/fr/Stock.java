package com.limos.fr;

import org.apache.kafka.common.serialization.Serde;

public class Stock implements TimestampedEvent {
    Character name;
    Double value;
    Long ts;

    public Stock(Character name, Double value, Long ts) {
        this.name = name;
        this.value = value;
        this.ts = ts;
    }

    public Character getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }

    public long getTs() {
        return ts;
    }

    public static void main(String[] args){
        Stock stock = new Stock('A', 12.3, 12L);
        Stock stock1 = new Stock('G', 27.3, 13L);
        Stock stock2 = new Stock('G', 56.4, 27L);

        Serde<Stock> serde = StockSerde.instance();

        byte[] stockByte = serde.serializer().serialize("csbdjc", stock);
        byte[] stock1Byte = serde.serializer().serialize("csbdjc", stock1);
        byte[] stock2Byte = serde.serializer().serialize("csbdjc", stock2);

        System.out.println(serde.deserializer().deserialize("csbdjc", stockByte));
        System.out.println(serde.deserializer().deserialize("csbdjc", stock1Byte));
        System.out.println(serde.deserializer().deserialize("csbdjc", stock2Byte));

    }

    @Override
    public String toString() {
        return "Stock{" +
                "name=" + name +
                ", value=" + value +
                ", ts=" + ts +
                '}';
    }
}
