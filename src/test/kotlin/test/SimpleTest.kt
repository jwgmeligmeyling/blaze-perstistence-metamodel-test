package test

import com.blazebit.persistence.Criteria
import com.blazebit.persistence.CriteriaBuilderFactory
import domain.*
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
                .innerJoinOn(person.documents, ownedDocument)
                    .on(ownedDocument.numPages.eq(5))
                .select(person.id, "pid")
                .select(ownedDocument.numPages.sum())
                .resultList
        }
    }

    @Test
    fun doSomeMoreTest2() {
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

    @Test
    fun doSomeMoreTest3() {
        val result = doInJpa { entityManager ->

            val ownedDocument = QDocument("ownedDocument")

            cbf!!.create(entityManager, Tuple::class)
                .from(person)
                .leftJoin(person.documents, ownedDocument)
                .select(person.id, "pid")
                .select(ownedDocument.numPages.sum())
                .resultList
        }
    }

    @Test
    fun doSomeMoreTest5() {
        val result = doInJpa { entityManager ->

            val ownedDocument = QDocument("ownedDocument")

            cbf!!.create(entityManager, Tuple::class)
                .from(person)
                .leftJoin(person.documents, ownedDocument)
                .where(person.name).eq("test")
                .where(person.name).eqExpression(person.name.toUpperCase())
                .select(person.id, "pid")
                .select(ownedDocument.numPages.sum())
                .resultList
        }
    }

    @Test
    fun doSomeMoreTest6() {
        val result = doInJpa { entityManager ->

            val ownedDocument = QDocument("ownedDocument")

            cbf!!.create(entityManager, Tuple::class)
                .from(person)
                .leftJoin(person.documents, ownedDocument)
                .whereOr()
                    .where(person.name).eq("test")
                    .where(person.name).eqExpression(person.name)
                .endOr()
                .select(person.id, "pid")
                .select(ownedDocument.numPages.sum())
                .resultList
        }
    }

    @Test
    fun doSomeMoreTests7() {
        val result = doInJpa { entityManager ->
            val ownedDocument = QDocument("ownedDocument")

            cbf!!.create(entityManager, QPersonCte.personCte)
                    .with(QPersonCte.personCte)
                    .from(QPerson.person)
                    .leftJoin(person.documents, ownedDocument)
                    .where(QPerson.person.name.eq("dipshit"))
                    .whereOr()
                        .where(person.name).eq("test")
                        .where(person.name).eqExpression(person.name)
                    .endOr()
                    .bind(QPersonCte.personCte.id).select(QPerson.person.id)
                    .bind(QPersonCte.personCte.name).select(QPerson.person.name)
                    .end()
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
        return arrayOf(Person::class.java, Address::class.java, Document::class.java, PersonCte::class.java)
    }
}