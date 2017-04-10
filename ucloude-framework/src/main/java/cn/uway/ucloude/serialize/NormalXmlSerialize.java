package cn.uway.ucloude.serialize;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class NormalXmlSerialize implements IXmlSerialize {

	@Override
	public <T> T deSerialize(String xml, Class<T> sourceClass) {
		try {
			JAXBContext context = JAXBContext.newInstance(sourceClass);
			StringReader reader = new StringReader(xml);
			return (T) context.createUnmarshaller().unmarshal(reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String serialize(Object obj) {
		try {
			JAXBContext context = JAXBContext.newInstance(obj.getClass());
			Marshaller marshaller = context.createMarshaller();
			StringWriter writer = new StringWriter();
			marshaller.marshal(obj, writer);
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
