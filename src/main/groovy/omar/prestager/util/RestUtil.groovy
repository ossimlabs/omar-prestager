package omar.prestager.util

import groovy.transform.CompileStatic

import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Singleton

@Singleton
@CompileStatic
class RestUtil {
  Logger log = LoggerFactory.getLogger( RestUtil )

  String postToAddRaster( File imageFile ) {
    String results

    try {
      URL url = "http://${System.getenv('OMAR_STAGER_APP_SERVICE_HOST')}:${System.getenv('OMAR_STAGER_APP_SERVICE_PORT')}".toURL()
      HttpClient client = HttpClient.create( url )
      HttpRequest request = HttpRequest.create( HttpMethod.POST, "/omar-stager/dataManager/addRaster" )

      request.parameters.add( "filename", imageFile?.absolutePath )
      results = client.toBlocking().retrieve( request, String.class )
      log.info results
    } catch ( e ) {
      log.error e.message
    }

    results
  }

  String postToRemoveRaster( File imageFile ) {
    String results
    try {
      URL url = "http://localhost".toURL()
      HttpClient client = HttpClient.create( url )
      HttpRequest request = HttpRequest.create( HttpMethod.POST, "/omar-services/dataManager/removeRaster" )

      request.parameters.add( "filename", imageFile?.absolutePath )
      results = client.toBlocking().retrieve( request, String.class );
      log.info results
    } catch ( e ) {
      log.error e.message
    }

    results
  }
}
