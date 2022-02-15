package omar.prestager.util

import groovy.transform.CompileStatic

@CompileStatic
class SystemCall {
   int runCommand( List<String> cmd ) {
    Process proc = cmd.execute()
    StringBuilder stdout = new StringBuilder()
    StringBuilder stderr = new StringBuilder()

    println cmd.join( ' ' )

    proc.consumeProcessOutput( stdout, stderr )

    def exitCode = proc.waitFor()

    if ( exitCode ) {
      System.err.println stderr
    } else {
      println stdout
    }

    exitCode
  }
}
