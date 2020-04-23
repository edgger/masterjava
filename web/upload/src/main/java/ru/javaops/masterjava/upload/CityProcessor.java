package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.xml.util.JaxbUnmarshaller;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class CityProcessor {
    private static CityDao cityDao = DBIProvider.getDao(CityDao.class);

    public void process(StaxStreamProcessor processor,
                        JaxbUnmarshaller unmarshaller) throws XMLStreamException, JAXBException {
        log.info("Start processing");

        List<City> chunk = new ArrayList<>();
        while (processor.startElement("City", "Cities")) {
            ru.javaops.masterjava.xml.schema.CityType xmlCity = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.CityType.class);
            final City city = new City(xmlCity.getId(), xmlCity.getValue());
            chunk.add(city);
        }

        int[] result = cityDao.insertBatch(chunk);
        long inserted = IntStream.of(result).filter(value -> value > 0).count();

        log.info("End processing, inserted " + inserted);
    }

}
