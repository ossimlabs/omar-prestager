package omar.prestager.util

import groovy.transform.CompileStatic
import org.apache.commons.io.FilenameUtils

import javax.inject.Singleton

@CompileStatic
@Singleton
class GdalUtil extends SystemCall {
  File createMask( File thumbnailImage ) {
    File maskImage = "${ FilenameUtils.removeExtension( thumbnailImage.absolutePath ) }_data_mask.vrt" as File

    List<String> cmd = [
        'gdal_translate', '-scale', '1', '255', '1', '1', '-ot', 'Byte', '-of', 'vrt', '-a_nodata', '0',
        thumbnailImage,
        maskImage
    ] as List<String>

    runCommand( cmd )
    maskImage
  }

  File createShapeFile( File maskImage ) {
    File shpFile = "${ FilenameUtils.removeExtension( maskImage.absolutePath ) }.shp" as File

    List<String> cmd = [
        'gdal_polygonize.py', '-8', maskImage, shpFile
    ] as List<String>

    runCommand( cmd )
    shpFile
  }

  void setNullValues(File thumbnailImage) {
    File tempFile = "${thumbnailImage.parentFile}/.${thumbnailImage.name}" as File

    List<String> cmd = [
        'gdal_calc.py', '-A', thumbnailImage, "--outfile=${tempFile}", '--calc="A*(A>0)"', '--NoDataValue=0'
    ] as List<String>

    runCommand( cmd )
    tempFile.renameTo thumbnailImage
  }
}
