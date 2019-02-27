package domain

import javax.persistence.*

@Entity
class Document(
        val name: String,
        @ManyToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER)
        val owner : Person,
        val numPages : Long
) : AbstractJpaPersistable<Long>()
