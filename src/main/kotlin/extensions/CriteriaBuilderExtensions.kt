package extensions

import com.blazebit.persistence.CriteriaBuilder
import com.blazebit.persistence.CriteriaBuilderFactory
import com.blazebit.persistence.SelectBuilder
import com.mysema.query.types.CollectionExpression
import com.mysema.query.types.EntityPath
import com.mysema.query.types.Path
import javax.persistence.EntityManager
import kotlin.reflect.KClass

fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, klass: KClass<T>) : CriteriaBuilder<T> {
    return this.create(entityManager, klass.java)
}


fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, klass: KClass<T>, alias: String) : CriteriaBuilder<T> {
    return this.create(entityManager, klass.java, alias)
}