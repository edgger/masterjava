package ru.javaops.masterjava.xml.util;

import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.*;


/**
 * Marshalling/Unmarshalling JAXB helper
 * XML Facade
 */
public class JaxbParser {

    protected Schema schema;
    private JAXBContext jaxbContext;

    private static JaxbUnmarshaller getJaxbUnmarshaller(JAXBContext jaxbContext, Schema schema) throws JAXBException {
        JaxbUnmarshaller jaxbUnmarshaller = new JaxbUnmarshaller(jaxbContext);
        if (schema != null) {
            jaxbUnmarshaller.setSchema(schema);
        }
        return jaxbUnmarshaller;
    }

    private static JaxbMarshaller getJaxbMarshaller(JAXBContext jaxbContext, Schema schema) throws JAXBException {
        JaxbMarshaller jaxbMarshaller = new JaxbMarshaller(jaxbContext);
        if (schema != null) {
            jaxbMarshaller.setSchema(schema);
        }
        return jaxbMarshaller;
    }

    public JaxbParser(Class... classesToBeBound) {
        try {
            jaxbContext = JAXBContext.newInstance(classesToBeBound);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //    http://stackoverflow.com/questions/30643802/what-is-jaxbcontext-newinstancestring-contextpath
    public JaxbParser(String context) {
        try {
            jaxbContext = JAXBContext.newInstance(context);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Unmarshaller
    public <T> T unmarshal(InputStream is) throws JAXBException {
        return (T) getJaxbUnmarshaller(jaxbContext, schema).unmarshal(is);
    }

    public <T> T unmarshal(Reader reader) throws JAXBException {
        return (T) getJaxbUnmarshaller(jaxbContext, schema).unmarshal(reader);
    }

    public <T> T unmarshal(String str) throws JAXBException {
        return (T) getJaxbUnmarshaller(jaxbContext, schema).unmarshal(str);
    }

    public <T> T unmarshal(XMLStreamReader reader, Class<T> elementClass) throws JAXBException {
        return getJaxbUnmarshaller(jaxbContext, schema).unmarshal(reader, elementClass);
    }

    // Marshaller
    public String marshal(Object instance) throws JAXBException {
        return getJaxbMarshaller(jaxbContext, schema).marshal(instance);
    }

    public void marshal(Object instance, Writer writer) throws JAXBException {
        getJaxbMarshaller(jaxbContext, schema).marshal(instance, writer);
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void validate(String str) throws IOException, SAXException {
        validate(new StringReader(str));
    }

    public void validate(Reader reader) throws IOException, SAXException {
        schema.newValidator().validate(new StreamSource(reader));
    }
}
