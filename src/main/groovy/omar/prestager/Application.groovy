package omar.prestager

import io.micronaut.runtime.Micronaut
import groovy.transform.CompileStatic
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = @Info(
        title = "omar-prestager",
        version = "0.1",
        description = "Swagger API for omar prestager.\n This API contians two functions: processFile and queueFile. The queueFile function adds a file to queue and processFile takes a file from the queue adds infomation to the database and sends a file to omar-stager."
    )
)
@CompileStatic
class Application {
    static void main(String[] args) {
        Micronaut.run(Application, args)
    }
}
