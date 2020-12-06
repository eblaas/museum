package de.eblaas.museum;

import com.opencsv.bean.CsvToBeanBuilder;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;

import static de.eblaas.museum.MetObjectService.BATCH_SIZE;
import static org.apache.logging.log4j.util.Strings.isNotBlank;


@Slf4j
@RequiredArgsConstructor
@Component
@Profile("import")
public class FileImportDatasource {

    private static String MISSING_DIM = "Dimensions unavailable";
    private static String MISSING_DIM2 = "Dimension unavailable";

    private final Resource resource;
    private final MetObjectService service;

    @Value("${import.size}")
    private long importSize;

    /**
     * Stream file content line by line
     */
    private Flowable<MetObject> streamFileContent(Resource resource) {

        return Flowable.using(
                () -> new BufferedReader(new FileReader(resource.getFile())),
                reader -> Flowable.fromIterable(() -> new CsvToBeanBuilder<MetObject>(reader)
                        .withType(MetObject.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build().iterator()),
                BufferedReader::close
        );
    }


    @PostConstruct
    public void importDate() {
        log.info("Start importing data ...");

        if (!resource.exists()) {
            log.error("Import file not found. Import failed. path={}", resource.getDescription());
            return;
        }

        service.initDb();

        Flowable.just(resource)
                // start import in a dedicated thread
                .observeOn(Schedulers.io())
                .flatMap(this::streamFileContent)
                // filter empty dimension strings
                .filter(obj -> isNotBlank(obj.getDimensionRaw()))
                // filter known missing dimension strings
                .filter(obj -> !obj.getDimensionRaw().equals(MISSING_DIM) && !obj.getDimensionRaw().equals(MISSING_DIM2))
                // only objects with parsable dimensions get stored in DB
                .filter(obj -> obj.validDimension())
                // limit import size if activated
                .take(importSize > 0 ? importSize : Long.MAX_VALUE)
                // create batches of objects for batch DB insert
                .buffer(BATCH_SIZE)
                .doOnComplete(() -> log.info("Import finished."))
                // insert batches to database
                .forEach(service::batchInsert);
    }


}

