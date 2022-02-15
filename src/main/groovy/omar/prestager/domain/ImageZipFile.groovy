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
    PROCESSING_IMAGES,
    COMPLETE
  }

  @Id
  @GeneratedValue
  Long id

  String filename

  LocalDateTime insertDate

  String status
}
