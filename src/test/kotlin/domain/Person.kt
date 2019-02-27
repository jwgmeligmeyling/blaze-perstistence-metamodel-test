package domain

import javax.persistence.*

@Entity
class Person(
        val name: String,
        @OneToOne(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
        val address: Address,
        @OneToMany(mappedBy = "owner")
        val documents: Collection<Document>
) : AbstractJpaPersistable<Long>()
