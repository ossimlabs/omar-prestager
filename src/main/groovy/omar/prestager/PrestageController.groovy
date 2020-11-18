package omar.prestager

import groovy.transform.CompileStatic
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;

@CompileStatic
@Controller( "/prestage" )
class PrestageController {
  OssimService ossimService

  PrestageController( OssimService ossimService ) {
    this.ossimService = ossimService
  }

  @ExecuteOn( TaskExecutors.IO )
  @Post( uri = "/processFile", produces = "text/plain" )
  @Operation(summary = "A queued file will have a histogram created via ossim-img2rr and have its state changed in the queue table.",
    description = "Grabs 'QUEUED' file and changes the state to 'STAGING'. A histogram will be created and stored as an internal file as a '.his' extension. Upon completiong the state state in the database will be set to 'FAILED_HISTOGRAM' or 'READY_TO_INDEX'. If the file is 'READY_TO_INDEX', it will be posted to the STAGER APP. Afterward the file will be marked as completed upon success or FAILED_POST."
      )
  @Tag(name = "processFile")
  @ApiResponse(responseCode = "400", description = "Invalid file supplied")
  @ApiResponse(responseCode = "404", description = "File not found")
  @ApiResponse(responseCode = "200", description = "Jobs Done.")
  @ApiResponse(responseCode = "503", description = "Out to lunch.")
  @ApiResponse(responseCode = "417", description = "Garbage in, Garbage Out.")
  String processFile( @QueryValue String filename ) {
    ossimService.processFile( new File( filename ) )
  }
  
  
  @ExecuteOn( TaskExecutors.IO )
  @Post( uri = "/queueFile", produces = MediaType.APPLICATION_JSON )
  @Operation(summary = "A provided file will be entered into the prestager queue.",
    description = " The file to sent to the prestager will have a entry created in the database and be flagged in the database table queue as 'QUEUED'."
    )
  @Tag(name = "queueFile")
  @ApiResponse(responseCode = "400", description = "Invalid file supplied.")
  @ApiResponse(responseCode = "404", description = "File not found.")
  @ApiResponse(responseCode = "200", description = "Jobs Done.")
  @ApiResponse(responseCode = "503", description = "Out to lunch.")
  @ApiResponse(responseCode = "417", description = "Garbage in, garbage out.")
  ImageFile queueFile(  @QueryValue String filename  ) {
    ossimService.queueFile( new ImageFile(filename: filename) )
  }
  
}
