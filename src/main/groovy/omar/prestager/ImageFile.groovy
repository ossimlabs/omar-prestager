package omar.prestager

import groovy.transform.CompileStatic
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

import java.time.LocalDateTime

@CompileStatic
@MappedEntity
class ImageFile {
  static enum FileStatus {
    QUEUED,
    STAGING,
    READY_TO_INDEX,
    INDEXING,
    COMPLETE,
    FAILED_HISTOGRAM,
    FAILED_POST
  }

  @Id
  @GeneratedValue
  Long id

  String filename

  LocalDateTime insertDate

  String status
}
