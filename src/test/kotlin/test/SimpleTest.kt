package test

import com.blazebit.persistence.Criteria
import domain.Address
import domain.Person
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase
import org.junit.Test
import domain.QPerson.person
import javax.persistence.EntityManager
import extensions.*

class SimpleTest : BaseCoreFunctionalTestCase() {

    @Test
    fun doSomeTest() {

        val config = Criteria.getDefault()
        val cbf = config.createCriteriaBuilderFactory(sessionFactory())
        val result = doInJpa { entityManager -> cbf.create(entityManager, person)
//                .where(person.name.startsWith("test"))
                .where(person.name).eq("")
                .resultList}

        println("Hello")
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
        return arrayOf(Person::class.java, Address::class.java)
    }
}