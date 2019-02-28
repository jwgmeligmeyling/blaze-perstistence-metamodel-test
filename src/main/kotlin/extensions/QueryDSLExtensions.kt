package extensions

import com.blazebit.persistence.*
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
    val (expression, parameters) = parseExpressionAndBindParameters(collectionPath)
    return innerJoin(expression, alias).setParameters(parameters)
}


fun <T, P> CriteriaBuilder<T>.innerJoinOn(collectionPath : CollectionExpression<*, P>, entityPath : Path<P>): JoinOnBuilder<CriteriaBuilder<T>> {
    val alias = entityPath.metadata.name
    val (expression, parameters) = parseExpressionAndBindParameters(collectionPath)
    return innerJoinOn(expression, alias)
}

fun <T : CriteriaBuilder<*>> JoinOnBuilder<T>.on(predicate: Predicate) : T {
    val ser = JPQLSerializer(JPQLTemplates.DEFAULT)
    predicate.accept(ser, null)
    var jpqlQueryFragment = ser.toString()


    val parameterNames = ser.constantToLabel.values.map { it to "qsl_param_${Counter.getCount()}" }.toMap()
    for ((label, parameterName) in parameterNames) {
        jpqlQueryFragment = jpqlQueryFragment.replace("?$label", ":$parameterName")
    }

    val result = this.setOnExpression(jpqlQueryFragment)

    for ((constant, label) in ser.constantToLabel) {
        result.setParameter(parameterNames[label], constant)
    }

    return result
}

fun <T, P> CriteriaBuilder<T>.leftJoin(collectionPath : CollectionExpression<*, P>, entityPath : Path<P>): CriteriaBuilder<T> {
    val alias = entityPath.metadata.name
    val (expression, parameters) = parseExpressionAndBindParameters(collectionPath)
    return leftJoin(expression, alias).setParameters(parameters)
}

fun <T> CriteriaBuilder<T>.select(expression: Expression<*>): CriteriaBuilder<T> {
    val (expr, parameters) = parseExpressionAndBindParameters(expression)
    return select(expr).setParameters(parameters)
}

fun <T> CriteriaBuilder<T>.select(expression: Expression<*>, alias: String): CriteriaBuilder<T> {
    val (expr, parameters) = parseExpressionAndBindParameters(expression)
    return select(expr, alias).setParameters(parameters)
}

private fun  <A : CriteriaBuilder<T>, T> A.setParameters(parameters : Map<String, Any>) : A {
    for ((parameterName, constant) in parameters) {
        setParameter(parameterName, constant)
    }
    return this
}


fun <A : CriteriaBuilder<T>, T> A.where(predicate : Predicate) : CriteriaBuilder<T> {
    val (jpqlQueryFragment, parameters) = parseExpressionAndBindParameters(predicate)
    return setWhereExpression(jpqlQueryFragment).setParameters(parameters)
}

fun <T> CriteriaBuilder<T>.from(entityPath : EntityPath<*>) : CriteriaBuilder<T> {
    val entityType : Class<*> = entityPath.annotatedElement as Class<*>
    val alias = entityPath.metadata.name
    return this.from(entityType, alias)
}

private fun CriteriaBuilder<*>.parseExpressionAndBindParameters(expression : Expression<*>) : Pair<String, HashMap<String, Any>> {
    val ser = JPQLSerializer(JPQLTemplates.DEFAULT, this.entityManager)
    expression.accept(ser, null)
    var jpqlQueryFragment = ser.toString()

    val parameters = HashMap<String, Any>()
    for ((constant, label) in ser.constantToLabel) {
        val parameterName = "qsl_param_${Counter.getCount()}"
        parameters[parameterName] = constant
        jpqlQueryFragment = jpqlQueryFragment.replace("?$label", ":$parameterName")
    }

    return Pair(jpqlQueryFragment, parameters)
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