package omar.prestager.services

import geoscript.feature.Feature
import geoscript.geom.Geometry
import geoscript.geom.MultiPolygon
import geoscript.layer.Shapefile
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.micronaut.scheduling.annotation.Scheduled
import omar.prestager.domain.ImageZipFile
import omar.prestager.domain.ImageZipFileRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.transaction.Transactional
import java.time.LocalDateTime

import static groovyx.gpars.GParsPool.*

import omar.prestager.util.GdalUtil
import omar.prestager.util.OssimUtil
import omar.prestager.util.RestUtil
import omar.prestager.util.ZipUtil
import org.apache.commons.io.FilenameUtils

import javax.inject.Singleton

@Singleton
@CompileStatic
class IngestService {
  ImageZipFileRepository imageZipFileRepository
  RestUtil restUtil
  OssimUtil ossimUtil
  GdalUtil gdalUtil
  ZipUtil zipUtil

  Logger log = LoggerFactory.getLogger( IngestService )

  IngestService( ImageZipFileRepository imageZipFileRepository,
                 RestUtil restUtil, OssimUtil ossimUtil, GdalUtil gdalUtil, ZipUtil zipUtil ) {

    this.imageZipFileRepository = imageZipFileRepository
    this.restUtil = restUtil
    this.ossimUtil = ossimUtil
    this.gdalUtil = gdalUtil
    this.zipUtil = zipUtil
  }

  @Transactional
  ImageZipFile queueZipFile( ImageZipFile imageZipFile ) {
    imageZipFile.insertDate = LocalDateTime.now()
    imageZipFile.status = ImageZipFile.FileStatus.QUEUED
    log.info "Status: ${ imageZipFile.status }"
    imageZipFileRepository.save( imageZipFile )
  }

  @CompileDynamic
  @Scheduled( fixedRate = '${omar.prestager.process.pollEvery}' )
  void findWork() {
    ImageZipFile imageZipFile = imageZipFileRepository.findByStatusEquals(
        ImageZipFile.FileStatus.QUEUED.toString() ).orElse( null )

    if ( imageZipFile ) {
      imageZipFile.status = ImageZipFile.FileStatus.UNZIPPING
      updateImageZipFile( imageZipFile )
      log.info "Status: ${ imageZipFile.status }"

      String baseDir = "/3pa-skysat"
      File archiveDir = "${ baseDir }/archive" as File
      long start = System.currentTimeMillis()

      imageZipFile.status = ImageZipFile.FileStatus.PROCESSING_IMAGES
      updateImageZipFile( imageZipFile )
      log.info "Status: ${ imageZipFile.status }"

      File imageDir = zipUtil.unzipFile( imageZipFile.filename as File, archiveDir )
      List<File> ntfFiles = [ ]

      imageDir.eachFileMatch( ~/.*ntf/ ) { File ntfFile -> ntfFiles << ntfFile }

      withPool {
        ntfFiles.eachParallel { ntfFile ->
          File omdFile = "${ FilenameUtils.removeExtension( ntfFile.absolutePath ) }.omd" as File

          ossimUtil?.createOverviews( ntfFile )

          File thumbnail = ossimUtil?.createThumbnail( ntfFile )

          gdalUtil?.setNullValues( thumbnail )

          File mask = gdalUtil?.createMask( thumbnail )
          File shpFile = gdalUtil?.createShapeFile( mask )

          def layer = new Shapefile( shpFile )

          def geom = new MultiPolygon(
              Geometry.cascadedUnion( layer.collectFromFeature { Feature f -> f?.geom } )?.simplify( 0.01 )
          )


          thumbnail.delete()

          omdFile.withWriter { out ->
            out.println "mission_id: SkySat"
            out.println "ground_geom_0: ${ geom }"
          }

          restUtil?.postToAddRaster( ntfFile )
        }
      }

      long stop = System.currentTimeMillis()

      imageZipFile.status = ImageZipFile.FileStatus.COMPLETE
      updateImageZipFile( imageZipFile )
      log.info "Status: ${ imageZipFile.status } ${ stop - start }"
    }
  }

  @Transactional
  ImageZipFile updateImageZipFile( ImageZipFile imageZipFile ) {
    imageZipFileRepository.update( imageZipFile )
  }


  @CompileDynamic
  def ingest() {
    String baseDir = "${ System.getenv( 'OSSIM_DATA' ) }/omar-ingest"
    File zipFile = "${ baseDir }/ingest/PLANET_b8ca900c-ccdc-46dc-a53b-2f595ecaca36.zip" as File
    File archiveDir = "${ baseDir }/archive" as File

    long start = System.currentTimeMillis()

    File imageDir = zipUtil?.unzipFile( zipFile, archiveDir )
    //def imageDir = new File(archiveDir, getPrefix(zipFile))

    List<File> ntfFiles = [ ]

    imageDir.eachFileMatch( ~/.*ntf/ ) { File ntfFile -> ntfFiles << ntfFile }

    withPool {
      ntfFiles.eachParallel { ntfFile ->

        restUtil?.postToRemoveRaster( ntfFile )

        File omdFile = "${ FilenameUtils.removeExtension( ntfFile.absolutePath ) }.omd" as File

        ossimUtil?.createOverviews( ntfFile )

        File thumbnail = ossimUtil?.createThumbnail( ntfFile )

        gdalUtil?.setNullValues( thumbnail )

        File mask = gdalUtil?.createMask( thumbnail )
        File shpFile = gdalUtil?.createShapeFile( mask )

        def layer = new Shapefile( shpFile )

        def geom = new MultiPolygon(
            Geometry.cascadedUnion( layer.collectFromFeature { Feature f -> f?.geom } )?.simplify( 0.01 )
        )


        thumbnail.delete()

        omdFile.withWriter { out ->
          out.println "mission_id: SkySat"
          out.println "ground_geom_0: ${ geom }"
        }

        restUtil?.postToAddRaster( ntfFile )
      }
    }

    long stop = System.currentTimeMillis()

    println "${ ( stop - start ) / 1000 }"
  }
}
