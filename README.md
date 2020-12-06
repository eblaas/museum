# MET object service
The service provides an API for checking if a MET object fits into specified boundaries.


The MET object size (represented as a raw text string in the dataset) is mapped into a 4 dimensional space 
[height, width, depth, weight]. 

> Note: not every object has `all` dimensions filled. 

Objects with:
* diameter are mapped to [diam, diam, -1, -1]
* single size are mapped to [size, size, -1, -1], consider orientation of object
* H x W mapped to [height, width, -1, -1]
* H x W x D mapped to [height, width, depth, -1]
* a weight are mapped to [_, _, _, weight]
* several items are mapped to max value of each dimension [maxHeight, maxWidth, maxDepth, maxWeight]

Dimension values are normalized to default units:
* cm -> height, width, depth
* g  -> weight

#### Object fitting
The required size of a fitting object can be specified by dimension boundaries represented by upper and lower 
boundaries for each dimension:
 * lower boundaries [minHeight, minWidth, minDepth, minWeight], 
 * upper boundaries [maxHeight, maxWidth, maxDepth, maxWeight]

> Note: not all dimension boundaries need to be defined, only required ones 

#### Fitting behavior 

* lower boundaries: the objects `only` fits if the dimension is available (Why: to define queries that require a three-dimensional object e.g. sculpture)
* upper boundaries: the objects also fits if the dimension is unavailable but never greater than the defined size


#### Storage

Imported objects are stored in a database because the selected model for dimensions is a good fit for database tables.
Each dimension is stored as a separate column. 

The following indices are created:
* primary key on the object id (Why: fetch objects by id efficiently, important for doesItFit(id, dimension)) 
* combined index (height, width, depth, weight) (Why: list objects fitting the dimension boundaries, index column 
order is defined by importance of a dimension)
 
Why a database:
* decent dataset size: 500k records can be queried efficiently
* persistence: service can be restarted without re-importing data
* availability: multiple instances of the service can be started and use the same DB (rolling updates) 
* out-of-the-box support for queries like: give me objects fitting the dimension boundaries (not only for single id)

#### HTTP REST API

The service provides two HTTP endpoints for retrieving fitting objects:
* GET /objects/{id}?minHeight=50&minWidth=20` check if object id fits the dimension boundaries
* GET /objects/?minHeight=50&minWidth=20` list objects fitting the dimension boundaries

Why REST API:
* common standard
* ease to deploy via docker container
* flexibility, every language supports it

### API Documentation
[Swagger](http://localhost:8080/swagger-ui.html "http://localhost:8080/swagger-ui.html")

### Run application

```shell script
# docker image is available on docker hub, including test data, be patient with download :)
docker run --rm -p 8080:8080 -e IMPORT_SIZE=30000 eblaas:museum

# check if object with id 2034 fits, positive fit
curl -X GET "http://localhost:8080/api/v1/objects/2034?maxHeight=90&maxWidth=140.5" | jq

# check if object with id 2034 fits, negative fit
curl -X GET "http://localhost:8080/api/v1/objects/2034?maxHeight=50&maxWidth=140.5" | jq

# list objects
curl -X GET "http://localhost:8080/api/v1/objects/?minDepth=5&minHeight=5&minWeight=5&minWidth=5&maxHeight=35"  | jq
```
