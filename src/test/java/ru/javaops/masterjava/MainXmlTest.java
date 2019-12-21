package ru.javaops.masterjava;

import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.junit.Assert.*;

public class MainXmlTest {

    @Test
    public void main() throws IOException, JAXBException, XMLStreamException {
        String[] args = {"topjava"};
        MainXml.main(args);
    }
}