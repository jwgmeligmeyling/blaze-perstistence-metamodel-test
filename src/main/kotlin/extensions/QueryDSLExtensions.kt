package extensions

import com.blazebit.persistence.*
import com.mysema.query.jpa.JPQLSerializer
import com.mysema.query.jpa.JPQLTemplates
import com.mysema.query.types.*
import com.mysema.query.types.Path
import javax.persistence.EntityManager

/**
 * Creates a new criteria builder with the given result class. The result class will be used as default from class.
 * The alias will be used as default alias for the from class. Both can be overridden by invoking
 * {@link BaseQueryBuilder#from(java.lang.Class, java.lang.String)}.
 *
 * @param entityManager The entity manager to use for the criteria builder
 * @param entityPath Source of the query
 * @param <T> The type of the result class
 * @return A new criteria builder
 */
fun <T : Any> CriteriaBuilderFactory.create(entityManager : EntityManager, entityPath : EntityPath<T>) : CriteriaBuilder<T> {
    val entityType : Class<T> = entityPath.annotatedElement as Class<T>
    val alias = entityPath.metadata.name
    return this.create(entityManager, entityType, alias)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, P> CriteriaBuilder<T>.innerJoin(path : CollectionExpression<*, P>, alias : Path<P>): CriteriaBuilder<T> {
    val aliasName = alias.metadata.name
    val (expression, parameters) = parseExpressionAndBindParameters(path)
    return innerJoin(expression, aliasName).setParameters(parameters)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, P> CriteriaBuilder<T>.leftJoin(path : CollectionExpression<*, P>, alias : Path<P>): CriteriaBuilder<T> {
    val aliasName = alias.metadata.name
    val (expression, parameters) = parseExpressionAndBindParameters(path)
    return leftJoin(expression, aliasName).setParameters(parameters)
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The query builder for chaining calls
 */
fun <T, P> CriteriaBuilder<T>.rightJoin(path : CollectionExpression<*, P>, alias : Path<P>): CriteriaBuilder<T> {
    val aliasName = alias.metadata.name
    val (expression, parameters) = parseExpressionAndBindParameters(path)
    return rightJoin(expression, aliasName).setParameters(parameters)
}

/**
 * Create an inner join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, P> CriteriaBuilder<T>.innerJoinOn(path : CollectionExpression<*, P>, alias : Path<P>): JoinOnBuilder<CriteriaBuilder<T>> {
    val aliasName = alias.metadata.name
    val expression = parseJPQLExpression(path)
    return innerJoinOn(expression, aliasName)
}

/**
 * Create a left join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, P> CriteriaBuilder<T>.leftJoinOn(collectionPath : CollectionExpression<*, P>, entityPath : Path<P>): JoinOnBuilder<CriteriaBuilder<T>> {
    val aliasName = entityPath.metadata.name
    val expression = parseJPQLExpression(collectionPath)
    return leftJoinOn(expression, aliasName)
}

/**
 * Create a right join with the given target.
 *
 * @param path The path to join
 * @param alias The alias for the joined element
 * @return The restriction builder for the on-clause
 */
fun <T, P> CriteriaBuilder<T>.rightJoinOn(path : CollectionExpression<*, P>, alias : Path<P>): JoinOnBuilder<CriteriaBuilder<T>> {
    val aliasName = alias.metadata.name
    val expression = parseJPQLExpression(path)
    return rightJoinOn(expression, aliasName)
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

/**
 * Adds a select clause with the given expression to the query.
 *
 * @param expression The expression for the select clause
 * @return The query builder for chaining calls
 */
fun <T> CriteriaBuilder<T>.select(expression: Expression<*>): CriteriaBuilder<T> {
    val (expr, parameters) = parseExpressionAndBindParameters(expression)
    return select(expr).setParameters(parameters)
}

/**
 * Adds a select clause with the given expression and alias to the query.
 *
 * @param expression The expression for the select clause
 * @param alias The alias for the expression
 * @return The query builder for chaining calls
 */
fun <T> CriteriaBuilder<T>.select(expression: Expression<*>, alias: String): CriteriaBuilder<T> {
    val (expr, parameters) = parseExpressionAndBindParameters(expression)
    return select(expr, alias).setParameters(parameters)
}

/**
 * Sets the given expression as expression for the where clause.
 *
 * @param predicate The where predicate
 * @return The builder
 */
fun <A : CriteriaBuilder<T>, T> A.where(predicate : Predicate) : CriteriaBuilder<T> {
    val (jpqlQueryFragment, parameters) = parseExpressionAndBindParameters(predicate)
    return setWhereExpression(jpqlQueryFragment).setParameters(parameters)
}

/**
 * Set the sources of this query
 *
 * @param entityPath The entity which should be queried
 * @return The query builder for chaining calls
 */
fun <T> CriteriaBuilder<T>.from(entityPath : EntityPath<*>) : CriteriaBuilder<T> {
    val entityType : Class<*> = entityPath.annotatedElement as Class<*>
    val alias = entityPath.metadata.name
    return this.from(entityType, alias)
}

/**
 * Starts a RestrictionBuilder for a where predicate with the given expression as left hand expression.
 *
 * @param path The left hand expression for a where predicate
 * @return The restriction builder for the given expression
 */
fun <A : BaseWhereBuilder<A>> A.where(path: Path<*>) : RestrictionBuilder<A> {
    var jpqlQueryFragment = parseJPQLExpression(path)
    return this.where(jpqlQueryFragment)
}

/**
 * Finishes the EQ predicate and adds it to the parent predicate container represented by the type T.
 * The predicate checks if the left hand side is equal to the given expression.
 *
 * @param path The expression on the right hand side
 * @return The parent predicate container builder
 */
fun <A : RestrictionBuilder<T>, T> A.eqExpression(path: Path<*>) : T {
    var jpqlQueryFragment = parseJPQLExpression(path)
    return this.eqExpression(jpqlQueryFragment)
}

/**
 * Finishes the EQ predicate and adds it to the parent predicate container represented by the type T.
 * The predicate checks if the left hand side is equal to the given expression.
 *
 * @param expression The expression on the right hand side
 * @return The parent predicate container builder
 */
fun <A : RestrictionBuilder<T>, T> A.eqExpression(expression: Expression<*>) : T {
    var jpqlQueryFragment = parseJPQLExpression(expression)
    return this.eqExpression(jpqlQueryFragment)
}

/**
 * Finishes the NEQ predicate and adds it to the parent predicate container represented by the type T.
 * The predicate checks if the left hand side is equal to the given expression.
 *
 * @param expression The expression on the right hand side
 * @return The parent predicate container builder
 */
fun <A : RestrictionBuilder<T>, T> A.notEqExpression(expression: Expression<*>) : T {
    var jpqlQueryFragment = parseJPQLExpression(expression)
    return this.notEqExpression(jpqlQueryFragment)
}

/**
 * Starts a builder for a between predicate with lower bound expression.
 *
 * @param expression The between start expression
 * @return The BetweenBuilder
 */
fun <A : RestrictionBuilder<T>, T> A.betweenExpression(expression: Expression<*>) : BetweenBuilder<T> {
    var jpqlQueryFragment = parseJPQLExpression(expression)
    return this.betweenExpression(jpqlQueryFragment)
}

/**
 * Starts a builder for a not between predicate with lower bound expression.
 *
 * @param expression The between start expression
 * @return The BetweenBuilder
 */
fun <A : RestrictionBuilder<T>, T> A.notBetweenExpression(expression: Expression<*>) : BetweenBuilder<T> {
    var jpqlQueryFragment = parseJPQLExpression(expression)
    return this.notBetweenExpression(jpqlQueryFragment)
}

private fun  <A : CriteriaBuilder<T>, T> A.setParameters(parameters : Map<String, Any>) : A {
    for ((parameterName, constant) in parameters) {
        setParameter(parameterName, constant)
    }
    return this
}

private fun parseJPQLExpression(expression: Expression<*>): String {
    val ser = JPQLSerializer(JPQLTemplates.DEFAULT)
    expression.accept(ser, null)
    assert( ser.constantToLabel.isEmpty() )
    return ser.toString()
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
