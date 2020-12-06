package de.eblaas.museum;

import de.eblaas.museum.Dimension.DimensionBoundary;
import de.eblaas.museum.Dimension.DimensionBoundaryFitResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/objects")
class MetObjectsApi {

    private final MetObjectService service;


    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DimensionBoundaryFitResult doesItFit(@PathVariable("id") long id, DimensionBoundary dimensionBoundary) {
        return service.doesItFit(id, dimensionBoundary);
    }


    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DimensionBoundaryFitResult> listFittingObjects(DimensionBoundary dimensionBoundary) {
        return service.listFittingObjects(dimensionBoundary);
    }
}
