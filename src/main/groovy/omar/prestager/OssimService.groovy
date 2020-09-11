package omar.prestager

import groovy.transform.CompileStatic

import javax.inject.Singleton
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

@CompileStatic
@Singleton
class OssimService {
  File scratchDir = new File( '/scratch' )

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
  }
}