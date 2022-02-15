package omar.prestager.domain

import groovy.transform.CompileStatic
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime

@CompileStatic
@MappedEntity
class ImageZipFile {
  static enum FileStatus {
    QUEUED,
    UNZIPPING,
    READY_TO_INDEX,
    INDEXING,
    COMPLETE,
    FAILED_HISTOGRAM
  }

  @Id
  @GeneratedValue
  Long id

  String filename

  LocalDateTime insertDate

  String status
}
