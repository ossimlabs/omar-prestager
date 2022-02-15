package omar.prestager.util

import groovy.ant.AntBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@CompileStatic
@Singleton
class ZipUtil {

  String getPrefix( File zipFilename ) {
    ZipFile zipFile = new ZipFile( zipFilename )
    ZipEntry ntfFile = zipFile.entries().find { ZipEntry it -> it?.name?.endsWith( '.ntf' ) } as ZipEntry
    String datePart = ntfFile?.name?.split( '_' )?.first()

    "${ datePart[ 0..3 ] }/${ datePart[ 4..5 ] }/${ datePart[ 6..7 ] }"
  }

  @CompileDynamic
  File unzipFile( File zipFile, File archiveDir ) {

    AntBuilder ant = new AntBuilder()
    File imageDir = new File( archiveDir, getPrefix( zipFile ) )

    imageDir.mkdirs()

    ant.unzip(
        src: zipFile,
        dest: imageDir,
        overwrite: "true"
    )

    return imageDir
  }
}
