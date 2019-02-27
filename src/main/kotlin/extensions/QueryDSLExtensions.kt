package extensions

import com.blazebit.persistence.*
import com.blazebit.persistence.impl.AbstractCommonQueryBuilder
import com.blazebit.persistence.impl.ClauseType
import com.blazebit.persistence.impl.parameterManager
import com.mysema.query.jpa.JPQLSerializer
import com.mysema.query.jpa.JPQLTemplates
import com.mysema.query.types.*
import com.mysema.query.types.Path
import javax.persistence.EntityManager

fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, entityPath : EntityPath<T>) : CriteriaBuilder<T> {
    val entityType : Class<T> = entityPath.annotatedElement as Class<T>
    val alias = entityPath.metadata.name
    return this.create(entityManager, entityType, alias)
}

fun <T, P> CriteriaBuilder<T>.innerJoin(collectionPath : CollectionExpression<*, P>, entityPath : Path<P>): CriteriaBuilder<T> {
    val alias = entityPath.metadata.name
    return innerJoin(parseExpressionAndBindParameters(collectionPath, this), alias)
}

fun <T> CriteriaBuilder<T>.select(expression: Expression<*>): CriteriaBuilder<T> {
    return select(parseExpressionAndBindParameters(expression, this))
}

fun <T> CriteriaBuilder<T>.select(expression: Expression<*>, alias: String): CriteriaBuilder<T> {
    return select(parseExpressionAndBindParameters(expression, this), alias)
}


fun <A : CriteriaBuilder<T>, T> A.where(predicate : Predicate) : CriteriaBuilder<T> {
    val jpqlQueryFragment = parseExpressionAndBindParameters(predicate, this)
    return setWhereExpression(jpqlQueryFragment)
}

// TODO: temporary need CB specific hooks
//fun <A : WhereBuilder<A>> A.where(predicate : Predicate) : A {
//    val ser : JPQLSerializer = JPQLSerializer(JPQLTemplates.DEFAULT)
//    predicate.accept(ser, null)
//    return this.setWhereExpression(ser.toString())
//}

fun <T> CriteriaBuilder<T>.from(entityPath : EntityPath<*>) : CriteriaBuilder<T> {
    val entityType : Class<*> = entityPath.annotatedElement as Class<*>
    val alias = entityPath.metadata.name
    return this.from(entityType, alias)
}


private fun parseExpressionAndBindParameters(expression : Expression<*>, builder : CriteriaBuilder<*>) : String {
    val ser = JPQLSerializer(JPQLTemplates.DEFAULT)
    expression.accept(ser, null)
    var jpqlQueryFragment = ser.toString()

    // TODO UGLYY
    /**
     * This is just a quick proof of concept.
     * In all reality, we probably need to convert the QueryDSL AST to Blaze-Persist AST.
     * This may require some additional methods in the CriteriaBuilder API.
     */
    val parameterManager = builder.parameterManager()

    ser.getConstantToLabel().forEach{ (constant, param) ->
        run {
            val parameter = parameterManager.addParameterExpression(constant, ClauseType.WHERE, builder as AbstractCommonQueryBuilder<*, *, *, *, *>)
            jpqlQueryFragment = jpqlQueryFragment.replace("?$param", ":${parameter.name}")
        }
    }

    return jpqlQueryFragment
}


fun <A : WhereBuilder<A>> A.where(path: Path<*>) : RestrictionBuilder<A> {
    return this.where(getPathExpression(path))
}

fun <A : RestrictionBuilder<A>> A.eqExpression(path: Path<*>) : A {
    return this.eqExpression(getPathExpression(path))
}

private fun getPathExpression(path : Path<*>) : String {
    return generateSequence(path.metadata) { pathMetadata -> pathMetadata.parent?.metadata }
            .map { pathMetadata -> pathMetadata.name }
            .toList()
            .reversed()
            .joinToString(".")
}