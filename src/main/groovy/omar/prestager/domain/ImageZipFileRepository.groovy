package omar.prestager.domain

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ImageZipFileRepository extends CrudRepository<ImageZipFile, Long> {
  Optional<ImageZipFile> findByStatusEquals(String status);
}