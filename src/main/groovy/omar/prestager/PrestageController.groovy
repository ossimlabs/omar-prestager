package omar.prestager

import groovy.transform.CompileStatic
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

@CompileStatic
@Controller( "/prestage" )
class PrestageController {
  OssimService ossimService

  PrestageController( OssimService ossimService ) {
    this.ossimService = ossimService
  }

  @ExecuteOn( TaskExecutors.IO )
  @Post( uri = "/processFile", produces = "text/plain" )
  String processFile( @QueryValue String filename ) {
    ossimService.processFile( new File( filename ) )
  }

  @ExecuteOn( TaskExecutors.IO )
  @Post( uri = "/queueFile", produces = MediaType.APPLICATION_JSON )
  ImageFile queueFile(  @QueryValue String filename  ) {
    ossimService.queueFile( new ImageFile(filename: filename) )
  }
}