package omar.prestager.util

import groovy.transform.CompileStatic

import org.apache.commons.io.FilenameUtils

import javax.inject.Singleton

@CompileStatic
@Singleton
class OssimUtil extends SystemCall {
  File createThumbnail( File inputImage ) {
    File outputImage = "${ FilenameUtils.removeExtension( inputImage.absolutePath ) }.tif" as File

    List<String> cmd = [
        'ossim-chipper',
        '--op', 'ortho',
        '--histogram-op', 'auto-minmax',
        '--thumbnail', 2048,
        '--srs', 'epsg:4326',
        '--output-radiometry', 'U8',
        '--writer', 'tiff_tiled',
        inputImage?.absolutePath,
        outputImage?.absolutePath
    ] as List<String>

    runCommand( cmd )
    outputImage
  }

  void createOverviews( File inputImage ) {
    File outputImage = "${ FilenameUtils.removeExtension( inputImage.absolutePath ) }.tif" as File

    List<String> cmd = [
        'ossim-img2rr',
        '--create-histogram-fast',
        inputImage
    ] as List<String>

    runCommand( cmd )
  }
}
