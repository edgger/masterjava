package ru.javaops.masterjava;

import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.Assert.*;

public class MainXmlTest {

    @Test
    public void main() throws IOException, JAXBException {
        String[] args = {"topjava"};
        MainXml.main(args);
    }
}