package omar.prestager

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.micronaut.scheduling.annotation.Scheduled

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.LocalDateTime
import javax.inject.Singleton
import javax.transaction.Transactional

@CompileStatic
@Singleton
class OssimService {
  Logger log = LoggerFactory.getLogger( OssimService )

  File scratchDir = new File( '/scratch' )
  ImageFileRepository imageFileRepository

  @Value( '${omar.prestager.index.stagerAddress}' )
  String stagerAddress

  URL stagerUrl
  HttpClient httpClient

  OssimService( ImageFileRepository imageFileRepository ) {
    this.imageFileRepository = imageFileRepository
  }

  String processFile( File file ) {
    def workDir = new File( scratchDir, file.parent )

    workDir?.mkdirs()

    def cmd = [
        'ossim-img2rr',
        '-d', workDir,
        '--create-histogram-fast',
        file
    ]

    def stdout = new StringBuilder()
    def stderr = new StringBuilder()
    def process = cmd.execute()

    process.consumeProcessOutput( stdout, stderr )

    def exitCode = process.waitFor()
    def results

    if ( exitCode ) {
      results = stderr.toString()
    } else {
      results = stdout.toString()

      [ 'ovr', 'his' ].each { ext ->
        def baseName = FilenameUtils.getBaseName( file.absolutePath )
        def srcFile = new File( workDir, "${ baseName }.${ ext }" )
        def dstFile = new File( file.parentFile, "${ baseName }.${ ext }" )

        if ( srcFile.exists() ) {
          FileUtils.moveFile( srcFile, dstFile )
        }
      }
    }

    FileUtils.deleteDirectory( workDir )

    results
    return exitCode
  }

  @Transactional
  ImageFile queueFile( ImageFile imageFile ) {
    imageFile.insertDate = LocalDateTime.now()
    imageFile.status = ImageFile.FileStatus.QUEUED
    imageFileRepository.save( imageFile )
  }

  @Scheduled( fixedRate = '${omar.prestager.process.pollEvery}' )
  void findWork() {
    ImageFile imageFile = imageFileRepository.findByStatusEquals( ImageFile.FileStatus.QUEUED.toString() ).orElse( null )

    if ( imageFile ) {
      imageFile.status = ImageFile.FileStatus.STAGING
      updateImageFile( imageFile )

      def processStatus = processFile( imageFile.filename as File )
      
      if (processStatus == 0) {
        imageFile.status = ImageFile.FileStatus.READY_TO_INDEX
      }else{
        imageFile.status = ImageFile.FileStatus.FAILED_HISTOGRAM
      }
      updateImageFile( imageFile )
    }
  }

  @Transactional
  private ImageFile updateImageFile( ImageFile imageFile ) {
    imageFileRepository.update( imageFile )
  }

  @Scheduled( cron = '${omar.prestager.index.cron}' )
  void indexImage() {
    ImageFile imageFile = imageFileRepository.findByStatusEquals( ImageFile.FileStatus.READY_TO_INDEX.toString() ).orElse( null )

    while ( imageFile ) {
      if (imageFile.status == ImageFile.FileStatus.READY_TO_INDEX){
        imageFile.status = ImageFile.FileStatus.INDEXING
        updateImageFile( imageFile )

        try {
          def response = httpClient?.toBlocking()?.exchange( HttpRequest.POST( stagerUrl?.path,
              [ filename: imageFile?.filename ] ), String )

          log.info response?.body?.get()
        } catch ( e ) {
          log.error e.message
        }

        imageFile.status = ImageFile.FileStatus.COMPLETE
        updateImageFile( imageFile )

        imageFile = imageFileRepository.findByStatusEquals( ImageFile.FileStatus.READY_TO_INDEX.toString() ).orElse( null )
      }
    }
  }

  @EventListener
  void onStartup( ServerStartupEvent event ) {
    if ( !stagerAddress?.endsWith( '/dataManager/addRaster' ) ) {
      stagerUrl = new URL( "${ stagerAddress }/dataManager/addRaster" )
    } else {
      stagerUrl = new URL( stagerAddress )
    }

    httpClient = HttpClient.create( new URL( stagerUrl.toString() - stagerUrl?.path ) )

    log.info "stagerAddress = ${stagerAddress}"

  }
}