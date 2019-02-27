package extensions

import com.blazebit.persistence.*
import com.mysema.query.types.*
import com.mysema.query.types.Path
import javax.persistence.EntityManager

fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, entityPath : EntityPath<T>) : CriteriaBuilder<T> {
    val entityType : Class<T> = entityPath.annotatedElement as Class<T>
    val alias = entityPath.metadata.name
    return this.create(entityManager, entityType, alias)
}

fun <A : CriteriaBuilder<T>, T, P> A.innerJoin(target : CollectionExpression<*, P>, alias : Path<P>): A {
    print(alias)
    return this
}

fun <A : WhereBuilder<A>> A.where(path: Path<*>) : RestrictionBuilder<A> {
    return this.where(path.metadata.name)
}

fun <A : RestrictionBuilder<A>> A.eqExpression(path: Path<*>) : A {
    return this.eqExpression(path.metadata.name)
}

fun <A : WhereBuilder<A>?> WhereBuilder<A>.where(predicate : Predicate) : A {
    return when {
        predicate is Operation<*> -> when (predicate.operator) {
            Ops.STARTS_WITH -> {
                val path = predicate.args[0] as Path<*>
                val constant = predicate.args[1] as Constant<*>
                this.where(path.metadata.name).like()
                        .value(constant.constant.toString() + "%").escape('!')
            }
            else -> TODO()
        }
        else -> TODO()
    }
}

fun <T> CriteriaBuilder<T>.from(entityPath : EntityPath<*>) : CriteriaBuilder<T> {
    val entityType : Class<*> = entityPath.annotatedElement as Class<*>
    val alias = entityPath.metadata.name
    return this.from(entityType, alias)
}

fun <X> SelectBuilder<X>.select(path: Path<*>) : X {
    return this.select(path.toString())
}

fun <X> SelectBuilder<X>.select(path: Path<*>, alias : String) : X {
    return this.select(path.toString(), alias)
}
