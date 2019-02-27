package test

import com.blazebit.persistence.Criteria
import com.blazebit.persistence.CriteriaBuilderFactory
import domain.Address
import domain.Document
import domain.Person
import domain.QDocument
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase
import org.junit.Test
import domain.QPerson.person
import javax.persistence.EntityManager
import extensions.*
import org.junit.Before
import javax.persistence.Tuple

class SimpleTest : BaseCoreFunctionalTestCase() {

    var config = Criteria.getDefault()
    var cbf : CriteriaBuilderFactory? = null;

    @Before
    fun setUp() {
        cbf = config.createCriteriaBuilderFactory(sessionFactory())
    }

    @Test
    fun doSomeTest() {
        val result = doInJpa { entityManager -> cbf!!.create(entityManager, person)
                .where(person.name.eq("test").and(person.name.ne("bleh").or(person.name.eq("test").not())))
//                .where(person.name).eq("")
                .resultList}

        println("Hello")
    }

    @Test
    fun doSomeMoreTest() {
        val result = doInJpa { entityManager ->

            val ownedDocument = QDocument("ownedDocument")

            cbf!!.create(entityManager, Tuple::class)
                .from(person)
                .innerJoin(person.documents, ownedDocument)
                .select(person.id, "pid")
                .select(ownedDocument.numPages.sum())
                .resultList
        }
    }



    private fun <T> doInJpa(fn : (EntityManager) -> T) : T {
        val em = sessionFactory().createEntityManager()
        var res : T
        try {
            try {
                em.transaction.begin()
                res = fn.invoke(em)
                em.transaction.commit()
                return res
            }
            catch (e : Exception) {
                if (em.transaction.isActive) {
                    em.transaction.rollback()
                }
                throw e
            }
        }
        finally {
            em.close()
        }
    }

    override fun getAnnotatedClasses(): Array<Class<*>> {
        return arrayOf(Person::class.java, Address::class.java, Document::class.java)
    }
}