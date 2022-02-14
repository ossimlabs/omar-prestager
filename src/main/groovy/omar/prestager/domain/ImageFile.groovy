package omar.prestager.domain

import groovy.transform.CompileStatic
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
// import javax.persistence.Table
import java.time.LocalDateTime

@CompileStatic
@MappedEntity
// @Table(name="test-table")
class ImageFile {
  static enum FileStatus {
    QUEUED,
    STAGING,
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
